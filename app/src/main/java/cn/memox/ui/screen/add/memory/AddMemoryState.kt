package cn.memox.ui.screen.add.memory

import cn.memox.base.MutationState

data class AddMemoryState(
    val title: String = "",
    val content: String = "",
    val state: MutationState = MutationState.Idle
)

sealed class AddMemoryAction {
    data class UpdateTitle(val title: String) : AddMemoryAction()
    data class UpdateContent(val content: String) : AddMemoryAction()
    data class Submit(val onSuccess: (String) -> Unit) : AddMemoryAction()
}