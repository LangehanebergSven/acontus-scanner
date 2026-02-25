package com.example.scanner.domain

import com.example.scanner.data.local.dao.ArticleDao
import com.example.scanner.data.local.dao.MaterialDao
import com.example.scanner.data.model.SearchResult
import javax.inject.Inject

class SearchItemsUseCase @Inject constructor(
    private val articleDao: ArticleDao,
    private val materialDao: MaterialDao
) {
    suspend operator fun invoke(query: String, typeFilter: String? = null): List<SearchResult> {
        if (query.isBlank()) return emptyList()

        val searchArticles = typeFilter == null || typeFilter == "Artikel"
        val searchMaterials = typeFilter == null || typeFilter == "Material"

        val results = mutableListOf<SearchResult>()

        if (searchArticles) {
            results.addAll(articleDao.searchArticles(query).map { SearchResult.ArticleResult(it) })
        }
        if (searchMaterials) {
            results.addAll(materialDao.searchMaterials(query).map { SearchResult.MaterialResult(it) })
        }

        return results
    }
}
