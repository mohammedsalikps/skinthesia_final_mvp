package com.skinthesia.domain.model

enum class ArticleCategory { FOR_YOU, SKIN_SCIENCE, INGREDIENTS }

/** Editorial content for Skinthesia Learn (screens 17 and 18). */
data class LearningArticle(
    val id: String,
    val title: String,
    val category: ArticleCategory,
    val readMinutes: Int,
    val summary: String,
    /** Plain-text paragraphs separated by blank lines. */
    val body: String,
    val heroImage: ImageSource,
    val relatedTopics: List<String> = emptyList(),
)
