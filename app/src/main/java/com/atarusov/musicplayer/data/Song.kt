package com.atarusov.musicplayer.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    val id: Int,
    val name: String,
    val author: String,
    val resId: Int,
): Parcelable
