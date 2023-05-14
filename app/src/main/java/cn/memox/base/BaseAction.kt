package cn.memox.base

interface BaseAction {
    data class ShowToast(val msg: String) : BaseAction
}

data class BaseState(
    val toast: String = ""
)