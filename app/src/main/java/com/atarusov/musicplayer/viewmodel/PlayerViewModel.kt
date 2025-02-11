package com.atarusov.musicplayer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atarusov.musicplayer.data.Playlist
import com.atarusov.musicplayer.data.Song
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class PlayerCommand {
    data class Play(val song: Song) : PlayerCommand()
    data object Pause : PlayerCommand()
}

class PlayerViewModel : ViewModel() {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _song = MutableStateFlow(Playlist.playlist[0])
    val song: StateFlow<Song> = _song

    private val _playerCommands = MutableSharedFlow<PlayerCommand>()
    val playerCommands: SharedFlow<PlayerCommand> = _playerCommands

    fun onClickPlayPauseButton() {
        if (!_isPlaying.value) playCurrentSong()
        else pause()
    }

    fun onClickNextButton() {
        var newSongId = song.value.id + 1
        if (newSongId >= Playlist.playlist.size) newSongId = 0
        _song.value = Playlist.playlist[newSongId]
        playCurrentSong()
    }

    fun onClickPrevButton() {
        var newSongId = song.value.id - 1
        if (newSongId < 0) newSongId = Playlist.playlist.size - 1
        _song.value = Playlist.playlist[newSongId]
        playCurrentSong()
    }

    private fun playCurrentSong() {
        _isPlaying.value = true
        viewModelScope.launch {
            _playerCommands.emit(PlayerCommand.Play(song.value))
        }
    }

    private fun pause() {
        _isPlaying.value = false
        viewModelScope.launch {
            _playerCommands.emit(
                PlayerCommand.Pause
            )
        }
    }
}
