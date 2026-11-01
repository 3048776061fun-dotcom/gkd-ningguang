package li.gkd.app.ui.luminous

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * 凝光环境光背景。
 *
 * 三层与主题主色同源的光晕在界面底部缓慢漂移，形成 ColorOS 17 描述的
 * "更柔和的半透明 + 层次光照"；同时在手指按下处生成一团跟随弥散的光，
 * 对应官方描述的「光随指自然流动」。
 *
 * 该 Modifier 只绘制、不消费指针事件，因此可以安全地铺在任意容器底部。
 */
@Composable
fun Modifier.luminousHalo(
    spec: LuminousSpec = LocalLuminous.current,
    followTouch: Boolean = true,
): Modifier {
    val transition = rememberInfiniteTransition(label = "luminousHalo")
    val phases =
        listOf(
            transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(durationMillis = 19000, easing = LinearEasing)),
                label = "haloA",
            ),
            transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(durationMillis = 25000, easing = LinearEasing)),
                label = "haloB",
            ),
            transition.animateFloat(
                initialValue = 0f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(durationMillis = 31000, easing = LinearEasing)),
                label = "haloC",
            ),
        )

    val pointer = remember { mutableStateOf(Offset.Unspecified) }
    val touchAlpha = remember { Animatable(0f) }

    return this
        .then(
            if (followTouch) {
                Modifier.pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        pointer.value = down.position
                        touchAlpha.snapTo(0f)
                        touchAlpha.animateTo(1f, tween(durationMillis = 240, easing = LinearEasing))
                        val up = waitForUpOrCancellation()
                        if (up != null) pointer.value = up.position
                        touchAlpha.animateTo(0f, tween(durationMillis = 560, easing = LinearEasing))
                    }
                }
            } else {
                Modifier
            },
        ).drawBehind {
            val w = size.width
            val h = size.height
            if (w <= 0f || h <= 0f) return@drawBehind
            val maxSide = max(w, h)

            spec.halos.forEachIndexed { index, color ->
                val phase = phases[index % phases.size].value
                val angle = 2f * PI.toFloat() * phase + index * 2.1f
                val cx = w * (0.5f + 0.34f * cos(angle + index.toFloat()))
                val cy = h * (0.32f + 0.28f * sin(angle * 0.8f + index.toFloat()))
                val radius = maxSide * (0.58f + 0.10f * sin(angle))
                val alpha = spec.haloAlpha * (if (index == 0) 1f else 0.78f)
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            0f to color.copy(alpha = alpha),
                            0.45f to color.copy(alpha = alpha * 0.42f),
                            1f to Color.Transparent,
                            center = Offset(cx, cy),
                            radius = radius,
                        ),
                    radius = radius,
                    center = Offset(cx, cy),
                )
            }

            // 光随指弥散：按下处的一团柔光
            val touchValue = touchAlpha.value
            if (touchValue > 0f && pointer.value != Offset.Unspecified) {
                val r = maxSide * 0.42f
                val a = touchValue
                drawCircle(
                    brush =
                        Brush.radialGradient(
                            0f to spec.glow.copy(alpha = 0.30f * a),
                            0.5f to spec.glow.copy(alpha = 0.12f * a),
                            1f to Color.Transparent,
                            center = pointer.value,
                            radius = r,
                        ),
                    radius = r,
                    center = pointer.value,
                )
            }
        }
}

/** 只绘制静态的环境光晕（用于不希望响应触摸的容器，例如列表头部）。 */
@Composable
fun Modifier.luminousAmbient(spec: LuminousSpec = LocalLuminous.current): Modifier = luminousHalo(spec = spec, followTouch = false)
