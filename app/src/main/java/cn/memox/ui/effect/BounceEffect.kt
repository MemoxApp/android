package cn.memox.ui.effect

import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import kotlinx.coroutines.DelicateCoroutinesApi

class BounceEffect {
}

// 应用弹性动画
@OptIn(DelicateCoroutinesApi::class)
private fun applyBounceAnimation(scrollState: ScrollState, offset: Float) {
    val f = FloatValueHolder(offset)
    val anim = SpringAnimation(f)
    anim.spring = SpringForce().apply {
        dampingRatio = SpringForce.DAMPING_RATIO_HIGH_BOUNCY
        stiffness = SpringForce.STIFFNESS_LOW
        finalPosition = scrollState.value.toFloat()
    }
    anim.start()
}