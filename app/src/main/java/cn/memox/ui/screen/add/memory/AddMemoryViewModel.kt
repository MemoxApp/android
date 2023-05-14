package cn.memox.ui.screen.add.memory

import AddMemoryMutation
import androidx.lifecycle.viewModelScope
import cn.memox.base.BaseViewModel
import cn.memox.base.MutationState
import cn.memox.utils.apollo
import cn.memox.utils.defaultErrorHandler
import cn.memox.utils.onSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import type.AddMemoryInput

class AddMemoryViewModel : BaseViewModel<AddMemoryState, AddMemoryAction>(AddMemoryState()) {
    override fun reduce(action: AddMemoryAction): AddMemoryState {
        return when (action) {
            is AddMemoryAction.UpdateTitle -> state.copy(title = action.title)
            is AddMemoryAction.UpdateContent -> state.copy(content = action.content)
            is AddMemoryAction.Submit -> state.copy(state = MutationState.Requesting).then {
                viewModelScope.launch {
                    delay(500)
                    apollo().mutation(
                        AddMemoryMutation(
                            AddMemoryInput(
                                title = state.title,
                                content = state.content
                            )
                        )
                    )
                        .toFlow()
                        .onSuccess { data ->
                            toast("创建成功")
                            action.onSuccess(data.addMemory)
                            state.copy(state = MutationState.Success).update()
                        }
                        .defaultErrorHandler {
                            state.copy(state = MutationState.Error(it)).update()
                        }
                        .onCompletion {
                            viewModelScope.launch {
                                delay(2000)
                                state.copy(state = MutationState.Idle).update()
                            }
                        }
                        .launchIn(viewModelScope).start()
                }
            }
        }
    }
}