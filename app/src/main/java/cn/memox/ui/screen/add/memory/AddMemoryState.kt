package cn.memox.ui.screen.add.memory

import cn.memox.base.MutationState

data class AddMemoryState(
    val title: String = "",
    val content: String = "",
    val state: MutationState = MutationState.Idle,
    val preview: Boolean = false,
    val uploadState: UploadImageState = UploadImageState.Idle,
    val uploadCount: Pair<Int, Int> = Pair(0, 0)
)

sealed class AddMemoryAction {
    data class UpdateTitle(val title: String) : AddMemoryAction()
    data class UploadImage(val list: List<String>) : AddMemoryAction()
    data class UpdateContent(val content: String) : AddMemoryAction()
    data class Submit(val onSuccess: (String) -> Unit) : AddMemoryAction()
    object Preview : AddMemoryAction()
}


sealed class UploadImageState {
    object Idle : UploadImageState()
    data class Uploading(val progress: Float) :
        UploadImageState()

    object Success : UploadImageState()
    data class Error(val error: String) : UploadImageState()
}