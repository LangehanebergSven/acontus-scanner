package com.example.scanner.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.scanner.data.model.Article

@Dao
interface ArticleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(articles: List<Article>)

    @Query("SELECT * FROM articles WHERE articleId = :articleId")
    suspend fun getArticleById(articleId: String): Article?
    
    @Query("SELECT * FROM articles WHERE ean = :ean")
    suspend fun getArticleByEan(ean: String): Article?

    @Query("SELECT * FROM articles WHERE name LIKE '%' || :query || '%' OR articleId LIKE '%' || :query || '%'")
    suspend fun searchArticles(query: String): List<Article>

    @Query("DELETE FROM articles")
    suspend fun clearAll()
}
