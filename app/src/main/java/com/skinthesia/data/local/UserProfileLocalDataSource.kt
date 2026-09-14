package com.skinthesia.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.skinthesia.domain.model.AgeRange
import com.skinthesia.domain.model.LensFacing
import com.skinthesia.domain.model.LifestyleFactor
import com.skinthesia.domain.model.PhotoSource
import com.skinthesia.domain.model.SkinConcern
import com.skinthesia.domain.model.SkinGoal
import com.skinthesia.domain.model.SkinPhoto
import com.skinthesia.domain.model.SkinProfile
import com.skinthesia.domain.model.SkinTarget
import com.skinthesia.domain.model.SkinType
import com.skinthesia.domain.model.TargetGoal
import com.skinthesia.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.userProfileDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_profile")

/**
 * Persists the [UserProfile] in Preferences DataStore. Enum values are stored
 * by name; unknown names (from a future schema) degrade to null rather than crash.
 */
class UserProfileLocalDataSource(context: Context) {

    private val dataStore = context.applicationContext.userProfileDataStore

    val profile: Flow<UserProfile> = dataStore.data.map { it.toUserProfile() }

    suspend fun update(transform: (UserProfile) -> UserProfile) {
        dataStore.edit { prefs ->
            val current = prefs.toUserProfile()
            val next = transform(current)
            prefs.write(next.ensureIdentity())
        }
    }

    private fun UserProfile.ensureIdentity(): UserProfile =
        if (id.isBlank() || createdAt == 0L) copy(
            id = id.ifBlank { UUID.randomUUID().toString() },
            createdAt = if (createdAt == 0L) System.currentTimeMillis() else createdAt,
        ) else this

    private fun Preferences.toUserProfile(): UserProfile = UserProfile(
        id = this[Keys.ID].orEmpty(),
        displayName = this[Keys.DISPLAY_NAME],
        createdAt = this[Keys.CREATED_AT] ?: 0L,
        onboardingCompleted = this[Keys.ONBOARDING_COMPLETED] ?: false,
        skinProfile = toSkinProfile(),
    )

    private fun Preferences.toSkinProfile(): SkinProfile {
        val targetGoals = this[Keys.TARGET_GOALS].orEmpty().mapNotNull { enumOrNull<TargetGoal>(it) }.toSet()
        val targetPhrase = this[Keys.TARGET_PHRASE]
        val target = if (targetPhrase != null || targetGoals.isNotEmpty()) {
            SkinTarget(
                phrase = targetPhrase.orEmpty(),
                goals = targetGoals,
                durationWeeks = this[Keys.TARGET_WEEKS] ?: SkinTarget.DEFAULT_DURATION_WEEKS,
            )
        } else {
            null
        }
        return SkinProfile(
            ageRange = this[Keys.AGE_RANGE]?.let { enumOrNull<AgeRange>(it) },
            skinType = this[Keys.SKIN_TYPE]?.let { enumOrNull<SkinType>(it) },
            concerns = this[Keys.CONCERNS].orEmpty().mapNotNull { enumOrNull<SkinConcern>(it) }.toSet(),
            goals = this[Keys.GOALS].orEmpty()
                .split(LIST_SEPARATOR)
                .filter { it.isNotBlank() }
                .mapNotNull { enumOrNull<SkinGoal>(it) },
            target = target,
            lifestyleFactors = this[Keys.LIFESTYLE].orEmpty().mapNotNull { enumOrNull<LifestyleFactor>(it) }.toSet(),
            baselinePhoto = toPhoto(),
        )
    }

    private fun Preferences.toPhoto(): SkinPhoto? {
        val path = this[Keys.PHOTO_PATH] ?: return null
        return SkinPhoto(
            id = this[Keys.PHOTO_ID] ?: path,
            filePath = path,
            capturedAt = this[Keys.PHOTO_CAPTURED_AT] ?: 0L,
            source = this[Keys.PHOTO_SOURCE]?.let { enumOrNull<PhotoSource>(it) } ?: PhotoSource.CAMERA,
            lensFacing = this[Keys.PHOTO_LENS]?.let { enumOrNull<LensFacing>(it) },
            mirrored = this[Keys.PHOTO_MIRRORED] ?: false,
        )
    }

