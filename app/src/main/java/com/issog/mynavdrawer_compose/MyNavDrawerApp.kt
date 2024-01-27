package com.issog.mynavdrawer_compose

import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.issog.mynavdrawer_compose.ui.theme.MyNavDrawerComposeTheme
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun MyNavDrawerApp() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val context = LocalContext.current

    BackPressHandler(isEnabled = drawerState.isOpen) {
        scope.launch {
            drawerState.close()
        }
    }

    val items = listOf(
        MenuItem(
            stringResource(id = R.string.home),
            Icons.Default.Home
        ),
        MenuItem(
            stringResource(id = R.string.favourite),
            Icons.Default.Favorite
        ),
        MenuItem(
            stringResource(id = R.string.profile),
            Icons.Default.AccountCircle
        )
    )
    var selectedItem by remember {
        mutableStateOf(items[0])
    }

    Scaffold(
        topBar = {
            MyTopBar(
                onMenuClick = {
                    scope.launch {
                        if (drawerState.isClosed) {
                            drawerState.open()
                        } else {
                            drawerState.close()
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { paddingValues ->
        ModalNavigationDrawer(
            modifier = Modifier.padding(paddingValues),
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen,
            drawerContent = {
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(id = R.string.hello_from_nav_drawer),
                        modifier = Modifier.padding(bottom = 12.dp, start = 12.dp, end = 12.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    items.forEach { menu ->
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    imageVector = menu.icon,
                                    contentDescription = null
                                )
                            },
                            label = {
                                Text(text = menu.tile)
                            },
                            selected = menu == selectedItem,
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    val snackbarResult = snackbarHostState.showSnackbar(
                                        message = context.resources.getString(R.string.coming_soon, menu.tile),
                                        actionLabel = context.resources.getString(R.string.subscribe_question),
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Short
                                    )
                                    when (snackbarResult) {
                                        SnackbarResult.ActionPerformed -> {
                                            Toast.makeText(
                                                context,
                                                context.resources.getString(R.string.subscribed_info),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        SnackbarResult.Dismissed -> {
                                            Toast.makeText(
                                                context,
                                                context.resources.getString(R.string.subscribed_info),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                                selectedItem = menu
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }
                }
            },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (drawerState.isClosed) {
                            stringResource(id = R.string.swipe_to_open)
                        } else {
                            stringResource(id = R.string.swipe_to_close)
                        }
                    )
                }
            }
        )
    }
}

@Composable
fun BackPressHandler(isEnabled: Boolean = true, onBackPressed: () -> Unit) {
    val currentOnBackPressed by rememberUpdatedState(onBackPressed)
    val backCallback = remember {
        object : OnBackPressedCallback(isEnabled) {
            override fun handleOnBackPressed() {
                currentOnBackPressed.invoke()
            }
        }
    }

    SideEffect {
        backCallback.isEnabled = isEnabled
    }

    val backDispatcher = checkNotNull(LocalOnBackPressedDispatcherOwner.current) {
        "No OnBackPressedDispatcherOwner was provided via LocalOnBackPressedDispatcherOwner"
    }.onBackPressedDispatcher

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(key1 = lifecycleOwner, key2 = backDispatcher) {
        backDispatcher.addCallback(lifecycleOwner, backCallback)
        onDispose {
            backCallback.remove()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopBar(
    onMenuClick: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    onMenuClick.invoke()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = stringResource(id = R.string.menu)
                )
            }
        },
        title = { 
            Text(text = stringResource(id = R.string.app_name))
        }
    )
}

data class MenuItem(val tile: String, val icon: ImageVector)

@Preview(showBackground = true)
@Composable
fun MyNavDrawerAppPreview() {
    MyNavDrawerComposeTheme {
        MyNavDrawerApp()
    }
}
