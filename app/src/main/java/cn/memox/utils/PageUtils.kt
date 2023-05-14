package cn.memox.utils

import kotlin.math.ceil

const val PageSize = 15
val Int.page
    get() = ceil(this.toFloat() / PageSize).toInt()