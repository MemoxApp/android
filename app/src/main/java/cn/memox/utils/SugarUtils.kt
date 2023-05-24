package cn.memox.utils

import androidx.compose.runtime.MutableState
import com.apollographql.apollo3.api.Optional
import com.blankj.utilcode.util.EncryptUtils

fun <T> Boolean.ifElse(ifTrue: () -> T, ifFalse: () -> T): T {
    return if (this) {
        ifTrue()
    } else {
        ifFalse()
    }
}

fun <T> MutableState<Boolean>.ifElse(ifTrue: () -> T, ifFalse: () -> T): T {
    return if (this.value) {
        ifTrue()
    } else {
        ifFalse()
    }
}

fun <T> Boolean.ifElse(ifTrue: T, ifFalse: T): T {
    return if (this) {
        ifTrue
    } else {
        ifFalse
    }
}

fun <T> MutableState<Boolean>.ifElse(ifTrue: T, ifFalse: T): T {
    return if (this.value) {
        ifTrue
    } else {
        ifFalse
    }
}

fun MutableState<Boolean>.toggle() {
    this.value = !this.value
}

inline fun (() -> Unit).ifError(block: (Exception) -> Unit) {
    try {
        this()
    } catch (e: Exception) {
        block(e)
    }
}

val <T> T?.wrap get() = Optional.presentIfNotNull(this)
val None = Optional.Absent

fun md5(raw: String): String {
    return EncryptUtils.encryptMD5ToString(raw)
}

fun md5(raw: ByteArray): String {
    return EncryptUtils.encryptMD5ToString(raw)
}