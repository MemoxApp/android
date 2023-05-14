package cn.memox.utils

import cn.memox.App

fun string(id: Int) = resource.getString(id)

fun string(id: Int, vararg formatArgs: Any) = resource.getString(id, *formatArgs)

val resource = App.CONTEXT.resources