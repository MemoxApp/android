package cn.memox.ui.screen.feed

import CommentsQuery
import MemoryQuery
import cn.memox.base.FourState
import cn.memox.base.MutationState
import cn.memox.base.TriState
import cn.memox.ui.screen.feed.reply.ReplyState

data class FeedState(
    val id: String = "",
    val commentDesc: Boolean = true,
    val state: TriState = TriState.Idle,
    val commentListState: FourState = FourState.Idle,
    val commentState: MutationState = MutationState.Idle,
    val memory: MemoryQuery.Memory? = null,
    val comments: List<CommentsQuery.AllComment> = emptyList(),
    val replyState: ReplyState = ReplyState(),
    val replyCache: Map<FeedAction.Reply, String> = hashMapOf()
) {
    val replyContent: String? get() = replyCache[replyState.workingId]
}

sealed class FeedAction {
    data class Init(val id: String) : FeedAction()
    object LoadFeed : FeedAction()
    data class LoadComment(val refresh: Boolean) : FeedAction()
    data class Reply(val id: String, val subComment: Boolean) : FeedAction()
    data class UpdateReply(val content: String) : FeedAction()
    data class PublishReply(val onSuccess: () -> Unit) : FeedAction()
}