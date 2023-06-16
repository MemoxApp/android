package cn.memox.ui.screen.home.me

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import cn.memox.ui.screen.home.MainViewModel

object MeScreen {
    @Composable
    fun View(vm: MainViewModel = viewModel()) {
        Box(modifier = Modifier.fillMaxSize()) {

        }
    }
}