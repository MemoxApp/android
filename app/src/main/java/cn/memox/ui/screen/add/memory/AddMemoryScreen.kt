package cn.memox.ui.screen.add.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.memox.AppRoute
import cn.memox.R
import cn.memox.base.MutationState
import cn.memox.nav
import cn.memox.pop
import cn.memox.push
import cn.memox.ui.theme.colors
import cn.memox.ui.theme.comfortaa
import cn.memox.ui.theme.themeColor
import cn.memox.ui.widget.EditText
import cn.memox.ui.widget.SnackText
import cn.memox.utils.keep
import cn.memox.utils.string

object AddMemoryScreen {
    @Composable
    fun View() {
        Content()
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun Content(vm: AddMemoryViewModel = viewModel()) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val nav = nav()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            backgroundColor = colors().background,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            string(R.string.add_memory),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.W900,
                            fontFamily = comfortaa(),
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { nav.pop() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                tint = colors().textPrimary,
                                contentDescription = string(R.string.back)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (vm.state.state is MutationState.Idle) {
                                vm act AddMemoryAction.Submit {
                                    nav.pop()
                                    nav.push(AppRoute.memory(it))
                                }
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Send,
                                tint = colors().textPrimary,
                                contentDescription = string(R.string.create)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = colors().background,
                        scrolledContainerColor = colors().background,
                        titleContentColor = colors().textPrimary
                    ),
                    scrollBehavior = scrollBehavior
                )
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .background(colors().background)
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var color by keep(v = colors().textPrimary)
                    var backgroundColor by keep(v = Color.LightGray)
                    var text by keep(v = "")
                    when (val stats = vm.state.state) {
                        is MutationState.Error -> {
                            color = Color.White
                            backgroundColor = colors().error
                            text = stats.msg
                        }

                        is MutationState.Requesting -> {
                            color = Color.White
                            backgroundColor = colors().primary
                            text = stringResource(R.string.requesting)
                        }

                        is MutationState.Success -> {
                            color = Color.White
                            backgroundColor = colors().success
                            text = stringResource(R.string.create_success)
                        }

                        is MutationState.Idle -> {
                            text = ""
                        }
                    }
                    SnackText(
                        modifier = Modifier.fillMaxWidth(),
                        color = color,
                        backgroundColor = backgroundColor,
                        text = text
                    )
                    Input()
                }
            }
        )
    }

    @Composable
    private fun Input(vm: AddMemoryViewModel = viewModel()) {

        EditText(
            modifier = Modifier.padding(vertical = 4.dp),
            hint = stringResource(R.string.title), value = vm.state.title,
//            unselectedIcon = R.drawable.at_fill,
            selectedColor = themeColor,
//            unselectedColor = colors().card,
            background = colors().card,
            onChange = {
                vm act AddMemoryAction.UpdateTitle(it)
            },
        )
        EditText(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(top = 4.dp, bottom = 16.dp),
            hint = stringResource(R.string.content), value = vm.state.content,
//            unselectedIcon = R.drawable.at_fill,
            selectedColor = themeColor,
            singleLine = false,
//            unselectedColor = colors().card,
            background = colors().card,
            onChange = {
                vm act AddMemoryAction.UpdateContent(it)
            },
        )
    }
}

