package com.example.scanner.domain

import com.example.scanner.data.local.dao.ArticleDao
import com.example.scanner.data.local.dao.MaterialDao
import com.example.scanner.data.model.SearchResult
import javax.inject.Inject

class SearchItemsUseCase @Inject constructor(
    private val articleDao: ArticleDao,
    private val materialDao: MaterialDao
) {
    suspend operator fun invoke(query: String): List<SearchResult> {
        if (query.isBlank()) return emptyList()

        val articleResults = articleDao.searchArticles(query).map { SearchResult.ArticleResult(it) }
        val materialResults = materialDao.searchMaterials(query).map { SearchResult.MaterialResult(it) }

        return articleResults + materialResults
    }
}
