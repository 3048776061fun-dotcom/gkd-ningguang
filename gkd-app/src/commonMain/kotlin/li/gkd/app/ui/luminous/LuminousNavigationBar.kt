package li.gkd.app.ui.luminous

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class LuminousNavItem(
    val icon: ImageVector,
    val label: String,
)

object LuminousNavigationBarDefaults {
    val Height: Dp = 66.dp
}

/**
 * 浮岛式凝光导航栏。
 *
 * 对应 ColorOS 17 的两个设计点：
 * - **流体动效**：选中指示器不是"跳"到新位置，而是在各项之间连贯滑动、融合，
 *   让切换看起来像一次连续的流动而非多个独立状态。
 * - **柔性反馈**：每一项随点按细腻收缩、松手弹性回位，并泛起一圈涟漪。
 */
@Composable
fun LuminousNavigationBar(
    selectedIndex: Int,
    items: List<LuminousNavItem>,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets? = null,
) {
    val spec = LocalLuminous.current
    val shape = RoundedCornerShape(28.dp)
    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxWidth()
                .then(if (windowInsets != null) Modifier.windowInsetsPadding(windowInsets) else Modifier)
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .height(LuminousNavigationBarDefaults.Height),
    ) {
        val count = items.size.coerceAtLeast(1)
        val itemWidth = maxWidth / count

        // 流体动效：指示器连贯滑向选中项
        val indicatorOffset by animateDpAsState(
            targetValue = itemWidth * selectedIndex.coerceIn(0, count - 1),
            animationSpec =
                tween(
                    durationMillis = LuminousMotion.DurationFluid,
                    easing = LuminousMotion.Fluid,
                ),
            label = "luminousNavIndicator",
        )

        // 浮岛底：一整条通透玻璃
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .luminousGlass(shape = shape, spec = spec),
        )

        // 选中指示器：自带向外弥散的光
        Box(
            modifier =
                Modifier
                    .offset(x = indicatorOffset + 4.dp, y = 5.dp)
                    .size(
                        width = itemWidth - 8.dp,
                        height = LuminousNavigationBarDefaults.Height - 10.dp,
                    ).luminousGlass(
                        shape = RoundedCornerShape(24.dp),
                        spec = spec,
                        glowStrength = 1f,
                    ),
        )

        Row(modifier = Modifier.fillMaxSize()) {
            items.forEachIndexed { index, item ->
                LuminousNavItemContent(
                    item = item,
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                )
            }
        }
    }
}

@Composable
private fun LuminousNavItemContent(
    item: LuminousNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spec = LocalLuminous.current
    val interactionSource = remember { MutableInteractionSource() }
    val contentColor by animateColorAsState(
        targetValue =
            if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        animationSpec =
            tween(
                durationMillis = LuminousMotion.DurationStandard,
                easing = LuminousMotion.Standard,
            ),
        label = "luminousNavColor",
    )
    // 柔性反馈：选中时图标带弹性地"涨"起来
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.14f else 1f,
        animationSpec = LuminousMotion.elasticFloatSpring,
        label = "luminousNavIconScale",
    )
    Column(
        modifier =
            modifier
                .luminousPress(interactionSource = interactionSource, pressScale = 0.90f)
                .clip(RoundedCornerShape(24.dp))
                .luminousRipple(color = spec.glow, radius = 56.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                ).semantics { role = Role.Tab },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
            modifier =
                Modifier
                    .size(24.dp)
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                    },
        )
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = item.label,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            textAlign = TextAlign.Center,
        )
    }
}
