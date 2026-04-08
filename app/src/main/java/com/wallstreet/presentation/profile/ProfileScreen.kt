package com.wallstreet.presentation.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.wallstreet.core.preferences.ThemePreferences
import com.wallstreet.core.preferences.ThemeTypes
import com.wallstreet.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = koinViewModel()
) {
    val user = viewModel.user
    val isLoggedOut by viewModel.isLoggedOut.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val themePrefs = remember { ThemePreferences(context) }
    val scope = rememberCoroutineScope()

    val selectedTheme by themePrefs.theme.collectAsState(initial = ThemeTypes.SYSTEM)
    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) onLogout()
    }

    if (user == null) {
        Text("No user logged in")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()


    ) {


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)


        ) {
            Text("APP APPEARANCE", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))

            ThemeToggle()
            Spacer(modifier = Modifier.height(32.dp))
        }
        Text(
            "ACCOUNT",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))



        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                placeholder = painterResource(R.drawable.profile_circle),
                error = painterResource(R.drawable.profile_circle)

            )
            Column() {
                Text(user.name, style = MaterialTheme.typography.bodyMedium)
                Text(user.email, style = MaterialTheme.typography.bodyMedium)
            }

        }
        Divider(
            color = MaterialTheme.colorScheme.outline,
            thickness = 1.dp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                placeholder = painterResource(R.drawable.profile_circle),
                error = painterResource(R.drawable.profile_circle)

            )
            Column() {
                Text("Security & Privacy", style = MaterialTheme.typography.bodyMedium)
            }

        }
        Divider(
            color = MaterialTheme.colorScheme.outline,
            thickness = 1.dp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                placeholder = painterResource(R.drawable.profile_circle),
                error = painterResource(R.drawable.profile_circle)

            )
            Column() {
                Text("Privacy Policy", style = MaterialTheme.typography.bodyMedium)
            }

        }
        Divider(
            color = MaterialTheme.colorScheme.outline,
            thickness = 1.dp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                placeholder = painterResource(R.drawable.profile_circle),
                error = painterResource(R.drawable.profile_circle)

            )
            Column() {
                Text("Terms of Service", style = MaterialTheme.typography.bodyMedium)
            }

        }



        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "DELETE ACCOUNT",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))



        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                placeholder = painterResource(R.drawable.profile_circle),
                error = painterResource(R.drawable.profile_circle)

            )
            Column() {
                Text("Delete Account", style = MaterialTheme.typography.bodyMedium)
                Text("Permanently remove all data", style = MaterialTheme.typography.bodyMedium)
            }

        }
        Spacer(modifier = Modifier.height(12.dp))



        OutlinedButton(
            onClick = { viewModel.signOut() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.error
            )
        ) {
            Text("Logout")
        }
 
    }
}