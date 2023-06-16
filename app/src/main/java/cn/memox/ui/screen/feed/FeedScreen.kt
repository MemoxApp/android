package cn.memox.ui.screen.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.memox.R
import cn.memox.nav
import cn.memox.pop
import cn.memox.ui.effect.verticalShortScroll
import cn.memox.ui.screen.feed.reply.ReplyScreen
import cn.memox.ui.theme.Gap
import cn.memox.ui.theme.colors
import cn.memox.ui.theme.comfortaa
import cn.memox.ui.widget.LoadingBar
import cn.memox.ui.widget.RichText
import cn.memox.utils.keep
import cn.memox.utils.string
import kotlinx.coroutines.launch

object FeedScreen {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    fun View(id: String, vm: FeedViewModel = viewModel()) {
        LaunchedEffect(id) {
            vm act FeedAction.Init(id)
        }
        Box {
            ReplyScreen.ReplyView {
                Content()
            }
        }
    }

    @Composable
    private fun Content(vm: FeedViewModel = viewModel()) {
        Box(
            Modifier
                .background(colors.card)
                .systemBarsPadding()
                .imePadding()
                .fillMaxSize(),
        ) {
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                enter = fadeIn(),
                visible = vm.state.memory != null
            ) {
                MainContent()
            }
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                exit = fadeOut(),
                visible = vm.state.memory == null
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    LoadingBar()
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun MainContent(vm: FeedViewModel = viewModel()) {
        val scrollBehavior = enterAlwaysScrollBehavior(snapAnimationSpec = spring())
        val nav = nav()
        val smallStyle = MaterialTheme.typography.titleLarge
        Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection), topBar = {
            TopAppBar(
                title = {
                    var padding = 16.dp
                    val style = LocalTextStyle.current
                    if (style == smallStyle) {
                        padding = 0.dp
                    }
                    Text(vm.state.memory?.title?.ifBlank { "速记" } ?: "Loading",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.W900,
                        fontFamily = comfortaa(),
                        modifier = Modifier.padding(horizontal = padding))
                }, colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = colors.card,
                    scrolledContainerColor = colors.card,
                    titleContentColor = colors.textPrimary
                ), navigationIcon = {
                    IconButton(onClick = { nav.pop() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            tint = colors.textPrimary,
                            contentDescription = "Localized description"
                        )
                    }
                }, scrollBehavior = scrollBehavior
            )
        }, content = { innerPadding ->
            Column(
                modifier = Modifier
                    .background(colors.background)
                    .fillMaxSize()
                    .verticalShortScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
                    .padding(innerPadding),
            ) {
                FeedContent(vm)
                CommentList(vm)
            }
        }, floatingActionButton = {
            BottomBar()
        })
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.card)
                .padding(horizontal = 32.dp, vertical = Gap.Big)
        ) {
            RichText.RenderText(text)
        }
    }

    @Composable
    private fun CommentList(vm: FeedViewModel) {
        Column(
            Modifier
                .fillMaxWidth()
                .background(color = colors.card)
                .padding(horizontal = Gap.Big)
                .padding(bottom = Gap.Big), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 8.dp),
                text = string(R.string.comments),
                fontSize = 16.sp,
                fontWeight = FontWeight.W700,
                color = colors.primary
            )
            vm.state.comments.forEach { item ->
                CommentCard(comment = item) {}
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 160.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(vertical = 16.dp),
                text = string(R.string.comments_count, vm.state.comments.size),
                fontSize = 12.sp,
                color = colors.textPrimary
            )
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun BottomBar(vm: FeedViewModel = viewModel()) {
        val scope = rememberCoroutineScope()
        FloatingActionButton(
            onClick = {
                scope.launch {
                    vm.sheetState.show()
                    vm act FeedAction.Reply(vm.state.id, false)
                }
            },
            modifier = Modifier
                .padding(16.dp)
                .shadow(
                    12.dp,
                    RoundedCornerShape(50),
                    ambientColor = Color.LightGray,
                    spotColor = Color.LightGray
                ),
            shape = RoundedCornerShape(50),
            containerColor = colors.card,
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
        ) {
            Icon(
                painter = painterResource(id = R.drawable.message),
                contentDescription = "Comment",
                tint = colors.primary
            )
        }
    }
}