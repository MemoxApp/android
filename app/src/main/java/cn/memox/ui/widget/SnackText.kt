package cn.memox.ui.widget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cn.memox.utils.ifElse

@Composable
fun SnackText(modifier: Modifier = Modifier, color: Color, backgroundColor: Color, text: String) {
    val bgColor = animateColorAsState(
        targetValue = text.isBlank().ifElse(backgroundColor.copy(alpha = 0f), backgroundColor)
    )
    val textColor = animateColorAsState(targetValue = color)
    Column(
        modifier
            .animateContentSize()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor.value),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            text.isNotBlank(),
            enter = expandVertically(),
            exit = shrinkVertically(),
        ) {
            Text(
                text = text, color = textColor.value, modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            )
        }
    }
}