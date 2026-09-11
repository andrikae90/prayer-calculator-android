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
    private var playlist: List<Int> = emptyList()
    private var playlistSurah: Int? = null
    private var playlistIndex = 0

    fun toggle(chapterNumber: Int, ayahNumber: Int) {
        stopPlaylist()
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

    fun playAll(chapterNumber: Int, ayahNumbers: List<Int>) {
        if (ayahNumbers.isEmpty()) return
        release()
        playlistSurah = chapterNumber
        playlist = ayahNumbers
        playlistIndex = 0
        playPlaylistItem()
    }

    fun stopAll() {
        stopPlaylist()
        release()
    }

    private fun stopPlaylist() {
        playlist = emptyList()
        playlistSurah = null
        playlistIndex = 0
    }

    private fun playPlaylistItem() {
        val chapterNumber = playlistSurah ?: return
        if (playlistIndex >= playlist.size) {
            stopPlaylist()
            release()
            return
        }
        val ayahNumber = playlist[playlistIndex]
        val key = "$chapterNumber:$ayahNumber"
        prepareAndPlay(chapterNumber, ayahNumber, key, advancePlaylist = true)
    }

    private fun prepareAndPlay(
        chapterNumber: Int,
        ayahNumber: Int,
        key: String,
        advancePlaylist: Boolean = false
    ) {
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
                this@QuranAudioController.isLoading = false
                start()
                this@QuranAudioController.isPlaying = true
            }
            setOnCompletionListener {
                this@QuranAudioController.isPlaying = false
                if (advancePlaylist && this@QuranAudioController.playlist.isNotEmpty()) {
                    this@QuranAudioController.playlistIndex++
                    this@QuranAudioController.player?.release()
                    this@QuranAudioController.player = null
                    this@QuranAudioController.playPlaylistItem()
                }
            }
            setOnErrorListener { _, _, _ ->
                this@QuranAudioController.isLoading = false
                this@QuranAudioController.isPlaying = false
                if (advancePlaylist) {
                    this@QuranAudioController.playlistIndex++
                    this@QuranAudioController.player?.release()
                    this@QuranAudioController.player = null
                    this@QuranAudioController.playPlaylistItem()
                } else {
                    release()
                }
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
