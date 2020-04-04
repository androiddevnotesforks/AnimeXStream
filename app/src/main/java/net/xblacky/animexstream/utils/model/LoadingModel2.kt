package net.xblacky.animexstream.utils.model

import net.xblacky.animexstream.R
import net.xblacky.animexstream.utils.CommonViewModel2
import net.xblacky.animexstream.utils.constants.C

data class LoadingModel2(
    val loading: CommonViewModel2.Loading,
    val isListEmpty: Boolean,
    val errorCode: Int,
    val errorMsg: Int
)