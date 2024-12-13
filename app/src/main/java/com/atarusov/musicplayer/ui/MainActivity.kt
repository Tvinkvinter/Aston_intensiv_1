package com.atarusov.musicplayer.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.atarusov.musicplayer.viewmodel.PlayerCommand
import com.atarusov.musicplayer.service.PlayerService
import com.atarusov.musicplayer.viewmodel.PlayerViewModel
import com.atarusov.musicplayer.R
import com.atarusov.musicplayer.data.Song
import com.atarusov.musicplayer.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initPlayer(viewModel.song.value)

        lifecycleScope.launch {
            viewModel.isPlaying.collect { isPlaying ->
                setPlaying(isPlaying)
            }
        }

        lifecycleScope.launch {
            viewModel.song.collect { song ->
                setSong(song)
            }
        }

        lifecycleScope.launch {
            viewModel.playerCommands.collect{ command ->
                when(command) {
                    is PlayerCommand.PlayPause -> sendPlayPauseCommandToPlayer(command.isPlaying)
                    is PlayerCommand.SetNewSong -> sendSetNewSongCommandToPlayer(command.song)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                0
            )

        binding.btnPlayPause.setOnClickListener {
            viewModel.onClickPlayPauseButton()
        }

        binding.btnNext.setOnClickListener {
            viewModel.onClickNextButton()
        }

        binding.btnPrev.setOnClickListener {
            viewModel.onClickPrevButton()
        }
    }

    private fun setSong(song: Song) {
        binding.tvSongTitle.text = song.name
        binding.tvAuthor.text = song.author
    }

    private fun setPlaying(isPLaying: Boolean = false) {
        if (isPLaying) {
            binding.btnPlayPause.setImageResource(R.drawable.ic_pause_24)
        } else {
            binding.btnPlayPause.setImageResource(R.drawable.ic_play_24)
        }
    }

    private fun initPlayer(song: Song) {
        Intent(applicationContext, PlayerService::class.java).also {
            it.action = PlayerService.Action.INIT.toString()
            it.putExtra("song_resource_id", song.resId)
            startService(it)
        }
    }

    private fun sendPlayPauseCommandToPlayer(isPLaying: Boolean) {
        if (isPLaying) {
            Intent(applicationContext, PlayerService::class.java).also {
                it.action = PlayerService.Action.START.toString()
                startService(it)
            }
        } else {
            Intent(applicationContext, PlayerService::class.java).also {
                it.action = PlayerService.Action.PAUSE.toString()
                startService(it)
            }
        }
    }

    private fun sendSetNewSongCommandToPlayer(song: Song) {
        Intent(applicationContext, PlayerService::class.java).also {
            it.action = PlayerService.Action.SET.toString()
            it.putExtra("song_resource_id", song.resId)
            startService(it)
        }
    }
}