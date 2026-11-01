package li.gkd.app.ui.luminous

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 追光进度条。
 *
 * 对应 ColorOS 17 「播放时进度条追光」的描述：进度条本体是一段渐变的凝光，
 * 另有一枚光点沿已完成的进度来回流动，让"当前进程"一眼可见，
 * 而不是一条没有生命力的静态色带。
 */
@Composable
fun LuminousLinearProgress(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = LocalLuminous.current.glow,
    spec: LuminousSpec = LocalLuminous.current,
    height: Dp = 8.dp,
) {
    val transition = rememberInfiniteTransition(label = "luminousProgress")
    val shimmer by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 1500, easing = LinearEasing)),
        label = "luminousProgressShimmer",
    )
    Box(
        modifier =
            modifier
                .height(height)
                .clip(CircleShape)
                .drawBehind {
                    val p = progress().coerceIn(0f, 1f)
                    val corner = CornerRadius(size.height / 2f, size.height / 2f)
                    drawRoundRect(color = spec.track, cornerRadius = corner)
                    if (p <= 0f) return@drawBehind
                    val filled = size.width * p
                    drawRoundRect(
                        brush =
                            Brush.horizontalGradient(
                                0f to color.copy(alpha = 0.45f),
                                0.65f to color,
                                1f to color,
                            ),
                        size = Size(filled, size.height),
                        cornerRadius = corner,
                    )
                    // 追光：一枚沿进度流动的光点
                    val cx = filled * (0.08f + 0.92f * shimmer)
                    drawCircle(
                        brush =
                            Brush.radialGradient(
                                0f to Color.White.copy(alpha = 0.55f),
                                1f to Color.Transparent,
                                center = Offset(cx, size.height / 2f),
                                radius = size.height * 1.9f,
                            ),
                        radius = size.height * 1.9f,
                        center = Offset(cx, size.height / 2f),
                    )
                },
    )
}
