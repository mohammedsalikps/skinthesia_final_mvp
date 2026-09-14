package com.skinthesia.data.local

import com.skinthesia.R
import com.skinthesia.domain.model.ImageSource
import com.skinthesia.domain.repository.ImageKey
import com.skinthesia.domain.repository.SkinthesiaImageRepository

/**
 * Serves the bundled editorial imagery (generated light-and-shadow studies, no people
 * and no third-party rights). Replace the files in `res/drawable-nodpi`, or swap this
 * class for a CDN-backed one, to use licensed photography without touching any screen.
 */
class LocalImageRepository : SkinthesiaImageRepository {
    override fun image(key: ImageKey): ImageSource = when (key) {
        ImageKey.WELCOME_HERO -> ImageSource.Resource(R.drawable.img_hero_botanical)
        ImageKey.EDITORIAL_LIGHT -> ImageSource.Resource(R.drawable.img_editorial_light)
    }
}
