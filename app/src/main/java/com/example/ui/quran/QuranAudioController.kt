package com.example.ui.quran

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class QuranAudioController {
    var currentKey by mutableStateOf<String?>(null)
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var isLoading by mutableStateOf(false)
        private set

    private var player: MediaPlayer? = null

    fun toggle(chapterNumber: Int, ayahNumber: Int) {
        val key = "$chapterNumber:$ayahNumber"
        if (currentKey == key) {
            when {
                isLoading -> Unit
                isPlaying -> {
                    player?.pause()
                    isPlaying = false
                }
                player != null -> {
                    player?.start()
                    isPlaying = true
                }
                else -> prepareAndPlay(chapterNumber, ayahNumber, key)
            }
            return
        }

        release()
        prepareAndPlay(chapterNumber, ayahNumber, key)
    }

    private fun prepareAndPlay(chapterNumber: Int, ayahNumber: Int, key: String) {
        val chapter = chapterNumber.toString().padStart(3, '0')
        val ayah = ayahNumber.toString().padStart(3, '0')
        val url = "https://verses.quran.foundation/Alafasy/mp3/$chapter$ayah.mp3"

        currentKey = key
        isLoading = true
        isPlaying = false

        player = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(url)
            setOnPreparedListener {
                isLoading = false
                start()
                isPlaying = true
            }
            setOnCompletionListener {
                isPlaying = false
            }
            setOnErrorListener { _, _, _ ->
                isLoading = false
                isPlaying = false
                release()
                true
            }
            prepareAsync()
        }
    }

    fun release() {
        player?.runCatching { stop() }
        player?.release()
        player = null
        isLoading = false
        isPlaying = false
        currentKey = null
    }
}
