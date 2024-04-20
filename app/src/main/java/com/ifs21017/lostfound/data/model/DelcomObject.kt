package com.ifs21017.lostfound.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
@Parcelize
data class DelcomObject(
    val id: Int,
    val title: String,
    val description: String,
    var isCompleted: Boolean,
    val status: String,
    val cover: String?,
) : Parcelable