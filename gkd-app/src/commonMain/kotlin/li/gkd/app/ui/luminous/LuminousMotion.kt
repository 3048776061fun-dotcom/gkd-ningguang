package li.gkd.app.ui.luminous

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ColorOS 17 流体设计的三条动效主线：
 *
 * - **流体动效 (Fluid Motion)**：元素从一个状态平滑移动、合并、变换到下一个状态，
 *   过渡不像"切页"，而像一次连续的流动。 → [LuminousMotion.Fluid]
 * - **柔性反馈 (Flexible Feedback)**：轻点细腻收缩、拖拽自然拉伸、松手弹性回位，
 *   动效随手势的速度与力度自适应。 → [Modifier.luminousPress]
 * - **凝光视效 (Luminous Effect)**：光随指弥散、触碰泛起涟漪。 → [Modifier.luminousRipple]
 */
object LuminousMotion {
    /** 流体动效：起步快、收尾缓，让相邻元素的过渡连贯自然。 */
    val Fluid: Easing = CubicBezierEasing(0.32f, 0.94f, 0.60f, 1.00f)

    /** 标准动效：Material 风格加一点点前置加速。 */
    val Standard: Easing = CubicBezierEasing(0.40f, 0.00f, 0.20f, 1.00f)

    /** 柔性反馈：带轻微过冲的回弹曲线，对应"松手弹性回位"。 */
    val Elastic: Easing = CubicBezierEasing(0.34f, 1.46f, 0.64f, 1.00f)

    /** 退出：加速离场，不拖泥带水。 */
    val Accelerate: Easing = CubicBezierEasing(0.30f, 0.00f, 0.80f, 0.20f)

    const val DurationFluid = 420
    const val DurationStandard = 300
    const val DurationQuick = 180

    /** 用于状态位移（如导航栏指示器）的弹性弹簧。 */
    fun <T> fluidSpring(visibilityThreshold: T? = null) =
        spring<T>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = visibilityThreshold,
        )

    /** 用于按压回弹的弹簧：中等阻尼 + 轻微弹性，模拟真实惯性。 */
    val elasticFloatSpring =
        spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
            visibilityThreshold = 0.0005f,
        )
}

/**
 * 柔性反馈：按下时细腻收缩，松手时弹性回位。
 *
 * 对应 ColorOS 17 的 Flexible Feedback —— 按钮与工具栏随点按自然压缩、回弹，
 * 而不是机械地播放一段预设动画。
 */
@Composable
fun Modifier.luminousPress(
    interactionSource: InteractionSource,
    pressScale: Float = 0.94f,
    enabled: Boolean = true,
): Modifier {
    if (!enabled) return this
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressScale else 1f,
        animationSpec = LuminousMotion.elasticFloatSpring,
        label = "luminousPress",
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * 凝光涟漪：触碰处泛起一圈向外弥散的光晕。
 *
 * 对应 ColorOS 17 中"切歌时泛起涟漪 / 触碰时光随指流动"的描述。
 * 这里只做绘制，不消费任何指针事件，因此不会干扰子组件的点击与滚动。
 */
@Composable
fun Modifier.luminousRipple(
    color: Color = LocalLuminous.current.glow,
    radius: Dp = 72.dp,
    enabled: Boolean = true,
): Modifier {
    if (!enabled) return this
    val progress = remember { Animatable(0f) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    return this
        .pointerInput(Unit) {
            awaitEachGesture {
                // requireUnconsumed = false：保证子组件仍可正常响应点击
                val down = awaitFirstDown(requireUnconsumed = false)
                origin = down.position
                progress.snapTo(0f)
                progress.animateTo(1f, tween(durationMillis = 380, easing = LinearEasing))
                waitForUpOrCancellation()
                progress.animateTo(1.12f, tween(durationMillis = 180, easing = LinearEasing))
                progress.snapTo(0f)
            }
        }
        .then(
            Modifier.drawRippleContent(progress = progress, origin = origin, color = color, radius = radius),
        )
}

private fun Modifier.drawRippleContent(
    progress: Animatable<Float, AnimationVector1D>,
    origin: Offset,
    color: Color,
    radius: Dp,
): Modifier =
    this.drawWithContent {
        drawContent()
        val value = progress.value
        if (value > 0f && value < 1.12f) {
            val r = radius.toPx() * value
            val alpha = (1f - value).coerceIn(0f, 1f)
            drawCircle(
                brush =
                    Brush.radialGradient(
                        0f to color.copy(alpha = 0.26f * alpha),
                        0.55f to color.copy(alpha = 0.14f * alpha),
                        1f to Color.Transparent,
                        center = origin,
                        radius = r,
                    ),
                radius = r,
                center = origin,
            )
        }
    }
