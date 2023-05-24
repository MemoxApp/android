package cn.memox.ui.screen.add.memory

import AddMemoryMutation
import GetTokenMutation
import androidx.lifecycle.viewModelScope
import cn.memox.base.BaseViewModel
import cn.memox.base.MutationState
import cn.memox.model.UploadBceModel
import cn.memox.utils.AppFile
import cn.memox.utils.FileType.guessSuffix
import cn.memox.utils.apollo
import cn.memox.utils.defaultErrorHandler
import cn.memox.utils.errorHandler
import cn.memox.utils.ifElse
import cn.memox.utils.onSuccess
import cn.memox.utils.onSuccessFlatMapConcat
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import type.AddMemoryInput

class AddMemoryViewModel : BaseViewModel<AddMemoryState, AddMemoryAction>(AddMemoryState()) {
    @OptIn(FlowPreview::class)
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

            is AddMemoryAction.UploadImage -> {
                state.copy(uploadState = UploadImageState.Uploading(0f)).then {
                    viewModelScope.launch {
                        delay(500)
                        action.list.mapNotNull {
                            AppFile.getFile(it)
                        }.also {
                            // 更新上传计数器
                            state.copy(
                                uploadCount = Pair(
                                    state.uploadCount.first,
                                    state.uploadCount.second + it.size
                                )
                            ).update()
                        }
                            .asFlow()
                            .flatMapConcat {
                                println("GetToken:${it.nameWithoutExtension + "." + it.guessSuffix}")
                                return@flatMapConcat apollo().mutation(GetTokenMutation(it.nameWithoutExtension + "." + it.guessSuffix))
                                    .toFlow().map { data ->
                                        Pair(data, it)
                                    }
                            }.errorHandler(::toast)
                            .onSuccessFlatMapConcat { token, file ->
                                val tk = token.getToken
                                if (tk.exist) {
                                    return@onSuccessFlatMapConcat flow {
                                        emit(
                                            Pair(
                                                UploadImageState.Success,
                                                tk.id
                                            )
                                        )
                                    }
                                }
                                println("Start Upload(${tk.id}):${file.nameWithoutExtension + "." + file.guessSuffix},")
                                return@onSuccessFlatMapConcat UploadBceModel.upload(
                                    viewModelScope,
                                    file,
                                    tk.access_key,
                                    tk.secret_access_key,
                                    tk.session_token,
                                    tk.file_name
                                ).map { Pair(it, tk.id) }
                            }
                            .onEach { (it, id) ->
                                when (it) {
                                    is UploadImageState.Uploading -> {
                                        // 是不是按顺序进行的？
                                        state.copy(
                                            uploadState = UploadImageState.Uploading(it.progress)
                                        ).update()
                                    }

                                    is UploadImageState.Success -> {
                                        state.copy(
                                            uploadCount = Pair(
                                                state.uploadCount.first + 1,
                                                state.uploadCount.second
                                            ),
                                            content = state.content + state.content.isNotBlank()
                                                .ifElse("\n", "") + "![]($" + "{${id}})"
                                        )
                                            .update()
                                    }

                                    is UploadImageState.Error -> {
                                        toast(it.error)
                                        state.copy(
                                            uploadCount = Pair(
                                                state.uploadCount.first + 1,
                                                state.uploadCount.second
                                            ), uploadState = UploadImageState.Error(it.error)
                                        )
                                            .update()
                                    }

                                    else -> {}
                                }
                            }
                            .onCompletion {
                                state.copy(uploadState = UploadImageState.Success).update()
                                viewModelScope.launch {
                                    delay(2000)
                                    state.copy(uploadState = UploadImageState.Idle).update()
                                }
                            }.launchIn(viewModelScope).start()
                    }
                }
            }

            AddMemoryAction.Preview -> state.copy(preview = !state.preview)
        }
    }
}