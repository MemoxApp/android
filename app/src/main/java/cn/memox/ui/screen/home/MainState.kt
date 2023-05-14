package cn.memox.ui.screen.home

import MemoriesQuery
import cn.memox.base.FourState


data class MainState(
    val state: State = State.Memories,
    val memories: List<MemoriesQuery.AllMemory> = emptyList(),
    val memoriesState: FourState = FourState.Idle,
) {
    enum class State {
        Memories, Chat, Discover, Me,
    }

}

sealed class MainAction {
    object Memories : MainAction()
    object Chat : MainAction()
    object Discover : MainAction()
    object Me : MainAction()
    object RefreshMemories : MainAction()
    object LoadMoreMemories : MainAction()
    data class LikeMemory(val id: String) : MainAction()
}