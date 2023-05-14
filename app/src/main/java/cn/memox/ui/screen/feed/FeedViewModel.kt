package cn.memox.ui.screen.feed

import AddCommentMutation
import CommentsQuery
import MemoryQuery
import androidx.lifecycle.viewModelScope
import cn.memox.R
import cn.memox.base.BaseViewModel
import cn.memox.base.FourState
import cn.memox.base.MutationState
import cn.memox.base.TriState
import cn.memox.utils.PageSize
import cn.memox.utils.apollo
import cn.memox.utils.defaultErrorHandler
import cn.memox.utils.ifElse
import cn.memox.utils.onSuccess
import cn.memox.utils.page
import cn.memox.utils.string
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import type.AddCommentInput

class FeedViewModel : BaseViewModel<FeedState, FeedAction>(FeedState()) {
    override fun reduce(action: FeedAction): FeedState {
        return when (action) {
            is FeedAction.Init -> state.copy(id = action.id).then {
                FeedAction.LoadFeed.send()
                FeedAction.LoadComment(true).send()
            }

            is FeedAction.LoadFeed -> state.copy(state = TriState.Loading).then {
                viewModelScope.launch {
                    println("ID:${state.id}")
                    apollo().query(MemoryQuery(state.id)).toFlow()
                        .onSuccess { data ->
                            println(data)
                            state = state.copy(
                                state = TriState.Idle,
                                memory = data.memory
                            )
                        }
                        .defaultErrorHandler { err ->
                            toast(err)
                            state = state.copy(state = TriState.Error(err))
                        }
                        .launchIn(viewModelScope).start()
                }
            }

            is FeedAction.LoadComment -> state.copy(commentListState = FourState.Loading).then {
                val page = action.refresh.ifElse(0, state.comments.size.page)
                viewModelScope.launch {
                    apollo().query(
                        CommentsQuery(
                            state.id,
                            page,
                            PageSize,
                            state.commentDesc
                        )
                    ).toFlow()
                        .onSuccess { data ->
                            state = if (data.allComments.isEmpty())
                                state.copy(commentListState = FourState.Error(string(R.string.no_more)))
                            else
                                state.copy(
                                    commentListState = FourState.Idle,
                                    comments = (action.refresh).ifElse(
                                        data.allComments.filterNotNull(),
                                        state.comments + data.allComments.filterNotNull()
                                    ),
                                )
                        }
                        .defaultErrorHandler { err ->
                            state = state.copy(commentListState = FourState.Error(err))
                        }
                        .launchIn(viewModelScope).start()
                }
            }

            is FeedAction.Comment -> state.copy(commentState = MutationState.Requesting).then {
                viewModelScope.launch {
                    apollo().mutation(
                        AddCommentMutation(
                            AddCommentInput(
                                action.content,
                                action.id,
                                action.subComment
                            )
                        )
                    )
                        .toFlow()
                        .onSuccess {
                            state = state.copy(commentState = MutationState.Success)
                            this@FeedViewModel.act { FeedAction.LoadComment(true) }
                        }
                        .defaultErrorHandler { err ->
                            state = state.copy(commentState = MutationState.Error(err))
                        }
                        .onCompletion {
                            viewModelScope.launch {
                                delay(2000)
                                state = state.copy(commentState = MutationState.Idle)
                            }
                        }
                        .launchIn(viewModelScope).start()
                }
            }
        }
    }
}