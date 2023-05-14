package cn.memox.base

sealed class TriState {
    object Idle : TriState()
    object Loading : TriState()
    data class Error(val msg: String) : TriState()
}

sealed class FourState {
    object Idle : FourState()
    object Loading : FourState()
    object OnMore : FourState()
    data class Error(val msg: String) : FourState()
}

sealed class MutationState {
    object Idle : MutationState()
    object Requesting : MutationState()
    object Success : MutationState()
    data class Error(val msg: String) : MutationState()
}