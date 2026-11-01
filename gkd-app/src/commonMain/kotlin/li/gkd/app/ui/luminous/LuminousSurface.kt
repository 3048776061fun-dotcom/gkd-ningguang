package li.gkd.app.ui.luminous

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * 凝光玻璃材质。
 *
 * ColorOS 17 的凝光视效强调「更柔和的半透明与层次光照」：
 * - 半透明填充 → 底部环境光晕可以透上来，界面更通透
 * - 顶部亮、底部暗的边缘描边 → 模拟玻璃的折射厚度
 * - 左上掠过的一道内光泽 → 精心打磨的材质纹理
 * - 可选的 [glowStrength] → 元素自身向外弥散的光（用于选中态指示器）
 */
@Composable
fun Modifier.luminousGlass(
    shape: Shape = RoundedCornerShape(24.dp),
    spec: LuminousSpec = LocalLuminous.current,
    glowStrength: Float = 0f,
    borderWidth: Dp = 1.dp,
): Modifier =
    this.drawBehind {
        val outline = shape.createOutline(size, layoutDirection, this)

        if (glowStrength > 0f) {
            drawOutline(
                outline = outline,
                brush =
                    Brush.radialGradient(
                        0f to spec.glow.copy(alpha = 0.34f * glowStrength),
                        0.62f to spec.glow.copy(alpha = 0.12f * glowStrength),
                        1f to Color.Transparent,
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = max(size.width, size.height) * 0.95f,
                    ),
            )
        }

        // 半透明主体
        drawOutline(outline = outline, color = spec.glassFill)

        // 材质纹理：左上斜向掠过的一道薄光
        drawOutline(
            outline = outline,
            brush =
                Brush.linearGradient(
                    0f to spec.sheen.copy(alpha = 0.45f),
                    0.38f to Color.Transparent,
                    1f to Color.Transparent,
                    start = Offset.Zero,
                    end = Offset(size.width * 0.92f, size.height * 0.78f),
                ),
        )

        // 折射边缘：顶部进光、底部厚度
        drawOutline(
            outline = outline,
            brush =
                Brush.verticalGradient(
                    0f to spec.glassEdgeTop,
                    0.42f to Color.Transparent,
                    1f to spec.glassEdgeBottom,
                ),
            style = Stroke(width = borderWidth.toPx()),
        )
    }

/**
 * 凝光卡片：通透玻璃材质 + 柔性按压反馈 + 触碰涟漪。
 */
@Composable
fun LuminousCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    spec: LuminousSpec = LocalLuminous.current,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier =
            modifier
                .luminousPress(
                    interactionSource = interactionSource,
                    pressScale = 0.975f,
                    enabled = onClick != null,
                ).clip(shape)
                .luminousGlass(shape = shape, spec = spec)
                .then(
                    if (onClick != null) {
                        Modifier.luminousRipple(color = spec.glow, radius = 104.dp)
                    } else {
                        Modifier
                    },
                ).then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick,
                        )
                    } else {
                        Modifier
                    },
                ),
    ) {
        content()
    }
}
