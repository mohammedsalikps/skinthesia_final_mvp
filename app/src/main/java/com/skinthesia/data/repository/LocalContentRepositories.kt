package com.skinthesia.data.repository

import com.skinthesia.data.local.SkinthesiaJson
import com.skinthesia.data.local.db.BookingDao
import com.skinthesia.data.local.db.BookingEntity
import com.skinthesia.data.local.db.CommunityDao
import com.skinthesia.data.local.db.LibraryDao
import com.skinthesia.data.local.db.LikeEntity
import com.skinthesia.data.local.db.SavedArticleEntity
import com.skinthesia.data.local.db.UserCommentEntity
import com.skinthesia.data.local.db.UserPostEntity
import com.skinthesia.data.seed.ArticleSeed
import com.skinthesia.data.seed.CommunitySeed
import com.skinthesia.data.seed.ExpertSeed
import com.skinthesia.domain.model.Booking
import com.skinthesia.domain.model.BookingStatus
import com.skinthesia.domain.model.CommunityAuthor
import com.skinthesia.domain.model.CommunityComment
import com.skinthesia.domain.model.CommunityPost
import com.skinthesia.domain.model.CommunitySection
import com.skinthesia.domain.model.ConsultationExpert
import com.skinthesia.domain.model.ConsultationSlot
import com.skinthesia.domain.model.ConsultationType
import com.skinthesia.domain.model.Ids
import com.skinthesia.domain.model.LearningArticle
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.repository.AssessmentRepository
import com.skinthesia.domain.repository.CommunityRepository
import com.skinthesia.domain.repository.ConsultationRepository
import com.skinthesia.domain.repository.LearningRepository
import com.skinthesia.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId

/**
 * Bundled experts with locally generated availability. Bookings are stored on the
 * device and clearly simulated until a real scheduling service is connected.
 */
class LocalConsultationRepository(
    private val dao: BookingDao,
    private val clock: () -> Long = System::currentTimeMillis,
    private val zone: ZoneId = ZoneId.systemDefault(),
    seed: List<ConsultationExpert> = ExpertSeed.experts,
) : ConsultationRepository {

    private val all = seed

    override val experts: Flow<List<ConsultationExpert>> = flowOf(all)

    override suspend fun expert(id: String): ConsultationExpert? = all.firstOrNull { it.id == id }

    override suspend fun slots(expertId: String, date: LocalDate): List<ConsultationSlot> {
        if (date.dayOfWeek == DayOfWeek.SUNDAY) return emptyList()
        val booked = dao.confirmedFor(expertId).map { it.startAt }.toSet()
        val times = if (date.dayOfWeek == DayOfWeek.SATURDAY) WEEKEND_TIMES else WEEKDAY_TIMES
        val now = clock()
        return times.mapIndexed { index, (hour, minute) ->
            val start = date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()
            val seeded = (expertId.hashCode() + date.dayOfYear * 7 + index * 13).mod(5) != 0
            ConsultationSlot(
                id = "slot-$expertId-$start",
                expertId = expertId,
                startAt = start,
                durationMinutes = SLOT_MINUTES,
                isAvailable = seeded && start > now + LEAD_TIME_MILLIS && start !in booked,
            )
        }
    }

    override val bookings: Flow<List<Booking>> = dao.all().map { list -> list.mapNotNull { it.toDomain() } }

    override suspend fun booking(id: String): Booking? = dao.get(id)?.toDomain()

    override suspend fun book(
        expert: ConsultationExpert,
        slot: ConsultationSlot,
        type: ConsultationType,
        notes: String,
        shareSkinPrint: Boolean,
    ): Booking {
        val booking = Booking(
            id = Ids.new("bk"),
            expertId = expert.id,
            expertName = expert.name,
            startAt = slot.startAt,
            durationMinutes = type.durationMinutes,
            type = type,
            notes = notes.trim(),
            shareSkinPrint = shareSkinPrint,
            createdAt = clock(),
            status = BookingStatus.CONFIRMED,
            isSimulated = true,
        )
        dao.upsert(booking.toEntity())
        return booking
    }

    override suspend fun cancel(bookingId: String) {
        val booking = booking(bookingId) ?: return
        dao.upsert(booking.copy(status = BookingStatus.CANCELLED).toEntity())
    }

    override suspend fun deleteAll() = dao.deleteAll()

    private fun Booking.toEntity() = BookingEntity(id, expertId, startAt, status.name, SkinthesiaJson.encodeToString(Booking.serializer(), this))

    private fun BookingEntity.toDomain(): Booking? =
        runCatching { SkinthesiaJson.decodeFromString(Booking.serializer(), json) }.getOrNull()

    private companion object {
        const val SLOT_MINUTES = 30
        const val LEAD_TIME_MILLIS = 60 * 60 * 1000L
        val WEEKDAY_TIMES = listOf(9 to 30, 11 to 0, 12 to 30, 15 to 0, 16 to 30, 18 to 0, 19 to 30)
        val WEEKEND_TIMES = listOf(10 to 0, 11 to 30, 13 to 0)
    }
}

