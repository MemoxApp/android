package cn.memox.ui.effect

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.foundation.clipScrollableContainer
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.overscroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.ScrollAxisRange
import androidx.compose.ui.semantics.horizontalScrollAxisRange
import androidx.compose.ui.semantics.scrollBy
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.verticalScrollAxisRange
import cn.memox.utils.keep
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt

@Composable
fun rememberDampingScrollState(initial: Int = 0): ScrollState {
    return rememberSaveable(saver = ScrollState.Saver) {
        ScrollState(initial = initial)
    }
}

@OptIn(ExperimentalFoundationApi::class)
fun Modifier.verticalDampingScroll(
    scrollState: ScrollState,
    enabled: Boolean = true,
    flingBehavior: FlingBehavior? = null,
    reverseScrolling: Boolean = false
): Modifier = composed {
    val accumulator = keep(v = 0f)
    val tween =
        animateFloatAsState(targetValue = accumulator.value, animationSpec = spring())
    val overscrollEffect = rememberDampingOverscrollEffect { it, source ->
        if ((scrollState.value == 0 || scrollState.value == scrollState.maxValue) && source.toString() == "Drag") {
            accumulator.value += it.y
        } else {
            accumulator.value = 0f
        }
//            Log.i("ScrollView", "${it.y}, ${scrollState.value}, ${accumulator.value}, $source")
    }
    scroll(
        state = scrollState,
        isScrollable = enabled,
        overscrollEffect = overscrollEffect,
        reverseScrolling = reverseScrolling,
        flingBehavior = flingBehavior,
        isVertical = true
    ).graphicsLayer {
        translationY = sign(tween.value) * sqrt(abs(tween.value)) * 10
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.scroll(
    state: ScrollState,
    overscrollEffect: OverscrollEffect,
    reverseScrolling: Boolean,
    flingBehavior: FlingBehavior?,
    isScrollable: Boolean,
    isVertical: Boolean
) = composed(
    factory = {
        val coroutineScope = rememberCoroutineScope()
        val semantics = Modifier.semantics {
            val accessibilityScrollState = ScrollAxisRange(
                value = { state.value.toFloat() },
                maxValue = { state.maxValue.toFloat() },
                reverseScrolling = reverseScrolling
            )
            if (isVertical) {
                this.verticalScrollAxisRange = accessibilityScrollState
            } else {
                this.horizontalScrollAxisRange = accessibilityScrollState
            }
            Log.i("Scroll", "scroll: $isScrollable, $state")
            if (isScrollable) {
                // when b/156389287 is fixed, this should be proper scrollTo with reverse handling
                scrollBy(
                    action = { x: Float, y: Float ->
                        coroutineScope.launch {
                            if (isVertical) {
                                (state as ScrollableState).animateScrollBy(y)
                            } else {
                                (state as ScrollableState).animateScrollBy(x)
                            }
                        }
                        return@scrollBy true
                    }
                )
            }
        }
        val orientation = if (isVertical) Orientation.Vertical else Orientation.Horizontal
        val scrolling = Modifier.scrollable(
            orientation = orientation,
            reverseDirection = ScrollableDefaults.reverseDirection(
                LocalLayoutDirection.current,
                orientation,
                reverseScrolling
            ),
            enabled = isScrollable,
            interactionSource = MutableInteractionSource(),
            flingBehavior = flingBehavior,
            state = state,
            overscrollEffect = overscrollEffect
        )
        val layout =
            ScrollingLayoutElement(state, reverseScrolling, isVertical)
        semantics
            .clipScrollableContainer(orientation)
            .overscroll(overscrollEffect)
            .then(scrolling)
            .then(layout)
    },
    inspectorInfo = debugInspectorInfo {
        name = "scroll"
        properties["state"] = state
        properties["reverseScrolling"] = reverseScrolling
        properties["flingBehavior"] = flingBehavior
        properties["isScrollable"] = isScrollable
        properties["isVertical"] = isVertical
    }
)