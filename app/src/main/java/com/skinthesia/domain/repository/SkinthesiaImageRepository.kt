package com.skinthesia.domain.repository

import com.skinthesia.domain.model.ImageSource

/** Named photography slots used across the product. */
enum class ImageKey {
    /** Editorial hero on the Welcome screen. */
    WELCOME_HERO,
    /** Soft light-and-shadow backdrop for check-ins, journey and editorial cards. */
    EDITORIAL_LIGHT,
}

/**
 * Resolves photography for the UI. The bundled implementation serves generated
 * editorial imagery; a CDN-backed implementation can replace it without UI changes.
 */
interface SkinthesiaImageRepository {
    fun image(key: ImageKey): ImageSource
}
