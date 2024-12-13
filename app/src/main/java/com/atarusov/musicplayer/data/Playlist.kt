package com.atarusov.musicplayer.data

import com.atarusov.musicplayer.R

object Playlist {

    val playlist = listOf(
        Song(
            id = 0,
            name = "Happy new year",
            author = "ABBA",
            resId = R.raw.abba_happy_new_year
        ),
        Song(
            id = 1,
            name = "Let it snow! Let it snow! Let it snow!",
            author = "Dean Martin",
            resId = R.raw.dean_martin_let_it_snow
        ),
        Song(
            id = 2,
            name = "Bohemian rhapsody",
            author = "Queen",
            resId = R.raw.queen_bohemian_rhapsody
        ),
    )
}