package li.gkd.app.ui.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import li.gkd.app.ui.component.GkScaffold
import li.gkd.app.ui.luminous.LuminousNavItem
import li.gkd.app.ui.luminous.LuminousNavigationBar
import org.jetbrains.compose.resources.stringResource

// A desktop device profile supplies Android's bottom inset; Android uses native defaults.
val LocalHomeNavigationInsets = staticCompositionLocalOf<WindowInsets?> { null }

@Composable
fun GkHomeScaffold(
    page: ScaffoldExt,
    selectedTab: BottomNavItem,
    onSelectTab: (BottomNavItem) -> Unit,
) {
    val tabs = BottomNavItem.allSubObjects
    val navItems =
        tabs.map { item -> LuminousNavItem(icon = item.icon, label = stringResource(item.labelResource)) }
    GkScaffold(
        modifier = page.modifier,
        topBar = page.topBar,
        floatingActionButton = page.floatingActionButton,
        bottomBar = {
            LuminousNavigationBar(
                selectedIndex = tabs.indexOf(selectedTab).coerceAtLeast(0),
                items = navItems,
                onSelect = { index -> onSelectTab(tabs[index]) },
                windowInsets = LocalHomeNavigationInsets.current ?: NavigationBarDefaults.windowInsets,
            )
        },
        content = page.content,
    )
}
