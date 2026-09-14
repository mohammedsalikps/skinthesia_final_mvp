package com.skinthesia.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ArticleCategory(val label: String) {
    ACNE("Acne"),
    HYDRATION("Hydration"),
    INGREDIENTS("Ingredients"),
    PIGMENTATION("Pigmentation"),
    SUNSCREEN("Sunscreen"),
    SKIN_HEALTH("Skin health"),
    LIFESTYLE("Lifestyle"),
}

/** Structured article content, rendered with the editorial typography. */
@Serializable
sealed interface ArticleBlock {
    @Serializable
    @SerialName("paragraph")
    data class Paragraph(val text: String) : ArticleBlock

    @Serializable
    @SerialName("heading")
    data class Heading(val text: String) : ArticleBlock

    @Serializable
    @SerialName("callout")
    data class Callout(val title: String, val text: String) : ArticleBlock

    @Serializable
    @SerialName("bullets")
    data class Bullets(val items: List<String>) : ArticleBlock

    @Serializable
    @SerialName("takeaways")
    data class Takeaways(val items: List<String>) : ArticleBlock
}

@Serializable
data class LearningArticle(
    val id: String,
    val title: String,
    val subtitle: String,
    val category: ArticleCategory,
    val authorName: String,
    val authorRole: String,
    /** When written by an expert who also consults, links to their profile. */
    val expertId: String? = null,
    val readMinutes: Int,
    /** ISO date, for example "2026-08-12". */
    val publishedOn: String,
    val blocks: List<ArticleBlock>,
    /** Goals this article is most useful for; drives SkinPrint-aware personalisation. */
    val relevantGoals: Set<SkinGoal>,
    val relatedArticleIds: List<String> = emptyList(),
    val featured: Boolean = false,
)
