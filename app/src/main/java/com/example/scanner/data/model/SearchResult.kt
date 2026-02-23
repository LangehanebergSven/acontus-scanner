package com.example.scanner.data.model

sealed class SearchResult {
    data class ArticleResult(val article: Article) : SearchResult()
    data class MaterialResult(val material: Material) : SearchResult()
}
