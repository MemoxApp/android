package cn.memox.ui.screen.account

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults.textButtonColors
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter.Companion.tint
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.memox.AppRoute
import cn.memox.R
import cn.memox.nav
import cn.memox.replace
import cn.memox.ui.effect.verticalShortScroll
import cn.memox.ui.theme.comfortaa
import cn.memox.ui.theme.themeColor
import cn.memox.ui.widget.AppDialog
import cn.memox.ui.widget.EditText
import cn.memox.ui.widget.Loading
import cn.memox.utils.ifElse
import cn.memox.utils.keep
import cn.memox.utils.string

object AccountScreen {
    @Composable
    fun Dia(
        vm: AccountViewModel = viewModel(), content: @Composable () -> Unit
    ) {
        val value = keep("")
        val view = LocalView.current
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
        Loading(show = vm.state.showLoadingMsg.isNotBlank(),
            msg = vm.state.showLoadingMsg,
            timeout = 3000,
            onDismiss = { vm act AccountAction.ShowLoading("") }) {
            AppDialog("服务器地址").withView {
                EditText(hint = "Remote Server URL", value = value.value) {
                    value.value = it
                }
            }.positive("Confirm") {
                println("confirm")
                true
            }.negative("Cancel") {
                println("confirm")
                true
            }.Build(vm.state.showSetting,
                DialogProperties(),
                onDismissRequest = { vm act AccountAction.ToggleSetting }) {
                content()
            }
        }
    }

    @Composable
    fun View() {
        Dia {
            Content()
        }
    }

