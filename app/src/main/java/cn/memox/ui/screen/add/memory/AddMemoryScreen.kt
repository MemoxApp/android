package cn.memox.ui.screen.add.memory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.CircularProgressIndicator
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.memox.AppRoute
import cn.memox.LocalPicker
import cn.memox.MainActivity
import cn.memox.R
import cn.memox.base.MutationState
import cn.memox.nav
import cn.memox.pop
import cn.memox.push
import cn.memox.ui.effect.verticalShortScroll
import cn.memox.ui.theme.colors
import cn.memox.ui.theme.comfortaa
import cn.memox.ui.theme.themeColor
import cn.memox.ui.widget.EditText
import cn.memox.ui.widget.RichText
import cn.memox.ui.widget.SnackText
import cn.memox.utils.ifElse
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
            backgroundColor = colors.background,
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
                                tint = colors.textPrimary,
                                contentDescription = string(R.string.back)
                            )
                        }
                    },
                    actions = {
                        val promptText = when (val uploadState = vm.state.uploadState) {
                            is UploadImageState.Success -> string(R.string.upload_success)
                            is UploadImageState.Uploading -> string(
                                R.string.uploading,
                                vm.state.uploadCount.first,
                                vm.state.uploadCount.second,
                            )

                            is UploadImageState.Error -> uploadState.error
                            else -> null
                        }
                        if (promptText != null)
                            Text(
                                text = promptText,
                                color = colors.textPrimary,
                                modifier = Modifier.padding(end = 4.dp),
                                fontSize = 12.sp
                            )
                        IconButton(onClick = {
                            if (vm.state.state is MutationState.Idle && vm.state.uploadState !is UploadImageState.Uploading) {
                                vm act AddMemoryAction.Submit {
                                    nav.pop()
                                    nav.push(AppRoute.memory(it))
                                }
                            }
                        }) {
                            val uploadState = vm.state.uploadState
                            if (uploadState is UploadImageState.Uploading) {
                                if (uploadState.progress == 0f) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = themeColor,
                                        strokeWidth = 3.dp,
                                    )
                                } else {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = themeColor,
                                        progress = uploadState.progress,
                                        strokeWidth = 3.dp,
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    tint = colors.textPrimary,
                                    contentDescription = string(R.string.create)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = colors.background,
                        scrolledContainerColor = colors.background,
                        titleContentColor = colors.textPrimary
                    ),
                    scrollBehavior = scrollBehavior
                )
            },
            content = { innerPadding ->
                Column(
                    modifier = Modifier
                        .background(colors.background)
                        .fillMaxSize()
                        .imePadding()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var color by keep(v = colors.textPrimary)
                    var backgroundColor by keep(v = Color.LightGray)
                    var text by keep(v = "")
                    when (val stats = vm.state.state) {
                        is MutationState.Error -> {
                            color = Color.White
                            backgroundColor = colors.error
                            text = stats.msg
                        }

                        is MutationState.Requesting -> {
                            color = Color.White
                            backgroundColor = colors.primary
                            text = stringResource(R.string.requesting)
                        }

                        is MutationState.Success -> {
                            color = Color.White
                            backgroundColor = colors.success
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
    private fun ColumnScope.Input(vm: AddMemoryViewModel = viewModel()) {
        EditText(
            modifier = Modifier.padding(vertical = 4.dp),
            hint = stringResource(R.string.title), value = vm.state.title,
//            unselectedIcon = R.drawable.at_fill,
            selectedColor = themeColor,
//            unselectedColor = colors.card,
            background = colors.card,
            onChange = {
                vm act AddMemoryAction.UpdateTitle(it)
            },
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (vm.state.preview) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(top = 4.dp)
                        .verticalShortScroll(rememberScrollState())
                ) {
                    RichText.RenderText(vm.state.content)
                }
            } else {
                EditText(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 4.dp),
                    hint = stringResource(R.string.content), value = vm.state.content,
//            unselectedIcon = R.drawable.at_fill,
                    selectedColor = themeColor,
                    singleLine = false,
//            unselectedColor = colors.card,
                    background = colors.card,
                    onChange = {
                        vm act AddMemoryAction.UpdateContent(it)
                    },
                )
            }
        }
        val picker = LocalPicker.current
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            IconButton(onClick = {
                picker.launch(MainActivity.ChooseFiles {
                    vm act AddMemoryAction.UploadImage(it)
                })
            }) {
                Icon(
                    painter = painterResource(id = R.drawable.landscape_fill),
                    tint = colors.textPrimary,
                    contentDescription = string(R.string.create)
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = {
                vm act AddMemoryAction.Preview
            }) {
                Icon(
                    painter = painterResource(
                        id = vm.state.preview.ifElse(
                            R.drawable.eye_off_fill,
                            R.drawable.eye_fill
                        )
                    ),
                    tint = colors.textPrimary,
                    contentDescription = string(R.string.create)
                )
            }
        }
    }
}

