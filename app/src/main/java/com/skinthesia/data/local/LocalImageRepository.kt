package com.skinthesia.data.local

import com.skinthesia.R
import com.skinthesia.domain.model.ImageSource
import com.skinthesia.domain.repository.ImageKey
import com.skinthesia.domain.repository.SkinthesiaImageRepository

/**
 * Serves bundled placeholder photography. Swap the drawables under
 * `res/drawable-nodpi` for licensed photography, or replace this class with a
 * remote implementation, without touching any screen.
 */
class LocalImageRepository : SkinthesiaImageRepository {
    override fun image(key: ImageKey): ImageSource = when (key) {
        ImageKey.WELCOME_HERO -> ImageSource.Resource(R.drawable.img_hero_placeholder)
        ImageKey.PORTRAIT_SOFT -> ImageSource.Resource(R.drawable.img_portrait_placeholder)
    }
}
