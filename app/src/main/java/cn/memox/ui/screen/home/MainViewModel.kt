package cn.memox.ui.screen.home

import MemoriesQuery
import android.util.Log
import androidx.lifecycle.viewModelScope
import cn.memox.base.BaseViewModel
import cn.memox.base.FourState
import cn.memox.utils.PageSize
import cn.memox.utils.apollo
import cn.memox.utils.defaultErrorHandler
import cn.memox.utils.onSuccess
import cn.memox.utils.page
import cn.memox.utils.wrap
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import type.ListInput


class MainViewModel : BaseViewModel<MainState, MainAction>(MainState()) {
    init {
        MainAction.RefreshMemories.send()
    }

    override fun reduce(action: MainAction): MainState {
        return when (action) {
            is MainAction.Memories -> state.copy(state = MainState.State.Memories)
            is MainAction.Chat -> state.copy(state = MainState.State.Chat)
            is MainAction.Discover -> state.copy(state = MainState.State.Discover)
            is MainAction.Me -> state.copy(state = MainState.State.Me)

            is MainAction.RefreshMemories -> {
                state.copy(memoriesState = FourState.Loading).then {
                    viewModelScope.launch {
                        apollo().query(
                            MemoriesQuery(
                                ListInput(
                                    archived = false.wrap,
                                    byCreate = false.wrap,
                                    desc = true.wrap,
                                    page = 0,
                                    size = PageSize
                                )
                            )
                        )
                            .toFlow()
                            .onSuccess {
                                viewModelScope.launch {
                                    delay(500)
                                    state = state.copy(
                                        memories = it.allMemories.filterNotNull()
                                            .ifEmpty { state.memories }
                                            .onEach { memory ->
                                                Log.i("Memory", "reduce: $memory")
                                            },
                                        memoriesState = FourState.Idle
                                    )
                                }
                            }
                            .defaultErrorHandler { err ->
                                state =
                                    state.copy(memoriesState = FourState.Error(err))
                            }
                            .launchIn(viewModelScope)
                            .start()
                    }
                }
            }

            is MainAction.LoadMoreMemories -> {
                state.copy(memoriesState = FourState.OnMore).then {
                    viewModelScope.launch {
                        apollo().query(
                            MemoriesQuery(
                                ListInput(
                                    archived = false.wrap,
                                    byCreate = false.wrap,
                                    desc = true.wrap,
                                    page = state.memories.size.page,
                                    size = PageSize
                                )
                            )
                        )
                            .toFlow()
                            .onSuccess {
                                state = state.copy(
                                    memories = state.memories + it.allMemories.filterNotNull(),
                                    memoriesState = FourState.Idle
                                )
                            }
                            .defaultErrorHandler { err ->
                                state =
                                    state.copy(memoriesState = FourState.Error(err))
                            }
                            .launchIn(viewModelScope)
                            .start()
                    }
                }
            }

            is MainAction.LikeMemory -> {
                state.copy(memoriesState = FourState.Loading)
            }
        }
    }

    override fun onChange(old: MainState, new: MainState) {
        super.onChange(old, new)
        if (old.memoriesState != new.memoriesState && new.memoriesState is FourState.Error && new.memories.isNotEmpty()) {
            // 在已有内容但是加载失败时，不以文本形式显示错误信息，而是以 toast 形式显示
            toast(new.memoriesState.msg)
        }
    }
}