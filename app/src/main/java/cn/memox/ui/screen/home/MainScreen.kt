package cn.memox.ui.screen.home

import MemoriesQuery
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.memox.AppRoute
import cn.memox.R
import cn.memox.base.FourState
import cn.memox.nav
import cn.memox.push
import cn.memox.ui.theme.colors
import cn.memox.ui.theme.comfortaa
import cn.memox.ui.theme.themeColor
import cn.memox.ui.widget.AppDialog
import cn.memox.ui.widget.CacheImage
import cn.memox.ui.widget.StaggeredVerticalGrid
import cn.memox.ui.widget.Style
import cn.memox.utils.formatHumanized
import cn.memox.utils.pickImages
import cn.memox.utils.removeImages
import cn.memox.utils.string

object MainScreen {
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun View(vm: MainViewModel = viewModel()) {
        val refreshState = rememberPullRefreshState(
            refreshing = vm.state.memoriesState == FourState.Loading,
            onRefresh = { vm act MainAction.RefreshMemories })
        Column(
            Modifier
                .pullRefresh(refreshState)
                .background(colors.background)
                .systemBarsPadding()
                .imePadding()
                .fillMaxSize()
        ) {
            Scaffold(
                Modifier.fillMaxSize(),
                topBar = { TopBar() },
                floatingActionButton = { AddFab() },
                backgroundColor = colors.background,
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Content()
                    PullRefreshIndicator(
                        refreshing = vm.state.memoriesState == FourState.Loading,
                        state = refreshState,
                        contentColor = themeColor
                    )
                }
            }
        }
    }

    @Composable
    private fun AddFab() {
        val nav = nav()
        FloatingActionButton(
            onClick = { nav.push(AppRoute.addMemory) },
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                tint = Color.White
            )
        }
    }

    @Composable
    private fun TopBar() {
        // 白色背景带阴影的标题栏
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Memox",
                fontFamily = comfortaa(),
                fontWeight = FontWeight.W900,
                color = colors.textPrimary,
                fontSize = 28.sp
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    @Composable
    private fun Content(
        vm: MainViewModel = viewModel()
    ) {
        val deleteMemory = vm.state.showDelete
        AppDialog(
            string(R.string.archive_memory),
            string(R.string.archive_memory_desc)
        ).negative { true }
            .positive {
                vm act MainAction.ArchiveMemory
                true
            }
            .Build(
                show = deleteMemory != null,
                onDismissRequest = { vm act MainAction.DismissArchiveMemory }) {
                LazyColumn(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(vm.state.memories) { memory ->
                        Item(memory)
                    }
                }
            }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun Item(
        memory: MemoriesQuery.AllMemory,
        vm: MainViewModel = viewModel()
    ) {
        val nav = nav()
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .combinedClickable(
                    onLongClick = {
                        vm act MainAction.ShowArchiveMemoryDialog(memory)
                    }
                ) {
                    nav.push(AppRoute.memory(memory.id))
                }
                .fillMaxWidth()
                .background(colors.card)
                .padding(16.dp)
        ) {
            if (memory.title.isNotBlank())
                Text(
                    text = memory.title,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            Text(
                text = memory.update_time.formatHumanized,
                color = colors.textSecondary,
                fontSize = 12.sp
            )
            Text(
                text = memory.content.removeImages,
                color = colors.textPrimary,
                fontSize = 14.sp,
                maxLines = 10,
                overflow = TextOverflow.Ellipsis
            )
            val images = memory.content.pickImages

            if (images.isNotEmpty()) {
                val row = when (images.size) {
                    1 -> 1
                    2, 4 -> 2
                    else -> 3
                }
                val style = when (row) {
                    1 -> Style.Large
                    2 -> Style.Middle
                    else -> Style.Mini
                }
                StaggeredVerticalGrid(
                    maxRows = row,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    items(images) {
                        CacheImage(
                            src = it.second,
                            contentDescription = it.first,
                            style = style,
                            modifier = Modifier
                                .animateContentSize()
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun Preview() {
    MainScreen.Item(
        MemoriesQuery.AllMemory(
            id = "",
            user = MemoriesQuery.User(
                id = "",
                username = "Xeu",
                avatar = "",
            ),
            title = "Hello",
            content = "World",
            update_time = "100000010",
            create_time = "2021-10-01",
            hashtags = listOf(
                MemoriesQuery.Hashtag(
                    id = "",
                    name = "Hello",
                )
            ),
            archived = false
        )
    )
}
