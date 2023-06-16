package cn.memox.ui.screen.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cn.memox.ui.screen.home.main.MainScreen
import cn.memox.ui.screen.home.me.MeScreen
import cn.memox.ui.theme.colors

object MainContainer {
    @Composable
    fun View() {
        Box(modifier = Modifier
            .background(colors.background)
            .fillMaxSize()) {
            Pager()
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Pager() {
        HorizontalPager(pageCount = 2) {
            when (it) {
                0 -> MainScreen.View()
                else -> MeScreen.View()
            }
        }
    }
}