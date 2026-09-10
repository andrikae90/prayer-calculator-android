package com.example.data.repository

import com.example.domain.model.QuranAyah
import com.squareup.moshi.JsonClass
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface QuranAyahRepository {
    suspend fun getAyahs(chapterNumber: Int): Result<List<QuranAyah>>
}

class RemoteQuranAyahRepository : QuranAyahRepository {
    private val api: QuranApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.quran.com/api/v4/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(QuranApi::class.java)
    }

    override suspend fun getAyahs(chapterNumber: Int): Result<List<QuranAyah>> = runCatching {
        api.getVerses(chapterNumber).verses.map { verse ->
            QuranAyah(
                number = verse.verseNumber,
                textArabic = verse.textUthmani,
                translation = verse.translations.firstOrNull()?.text
                    ?.replace(Regex("<[^>]*>"), "")
                    .orEmpty()
            )
        }
    }
}

private interface QuranApi {
    @GET("verses/by_chapter/{chapter_number}")
    suspend fun getVerses(@retrofit2.http.Path("chapter_number") chapterNumber: Int): QuranVerseResponse
}

@JsonClass(generateAdapter = true)
private data class QuranVerseResponse(
    val verses: List<QuranVerse> = emptyList()
)

@JsonClass(generateAdapter = true)
private data class QuranVerse(
    @com.squareup.moshi.Json(name = "verse_key") val verseKey: String,
    @com.squareup.moshi.Json(name = "text_uthmani") val textUthmani: String,
    val translations: List<QuranTranslation> = emptyList()
) {
    val verseNumber: Int get() = verseKey.substringAfter(":").toIntOrNull() ?: 0
}

@JsonClass(generateAdapter = true)
private data class QuranTranslation(
    val text: String = ""
)