    private fun MutablePreferences.write(profile: UserProfile) {
        this[Keys.ID] = profile.id
        setOrRemove(Keys.DISPLAY_NAME, profile.displayName)
        this[Keys.CREATED_AT] = profile.createdAt
        this[Keys.ONBOARDING_COMPLETED] = profile.onboardingCompleted

        val skin = profile.skinProfile
        setOrRemove(Keys.AGE_RANGE, skin.ageRange?.name)
        setOrRemove(Keys.SKIN_TYPE, skin.skinType?.name)
        this[Keys.CONCERNS] = skin.concerns.map { it.name }.toSet()
        this[Keys.GOALS] = skin.goals.joinToString(LIST_SEPARATOR) { it.name }
        this[Keys.LIFESTYLE] = skin.lifestyleFactors.map { it.name }.toSet()

        val target = skin.target
        if (target == null) {
            remove(Keys.TARGET_PHRASE)
            remove(Keys.TARGET_GOALS)
            remove(Keys.TARGET_WEEKS)
        } else {
            this[Keys.TARGET_PHRASE] = target.phrase
            this[Keys.TARGET_GOALS] = target.goals.map { it.name }.toSet()
            this[Keys.TARGET_WEEKS] = target.durationWeeks
        }

        val photo = skin.baselinePhoto
        if (photo == null) {
            remove(Keys.PHOTO_ID)
            remove(Keys.PHOTO_PATH)
            remove(Keys.PHOTO_CAPTURED_AT)
            remove(Keys.PHOTO_SOURCE)
            remove(Keys.PHOTO_LENS)
            remove(Keys.PHOTO_MIRRORED)
        } else {
            this[Keys.PHOTO_ID] = photo.id
            this[Keys.PHOTO_PATH] = photo.filePath
            this[Keys.PHOTO_CAPTURED_AT] = photo.capturedAt
            this[Keys.PHOTO_SOURCE] = photo.source.name
            setOrRemove(Keys.PHOTO_LENS, photo.lensFacing?.name)
            this[Keys.PHOTO_MIRRORED] = photo.mirrored
        }
    }

    private fun MutablePreferences.setOrRemove(key: Preferences.Key<String>, value: String?) {
        if (value == null) remove(key) else this[key] = value
    }

    private inline fun <reified T : Enum<T>> enumOrNull(name: String): T? =
        runCatching { enumValueOf<T>(name) }.getOrNull()

    private object Keys {
        val ID = stringPreferencesKey("id")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val CREATED_AT = longPreferencesKey("created_at")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val AGE_RANGE = stringPreferencesKey("age_range")
        val SKIN_TYPE = stringPreferencesKey("skin_type")
        val CONCERNS = stringSetPreferencesKey("concerns")
        val GOALS = stringPreferencesKey("goals")
        val LIFESTYLE = stringSetPreferencesKey("lifestyle_factors")
        val TARGET_PHRASE = stringPreferencesKey("target_phrase")
        val TARGET_GOALS = stringSetPreferencesKey("target_goals")
        val TARGET_WEEKS = intPreferencesKey("target_weeks")
        val PHOTO_ID = stringPreferencesKey("photo_id")
        val PHOTO_PATH = stringPreferencesKey("photo_path")
        val PHOTO_CAPTURED_AT = longPreferencesKey("photo_captured_at")
        val PHOTO_SOURCE = stringPreferencesKey("photo_source")
        val PHOTO_LENS = stringPreferencesKey("photo_lens")
        val PHOTO_MIRRORED = booleanPreferencesKey("photo_mirrored")
    }

    private companion object {
        const val LIST_SEPARATOR = ","
    }
}
