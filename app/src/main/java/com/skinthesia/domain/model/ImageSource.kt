package com.skinthesia.domain.model

/**
 * Where an image comes from. The UI resolves these into painters, so bundled
 * placeholders can be swapped for licensed photography or remote assets without
 * touching feature code.
 */
sealed interface ImageSource {
    /** A bundled drawable resource id. */
    data class Resource(val resId: Int) : ImageSource

    /** An absolute path in app-private storage. */
    data class LocalFile(val path: String) : ImageSource

    /** A remote URL, for a future content backend. */
    data class Remote(val url: String) : ImageSource
}
