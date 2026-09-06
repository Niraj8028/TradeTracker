package com.wallstreet.presentation.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wallstreet.ui.theme.Gradient
import com.wallstreet.ui.theme.SuccessGreen

/**
 * Shared shell for every pre-auth screen (Welcome, Login, Register, Email verification).
 *
 * Paints the app [Gradient] background, hosts a standardized top-center snackbar, and stacks
 * a branded [header] (centered in the gradient area) above a bottom sheet-style [Surface]
 * holding [sheetContent]. Header and sheet never overlap — the sheet scrolls internally and
 * lifts above the keyboard.
 *
 * @param sheetHeightFraction when set, pins the sheet to this fraction of the screen height;
 *   `null` lets the sheet wrap its content.
 */
@Composable
fun AuthScaffold(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbarIsError: Boolean = true,
    sheetHeightFraction: Float? = null,
    header: @Composable ColumnScope.() -> Unit,
    sheetContent: @Composable ColumnScope.() -> Unit,
) {
    val gradient = Gradient.current
    val sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

    Scaffold(
        modifier = modifier,
        containerColor = Color.Transparent,
        snackbarHost = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 50.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) { data ->
                    Snackbar(
                        containerColor = if (snackbarIsError)
                            MaterialTheme.colorScheme.error else SuccessGreen,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(data.visuals.message) }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(top = padding.calculateTopPadding()),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                content = header,
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (sheetHeightFraction != null) Modifier.fillMaxHeight(sheetHeightFraction)
                        else Modifier
                    )
                    .shadow(
                        elevation = 20.dp,
                        shape = sheetShape,
                        ambientColor = Color.Black.copy(alpha = 0.3f),
                        spotColor = Color.Black.copy(alpha = 0.3f)
                    ),
                shape = sheetShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    content = sheetContent
                )
            }
        }
    }
}
