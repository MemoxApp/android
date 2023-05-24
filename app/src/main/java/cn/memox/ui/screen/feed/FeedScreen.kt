package cn.memox.ui.screen.feed

import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.memox.nav
import cn.memox.pop
import cn.memox.ui.effect.verticalShortScroll
import cn.memox.ui.theme.colors
import cn.memox.ui.theme.comfortaa
import cn.memox.ui.widget.RichText
import cn.memox.utils.keep

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
                .background(colors.background)
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
        val scrollBehavior = enterAlwaysScrollBehavior(snapAnimationSpec = spring())
        val nav = nav()
        val smallStyle = MaterialTheme.typography.titleLarge
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                LargeTopAppBar(
                    title = {
                        var padding = 16.dp
                        val style = LocalTextStyle.current
                        if (style == smallStyle) {
                            padding = 0.dp
                        }
                        Text(
                            vm.state.memory?.title?.ifBlank { "速记" } ?: "Loading",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.W900,
                            fontFamily = comfortaa(),
                            modifier = Modifier.padding(horizontal = padding)
                        )
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = colors.background,
                        scrolledContainerColor = colors.background,
                        titleContentColor = colors.textPrimary
                    ),
                    navigationIcon = {
                        IconButton(onClick = { nav.pop() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                tint = colors.textPrimary,
                                contentDescription = "Localized description"
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .background(colors.background)
                        .fillMaxSize()
                        .verticalShortScroll(rememberScrollState())
                        .padding(bottom = 16.dp)
                        .padding(
                            PaddingValues(
                                start = 32.dp,
                                end = 32.dp,
                                top = innerPadding.calculateTopPadding(),
                                bottom = innerPadding.calculateBottomPadding()
                            )
                        ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FeedContent(vm)
                    CommentList(vm)
                }
            }
        )
    }

    @Composable
    private fun FeedContent(vm: FeedViewModel) {
        var text by keep(v = "")
        LaunchedEffect(vm.state.memory) {
            val content = vm.state.memory?.content
            if (content != null) {
                text = content
            }
        }
        RichText.RenderText(text)
    }

    @Composable
    private fun CommentList(vm: FeedViewModel) {
        vm.state.comments.forEach { item ->
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