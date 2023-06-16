package cn.memox.ui.screen.feed.reply

import androidx.compose.ui.text.AnnotatedString
import cn.memox.ui.screen.feed.FeedAction

data class ReplyState(
    val refContent: AnnotatedString = AnnotatedString(""),
    val workingId: FeedAction.Reply? = null,
)