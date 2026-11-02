package li.gkd.app.ui.luminous

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlin.math.pow

/**
 * ColorOS 17 「凝光视效 (Luminous Effect)」 视觉规格。
 *
 * 设计要点取自 OPPO 官方对 Luminous Effect 的描述：
 * - 从光与材质的自然属性中汲取灵感，使用更柔和的半透明、层次光照与精心打磨的纹理
 * - 界面更轻盈、层次更自然，同时保持图标与文字清晰可读
 * - 光成为交互的一部分：弥散、涟漪、动态光照随触碰响应
 *
 * 因此这里的颜色全部由「动态取色」推导：先把 MaterialTheme 的 primary/secondary/tertiary
 * 与一组固定的凝光色（青、紫、薄荷）做混合，保证在任何主题（含 Android 动态取色）下
 * 光晕都与界面主色同源，而不会出现突兀的杂色。
 */
data class LuminousSpec(
    /** 当前是否为深色主题 */
    val dark: Boolean,
    /** 三层环境光晕的颜色，与主题主色同源 */
    val halos: List<Color>,
    /** 环境光晕的基础不透明度 */
    val haloAlpha: Float,
    /** 玻璃卡片的填充色（半透明，保证通透） */
    val glassFill: Color,
    /** 玻璃边缘高光（顶部，模拟折射进光） */
    val glassEdgeTop: Color,
    /** 玻璃边缘暗部（底部，模拟折射厚度） */
    val glassEdgeBottom: Color,
    /** 内部光泽 / 追光高光 */
    val sheen: Color,
    /** 卡片、列表项等普通容器的半透明填充（通透但不影响可读性） */
    val cardFill: Color,
    /** 交互发光色（涟漪、按钮光晕、追光进度条） */
    val glow: Color,
    /** 轨道底色 */
    val track: Color,
) {
    companion object {
        val Default =
            LuminousSpec(
                dark = false,
                halos = listOf(Color(0xFF7FCBFF), Color(0xFFB39CFF), Color(0xFF86E9C7)),
                haloAlpha = 0.38f,
                glassFill = Color(0xB3FFFFFF),
                glassEdgeTop = Color(0xB3FFFFFF),
                glassEdgeBottom = Color(0x1F0B4F6B),
                sheen = Color(0x8CFFFFFF),
                cardFill = Color(0xC7FFFFFF),
                glow = Color(0xFF4EA8FF),
                track = Color(0x1F0B4F6B),
            )
    }
}

// 凝光三原色：取自 ColorOS 凝光视效中常见的青、紫、薄荷三档环境光
private val HaloCyan = Color(0xFF6FC7FF)
private val HaloViolet = Color(0xFFA98CFF)
private val HaloMint = Color(0xFF77E8C1)

/** 依据当前 [ColorScheme] 推导凝光视觉规格。 */
fun luminousSpec(colorScheme: ColorScheme): LuminousSpec {
    val dark = relativeLuminance(colorScheme.background) < 0.5f
    // 深色模式下保留更多原色饱和度，浅色模式下向凝光色偏移以保证"通透纯净"
    val mix = if (dark) 0.30f else 0.62f
    val halos =
        listOf(
            mixColor(colorScheme.primary, HaloCyan, mix),
            mixColor(colorScheme.secondary, HaloViolet, mix),
            mixColor(colorScheme.tertiary, HaloMint, mix),
        )
    return LuminousSpec(
        dark = dark,
        halos = halos,
        haloAlpha = if (dark) 0.30f else 0.38f,
        glassFill = if (dark) Color(0x6612171F) else Color(0xB3FFFFFF),
        glassEdgeTop = if (dark) Color(0x54FFFFFF) else Color(0xBFFFFFFF),
        glassEdgeBottom = if (dark) Color(0x0FFFFFFF) else Color(0x240B4F6B),
        sheen = if (dark) Color(0x4DFFFFFF) else Color(0x99FFFFFF),
        cardFill = colorScheme.surfaceContainer.copy(alpha = if (dark) 0.72f else 0.80f),
        glow = halos.first(),
        track = if (dark) Color(0x33FFFFFF) else Color(0x240B4F6B),
    )
}

/** WCAG 相对亮度，用于在不依赖平台 API 的情况下判断深浅主题。 */
private fun relativeLuminance(color: Color): Float {
    val r = linearChannel(color.red)
    val g = linearChannel(color.green)
    val b = linearChannel(color.blue)
    return 0.2126f * r + 0.7152f * g + 0.0722f * b
}

private fun linearChannel(value: Float): Float =
    if (value <= 0.03928f) {
        value / 12.92f
    } else {
        ((value + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
    }

private fun mixColor(
    from: Color,
    to: Color,
    fraction: Float,
): Color =
    Color(
        red = from.red + (to.red - from.red) * fraction,
        green = from.green + (to.green - from.green) * fraction,
        blue = from.blue + (to.blue - from.blue) * fraction,
        alpha = 1f,
    )

/** 全应用共享的凝光视觉规格。 */
val LocalLuminous = staticCompositionLocalOf { LuminousSpec.Default }
