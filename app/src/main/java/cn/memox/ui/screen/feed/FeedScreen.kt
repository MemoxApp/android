package cn.memox.ui.screen.feed

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.memox.nav
import cn.memox.pop
import cn.memox.ui.theme.colors
import cn.memox.ui.theme.comfortaa
import cn.memox.ui.widget.CacheImage
import cn.memox.utils.pickImages
import cn.memox.utils.removeImages

object FeedScreen {
    @Composable
    fun View(id: String, vm: FeedViewModel = viewModel()) {
        LaunchedEffect(id) {
            vm act FeedAction.Init(id)
        }
        Content()
    }

    @Composable
    private fun Content() {
        Column(
            Modifier
                .background(colors().background)
                .systemBarsPadding()
                .imePadding()
                .fillMaxSize()
        ) {
            MainContent()
            BottomBar()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainContent(vm: FeedViewModel = viewModel()) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val nav = nav()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        Text(
                            vm.state.memory?.title ?: "Loading",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.W900,
                            fontFamily = comfortaa(),
                        )
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = colors().background,
                        scrolledContainerColor = colors().background,
                        titleContentColor = colors().textPrimary
                    ),
                    navigationIcon = {
                        IconButton(onClick = { nav.pop() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                tint = colors().textPrimary,
                                contentDescription = "Localized description"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            content = { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .background(colors().background)
                        .fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 32.dp,
                        end = 32.dp,
                        top = innerPadding.calculateTopPadding(),
                        bottom = innerPadding.calculateBottomPadding()
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    feedContent(vm)
                    commentList(vm)
                }
            }
        )
    }

    private fun LazyListScope.feedContent(vm: FeedViewModel) {
        item {
            Text(
                text = vm.state.memory?.content?.removeImages ?: "Loading",
                color = colors().textPrimary,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
            )
            val images = vm.state.memory?.content?.pickImages ?: emptyList()
            images.forEachIndexed { _, it ->
                CacheImage(
                    src = it.second,
                    contentDescription = it.first,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                        .padding(2.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.FillWidth,
//                            placeholder = painterResource(id = R.drawable.placeholder)
                )
            }
        }
    }

    private fun LazyListScope.commentList(vm: FeedViewModel) {
        items(vm.state.comments) { item ->
            Text(
                text = item.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }

    @Composable
    private fun BottomBar(vm: FeedViewModel = viewModel()) {
    }
}