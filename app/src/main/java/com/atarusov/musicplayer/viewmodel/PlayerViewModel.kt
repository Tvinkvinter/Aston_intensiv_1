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
    data class PlayPause(val isPlaying: Boolean) : PlayerCommand()
    data class SetNewSong(val song: Song) : PlayerCommand()
}

class PlayerViewModel : ViewModel() {

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private val _song = MutableStateFlow(Playlist.playlist[0])
    val song: StateFlow<Song> = _song

    private val _playerCommands = MutableSharedFlow<PlayerCommand>()
    val playerCommands: SharedFlow<PlayerCommand> = _playerCommands

    fun onClickPlayPauseButton() {
        _isPlaying.value = !isPlaying.value
        viewModelScope.launch {
            _playerCommands.emit(PlayerCommand.PlayPause(isPlaying.value))
        }
    }

    fun onClickNextButton() {
        var newSongId = song.value.id + 1
        if (newSongId >= Playlist.playlist.size) newSongId = 0

        setNewSong(Playlist.playlist[newSongId])
    }

    fun onClickPrevButton() {
        var newSongId = song.value.id - 1
        if (newSongId < 0) newSongId = Playlist.playlist.size - 1

        setNewSong(Playlist.playlist[newSongId])
    }

    private fun setNewSong(song: Song) {
        _song.value = song
        viewModelScope.launch {
            _playerCommands.emit(PlayerCommand.SetNewSong(song))
        }
    }
}
