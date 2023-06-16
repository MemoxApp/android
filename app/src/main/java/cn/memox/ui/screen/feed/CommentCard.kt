package cn.memox.ui.screen.feed


import CommentsQuery
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.memox.ui.theme.CardShapes
import cn.memox.ui.theme.Gap
import cn.memox.ui.theme.colors
import cn.memox.ui.widget.CacheImage
import cn.memox.ui.widget.RichText
import cn.memox.ui.widget.StaggeredVerticalGrid
import cn.memox.utils.click
import cn.memox.utils.formatDateTime
import cn.memox.utils.formatHumanized
import cn.memox.utils.pickImages

/**
 * 用户评论
 */
@Composable
fun CommentCard(
    comment: CommentsQuery.AllComment,
    onClick: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = Gap.Mid, vertical = Gap.Big)
    ) {
        Row(
            modifier = Modifier.padding(bottom = Gap.Zero)
        ) {
            Spacer(modifier = Modifier.size(Gap.Mid))
            Column {
//                Text(
//                    text = comment.user.username,
//                    color = colors.primary,
//                    fontSize = 16.sp,
//                )
                val humanized = comment.create_time.formatHumanized
                val detail = comment.create_time.formatDateTime
                val state = remember {
                    mutableStateOf(comment.create_time == comment.update_time)//时间相同显示发布时间
                }
                //内容
                RichText.RenderText(
                    text = comment.content,
                )
                //图片
                val images = remember { comment.content.pickImages }
                if (images.isNotEmpty()) {
                    StaggeredVerticalGrid(
                        maxRows = 4,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        images.forEachIndexed { index, it ->
                            CacheImage(
                                src = it.second,
                                contentDescription = it.first,
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .padding(Gap.Tiny)
                                    .clip(CardShapes.small),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Gap.Mid))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (state.value) humanized else detail,
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.click {
                            state.value = !state.value
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
//                    if (!main) {
//                        val comments = post.comments
//                        ActionItem(
//                            modifier = Modifier,
//                            action = ActionData(
//                                src = RT.Icons.message_3_line,
//                                value = comments.toString(10),
//                                color = colors.textPrimary
//                            ) {
//                                onComment()
//                            }
//                        )
//                    }
                }
            }
        }
    }
}