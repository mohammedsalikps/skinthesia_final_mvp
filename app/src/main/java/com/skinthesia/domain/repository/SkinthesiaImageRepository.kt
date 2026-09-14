package com.skinthesia.domain.repository

import com.skinthesia.domain.model.ImageSource

/** Named photography slots used across the product. */
enum class ImageKey {
    /** Editorial hero on the Welcome screen. */
    WELCOME_HERO,
    /** Soft portrait used as a fallback wherever a user photo is missing. */
    PORTRAIT_SOFT,
}

/**
 * Resolves photography for the UI. The Phase 1 implementation serves bundled
 * placeholders; a CDN-backed implementation can replace it without UI changes.
 */
interface SkinthesiaImageRepository {
    fun image(key: ImageKey): ImageSource
}