class LocalLearningRepository(
    private val dao: LibraryDao,
    private val clock: () -> Long = System::currentTimeMillis,
    seed: List<LearningArticle> = ArticleSeed.articles,
) : LearningRepository {

    private val all = seed.sortedByDescending { it.publishedOn }

    override val articles: Flow<List<LearningArticle>> = flowOf(all)

    override suspend fun article(id: String): LearningArticle? = all.firstOrNull { it.id == id }

    override val savedIds: Flow<Set<String>> = dao.savedIds().map { it.toSet() }

    override suspend fun toggleSaved(id: String) {
        if (dao.isSaved(id) > 0) dao.remove(id) else dao.save(SavedArticleEntity(id, clock()))
    }
}

/**
 * Seed conversations merged with the member's own posts, comments and likes (stored
 * in Room). A community backend replaces this behind [CommunityRepository].
 */
class LocalCommunityRepository(
    private val dao: CommunityDao,
    private val profiles: UserProfileRepository,
    private val assessments: AssessmentRepository,
    private val clock: () -> Long = System::currentTimeMillis,
) : CommunityRepository {

    private val seedPosts: List<CommunityPost> = CommunitySeed.posts(clock())

    private val allPosts: Flow<List<CommunityPost>> = combine(dao.posts(), dao.comments(), dao.likes()) { posts, comments, likes ->
        val liked = likes.map { it.targetId }.toSet()
        val userPosts = posts.mapNotNull { runCatching { SkinthesiaJson.decodeFromString(CommunityPost.serializer(), it.json) }.getOrNull() }
        val userComments = comments.mapNotNull { runCatching { SkinthesiaJson.decodeFromString(CommunityComment.serializer(), it.json) }.getOrNull() }
            .groupBy { it.postId }
        (userPosts + seedPosts).map { post ->
            val mergedComments = (post.comments + userComments[post.id].orEmpty()).map { comment ->
                val likedComment = comment.id in liked
                comment.copy(likedByMe = likedComment, likeCount = comment.likeCount + if (likedComment) 1 else 0)
            }
            val likedPost = post.id in liked
            post.copy(comments = mergedComments, likedByMe = likedPost, likeCount = post.likeCount + if (likedPost) 1 else 0)
        }
    }

    override fun feed(section: CommunitySection, goals: Set<SkinGoal>): Flow<List<CommunityPost>> = allPosts.map { posts ->
        when (section) {
            CommunitySection.FOR_YOU -> posts
                .sortedWith(
                    compareByDescending<CommunityPost> { it.isPinned }
                        .thenByDescending { post -> post.relatedGoals.count { it in goals } * 3 + if (post.hasExpertAnswer) 1 else 0 }
                        .thenByDescending { it.createdAt },
                )
            else -> posts.filter { it.section == section }
                .sortedWith(compareByDescending<CommunityPost> { it.isPinned }.thenByDescending { it.createdAt })
        }
    }

    override fun post(id: String): Flow<CommunityPost?> = allPosts.map { posts -> posts.firstOrNull { it.id == id } }

    override suspend fun toggleLike(postId: String) = toggle(postId)

    override suspend fun toggleCommentLike(commentId: String) = toggle(commentId)

    private suspend fun toggle(targetId: String) {
        if (dao.isLiked(targetId) > 0) dao.unlike(targetId) else dao.like(LikeEntity(targetId, clock()))
    }

    override suspend fun addComment(postId: String, body: String): CommunityComment {
        val comment = CommunityComment(
            id = Ids.new("cmt"),
            postId = postId,
            author = currentAuthor(),
            body = body.trim(),
            createdAt = clock(),
            likeCount = 0,
        )
        dao.insertComment(UserCommentEntity(comment.id, postId, comment.createdAt, SkinthesiaJson.encodeToString(CommunityComment.serializer(), comment)))
        return comment
    }

    override suspend fun createPost(section: CommunitySection, title: String, body: String, tags: List<String>): CommunityPost {
        val profile = profiles.current()
        val post = CommunityPost(
            id = Ids.new("post"),
            section = section,
            author = currentAuthor(),
            title = title.trim(),
            body = body.trim(),
            tags = tags.map { it.trim().lowercase() }.filter { it.isNotBlank() }.distinct().take(3),
            createdAt = clock(),
            likeCount = 0,
            relatedGoals = profile.goals.goals.take(2).toSet(),
        )
        dao.insertPost(UserPostEntity(post.id, post.createdAt, SkinthesiaJson.encodeToString(CommunityPost.serializer(), post)))
        return post
    }

    override suspend fun deleteUserContent() {
        dao.deletePosts()
        dao.deleteComments()
        dao.deleteLikes()
    }

    private suspend fun currentAuthor(): CommunityAuthor {
        val profile = profiles.current()
        val week = assessments.completedList().maxOfOrNull { it.week }
        val name = profile.firstName.trim().ifBlank { "You" }
        return CommunityAuthor(
            id = profile.id.ifBlank { "me" },
            displayName = name,
            journeyWeek = week,
            isCurrentUser = true,
        )
    }
}
