package li.gkd.app.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import li.gkd.app.ui.luminous.LocalLuminous

val LocalTopBarWindowInsets = staticCompositionLocalOf<WindowInsets?> { null }

/**
 * 凝光顶栏配色：静止时完全透明（与页面环境光融为一体），
 * 滚动后渐变为一层玻璃 —— 对应 ColorOS 17 流体动效中元素状态间的连贯过渡。
 */
@Composable
fun luminousTopAppBarColors(): TopAppBarColors {
    val spec = LocalLuminous.current
    return TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = spec.glassFill,
    )
}

@Composable
fun GkTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    expandedHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    colors: TopAppBarColors? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    canScroll: Boolean = true,
) {
    val actualColors = colors ?: luminousTopAppBarColors()
    val actualScrollBehavior =
        if (canScroll || scrollBehavior == null) {
            scrollBehavior
        } else {
            remember(scrollBehavior) {
                object : TopAppBarScrollBehavior by scrollBehavior {
                    // disable inner scroll effect
                    override val isPinned: Boolean
                        get() = true
                }
            }
        }
    // SingleRowTopAppBar 内部 containerColor+scrolledContainerColor 合成了一个动画
    // 应用主题颜色更新时形成叠加动画，导致和周围正常组件视觉变换效果表现割裂
    key(MaterialTheme.colorScheme.primary) {
        TopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
            expandedHeight = expandedHeight,
            windowInsets = LocalTopBarWindowInsets.current ?: TopAppBarDefaults.windowInsets,
            colors = actualColors,
            scrollBehavior = actualScrollBehavior,
        )
    }
}
