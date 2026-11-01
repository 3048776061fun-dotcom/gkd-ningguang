package li.gkd.app.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.FabPosition
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import li.gkd.app.ui.luminous.LocalLuminous
import li.gkd.app.ui.luminous.luminousHalo

/**
 * Native Scaffold on Android; the development window supplies equivalent safe insets.
 *
 * 页面底部铺设 ColorOS 17 凝光环境光（[luminousHalo]），Scaffold 容器改为透明，
 * 让光晕能够透上来 —— 这是「凝光视效」通透感的来源。
 */
@Composable
fun GkScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    floatingActionButtonPosition: FabPosition = FabPosition.End,
    containerColor: Color = Color.Unspecified,
    contentColor: Color = Color.Unspecified,
    contentWindowInsets: WindowInsets =
        LocalEditorWindowInsets.current ?: ScaffoldDefaults.contentWindowInsets,
    content: @Composable (PaddingValues) -> Unit,
) {
    val luminous = LocalLuminous.current
    val baseColor =
        if (containerColor == Color.Unspecified) {
            MaterialTheme.colorScheme.background
        } else {
            containerColor
        }
    Box(modifier = modifier.luminousHalo(spec = luminous)) {
        Scaffold(
            modifier = Modifier,
            topBar = topBar,
            bottomBar = bottomBar,
            snackbarHost = snackbarHost,
            floatingActionButton = floatingActionButton,
            floatingActionButtonPosition = floatingActionButtonPosition,
            // 容器透明，让底部的凝光环境光透上来
            containerColor = if (containerColor == Color.Unspecified) Color.Transparent else containerColor,
            contentColor = if (contentColor == Color.Unspecified) contentColorFor(baseColor) else contentColor,
            contentWindowInsets = contentWindowInsets,
            content = content,
        )
    }
}
