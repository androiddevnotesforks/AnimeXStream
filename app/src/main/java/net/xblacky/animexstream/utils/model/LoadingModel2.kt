package net.vapormusic.animexstream.utils.model

import net.vapormusic.animexstream.R
import net.vapormusic.animexstream.utils.CommonViewModel2
import net.vapormusic.animexstream.utils.constants.C

data class LoadingModel2(
    val loading: CommonViewModel2.Loading,
    val isListEmpty: Boolean,
    val errorCode: Int,
    val errorMsg: Int
)