    @Composable
    private fun Content(vm: AccountViewModel = viewModel()) {
        Box {
            Image(
                painter = painterResource(id = R.drawable.bg_pink),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Welcome()
            IconButton(modifier = Modifier
                .systemBarsPadding()
                .padding(16.dp)
                .size(32.dp)
                .align(Alignment.TopEnd),
                onClick = { vm act AccountAction.ToggleSetting }) {
                Image(
                    painter = painterResource(id = R.drawable.settings_fill),
                    contentDescription = null,
                    modifier = Modifier,
                    contentScale = ContentScale.Crop,
                    colorFilter = tint(Color.White)
                )
            }
        }
    }

    @OptIn(ExperimentalTextApi::class, ExperimentalFoundationApi::class)
    @Composable
    private fun Welcome(
        vm: AccountViewModel = viewModel()
    ) {
        val nav = nav()
        val shader = ShaderBrush(ImageShader(ImageBitmap.imageResource(id = R.drawable.shader)))
        val scrollState = rememberScrollState()
        Column(
            Modifier
                .fillMaxSize()
                .verticalShortScroll(scrollState)
                .systemBarsPadding()
                .imePadding(), verticalArrangement = Arrangement.Bottom
        ) {
            val annotation = buildAnnotatedString {
                pushStyle(
                    SpanStyle(
                        brush = shader
                    )
                )
                append("Manage \nYour \n")
                append("Memories.")
                pop()
            }
            Text(
                text = annotation,
                modifier = Modifier.padding(horizontal = 32.dp),
                fontSize = 48.sp,
                lineHeight = 60.sp,
                fontWeight = FontWeight.W900,
                color = themeColor,
                fontFamily = comfortaa()
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .animateContentSize(tween()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (vm.state.state) {
                    AccountState.State.Idle -> Spacer(
                        modifier = Modifier
                            .height(64.dp)
                            .fillMaxWidth()
                    )

                    else -> {
                        Input()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
            TextButton(modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = buttonColor(),
                onClick = {
                    when (vm.state.state) {
                        AccountState.State.Idle -> vm act AccountAction.ToggleState(
                            AccountState.State.Login
                        )

                        AccountState.State.Login -> vm act AccountAction.Login {
                            nav.replace(AppRoute.index)
                        }

                        AccountState.State.Register -> vm act AccountAction.Register
                        AccountState.State.Reset -> vm act AccountAction.Reset
                    }
                }) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 400.dp)
                        .fillMaxWidth(0.4f)
                        .padding(vertical = 8.dp), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = vm.state.isRegister.ifElse(
                            "注册", vm.state.isReset.ifElse(
                                "重置密码", "登录"
                            )
                        ), fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = buttonColorSecondary(),
                onClick = {
                    vm act AccountAction.ToggleState(
                        when (vm.state.state) {
                            AccountState.State.Idle -> AccountState.State.Register
                            AccountState.State.Reset -> AccountState.State.Login
                            else -> AccountState.State.Idle
                        }
                    )
                }) {
                Box(
                    modifier = Modifier
                        .widthIn(max = 400.dp)
                        .fillMaxWidth(0.4f)
                        .padding(vertical = 8.dp), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (vm.state.state != AccountState.State.Idle) "返回" else "注册",
                        fontSize = 16.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(64.dp))
        }
    }

    @Composable
    private fun Input(
        vm: AccountViewModel = viewModel()
    ) {
        Column(
            Modifier
                .widthIn(max = 400.dp)
                .padding(horizontal = 32.dp),
        ) {
            AnimatedContent(targetState = vm.state.state) { state ->
                if (state == AccountState.State.Register) EditText(
                    modifier = Modifier.padding(vertical = 4.dp),
                    hint = "昵称", value = vm.state.nickname,
                    unselectedIcon = R.drawable.user_fill,
                    selectedColor = Color.White,
                    unselectedColor = Color.White,
                    background = Color.White.copy(alpha = 0.2f),
                    onChange = {
                        vm act AccountAction.UpdateNickname(it)
                    },
                )
            }
            EditText(
                modifier = Modifier.padding(vertical = 4.dp),
                hint = "邮箱", value = vm.state.email,
                unselectedIcon = R.drawable.at_fill,
                selectedColor = Color.White,
                unselectedColor = Color.White,
                background = Color.White.copy(alpha = 0.2f),
                onChange = {
                    vm act AccountAction.UpdateEmail(it)
                },
            )
            AnimatedContent(targetState = vm.state.state) { state ->
                if (state == AccountState.State.Register || state == AccountState.State.Reset) EditText(
                    modifier = Modifier.padding(vertical = 4.dp),
                    hint = "邮箱验证码", value = vm.state.verifyCode,
                    unselectedIcon = R.drawable.hashtag,
                    unselectedColor = Color.White,
                    selectedColor = Color.White,
                    background = Color.White.copy(alpha = 0.2f),
                    sideContent = {
                        Text(text = vm.state.sendMailButtonText()) {
                            vm act AccountAction.SendVerifyCode
                        }
                    },
                    onChange = {
                        vm act AccountAction.UpdateVerifyCode(it)
                    },
                )
            }
            EditText(
                modifier = Modifier.padding(vertical = 4.dp),
                hint = "密码", value = vm.state.password,
                isMask = !vm.state.showPassword,
                unselectedIcon = R.drawable.key_fill,
                unselectedColor = Color.White,
                selectedColor = Color.White,
                background = Color.White.copy(alpha = 0.2f),
                sideContent = {
                    Icon(id = vm.state.showPassword.ifElse(
                        R.drawable.eye_off_fill, R.drawable.eye_fill
                    ), contentDescription = "", onClick = {
                        vm act AccountAction.TogglePasswordVisibility
                    })
                },
                onChange = {
                    vm act AccountAction.UpdatePassword(it)
                },
            )
            AnimatedContent(targetState = vm.state.state) { state ->
                if (state == AccountState.State.Login)
                    Text(modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            vm act AccountAction.ToggleState(AccountState.State.Reset)
                        }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                        text = string(R.string.forgot_password),
                        color = Color.White,
                        fontSize = 14.sp)
            }
        }
    }
}

@Preview
@Composable
fun AccountScreenPreview() {
    AccountScreen.View()
}

@Composable
private fun buttonColor() = textButtonColors(
    containerColor = themeColor.copy(alpha = 0.8f),
    contentColor = Color.White,
)

@Composable
private fun buttonColorSecondary() = textButtonColors(
    contentColor = themeColor,
    containerColor = Color.White.copy(alpha = 0.8f),
)