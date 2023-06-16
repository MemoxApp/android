package cn.memox.ui.screen.feed.reply

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.memox.R
import cn.memox.base.MutationState
import cn.memox.ui.screen.feed.FeedAction
import cn.memox.ui.screen.feed.FeedViewModel
import cn.memox.ui.theme.colors
import cn.memox.ui.theme.themeColor
import cn.memox.ui.widget.EditText
import cn.memox.utils.string
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object ReplyScreen {
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun ReplyView(vm: FeedViewModel = viewModel(), content: @Composable () -> Unit) {
        ModalBottomSheetLayout(
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding(),
            sheetContent = {
                ReplyCard(vm)
            },
            sheetState = vm.sheetState,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
        ) {
            content()
        }
    }

    /**
     * 回复弹窗
     */
    @OptIn(ExperimentalMaterialApi::class, ExperimentalComposeUiApi::class)
    @Composable
    private fun ReplyCard(vm: FeedViewModel = viewModel()) {
        val scope = rememberCoroutineScope()
        val ime = LocalSoftwareKeyboardController.current
        Column(
            Modifier
                .background(colors.card)
                .fillMaxWidth()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = string(R.string.comment),
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                val promptText = when (val replyState = vm.state.commentState) {
                    is MutationState.Error -> replyState.msg
                    else -> null
                }
                Text(
                    text = promptText ?: "",
                    color = colors.textPrimary,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .animateContentSize(tween()),
                    fontSize = 12.sp
                )
                val uploadState = vm.state.commentState
                if (uploadState is MutationState.Requesting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .size(24.dp),
                        color = themeColor,
                        strokeWidth = 3.dp,
                    )
                } else {
                    Text(
                        text = string(R.string.publish),
                        color = colors.primary,
                        fontSize = 18.sp,
                        modifier = Modifier
                            .clickable {
                                vm act FeedAction.PublishReply {
                                    ime?.hide()
                                    scope.launch(Dispatchers.Main) {
                                        delay(500)
                                        vm.sheetState.hide()
                                    }
                                }
                            }
                            .padding(16.dp)
                    )
                }
            }
            EditText(
                value = vm.state.replyContent ?: "",
                onChange = {
                    vm act FeedAction.UpdateReply(it)
                },
                background = colors.background,
                singleLine = false,
                hint = string(R.string.content),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.8f, false)
                    .defaultMinSize(minHeight = 200.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            )
            /*
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(5),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .reorderable(state)
                                    .detectReorderAfterLongPress(state)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .weight(1f, false),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                state = state.gridState,
                            ) {
                                itemsIndexed(
                                    images,
                                    key = { _, image -> image },
                                ) { index, image ->
                                    ReorderableItem(
                                        state,
                                        key = image
                                    ) { isDragging ->
                                        val elevation = animateDpAsState(if (isDragging) 8.dp else 0.dp)
                                        Box(
                                            Modifier
                                                .shadow(elevation.value)
                                        ) {
                                            LazyImage(
                                                src = image,
                                                modifier = Modifier
                                                    .aspectRatio(1f)
                                                    .fillMaxWidth(),
                                                contentDescription = string(R.string.image),
                                                scale = ContentScale.Crop,
                                                onClick = {
                                                    Msg.showImage(index, images)
                                                }
                                            )
                                            EasyImage(
                                                src = RT.Icons.close,
                                                contentDescription = string(R.string.delete_image),
                                                modifier = Modifier
                                                    .background(colors.onCard)
                                                    .size(ImageSize.Small)
                                                    .align(Alignment.TopEnd)
                                                    .clickable {
                                                        vm.send(FeedViewModel.Intent.DeleteImage(pid, image))
                                                    },
                                                tint = colors.textPrimary
                                            )
                                        }
                                    }
                                }
                                item {
                                    ReorderableItem(
                                        state,
                                        key = "add"
                                    ) {
                                        Box(
                                            Modifier
                                                .background(colors.onCard)
                                                .aspectRatio(1f)
                                                .clickable {
                                                    vm.send(FeedViewModel.Intent.AppendImage(pid))
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            EasyImage(
                                                src = RT.Icons.add,
                                                modifier = Modifier
                                                    .size(ImageSize.Small),
                                                contentDescription = string(R.string.image_add),
                                                tint = colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }
            */

            /*                Row(
                                Modifier
                                    .fillMaxWidth()
                            ) {
                                Box(modifier = Modifier
                                    .clip(AppShapes.small)
                                    .clickNoRepeat {
                                        if (hideSelf.value) {
                                            imeShowing.value = false
                                            hideSelf.value = false
                                            imeState.value = false
                                        } else {
                                            imeShowing.value = true
                                            hideSelf.value = true
                                            imeState.value = true
                                        }
                                    }
                                    .padding(horizontal = Gap.Big, vertical = Gap.Big)
                                ) {
                                    EasyImage(
                                        src = RT.Icons.emotion_line, contentDescription = string(R.string.emoji),
                                        modifier = Modifier.size(ImageSize.Mid),
                                        tint = if (!hideSelf.value) colors.primary else colors.textPrimary
                                    )
                                }
                            }*/
        }
    }
}