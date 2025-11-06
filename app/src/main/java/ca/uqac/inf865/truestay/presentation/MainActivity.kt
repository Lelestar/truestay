package ca.uqac.inf865.truestay.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.uqac.inf865.truestay.presentation.navigation.TrueStayNavigation
import ca.uqac.inf865.truestay.presentation.shared.auth.AuthState
import ca.uqac.inf865.truestay.presentation.shared.auth.AuthViewModel
import ca.uqac.inf865.truestay.data.repository.ThemePreferencesRepositoryImpl
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val repository by lazy { ThemePreferencesRepositoryImpl(applicationContext) }
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by repository.isDarkTheme.collectAsState(initial = false)

            TrueStayTheme(
                darkTheme = isDarkTheme,
                dynamicColor = false
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authState by authViewModel.authState.collectAsStateWithLifecycle()

                    TrueStayApp(authState = authState)
                }
            }
        }
    }
}

@Composable
fun TrueStayApp(
    authState: AuthState
) {
    when (authState) {
        is AuthState.Loading -> {
            // Loading screen during authentication check
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is AuthState.Unauthenticated -> {
            // Navigation without authenticated user
            TrueStayNavigation(userRole = null)
        }
        is AuthState.Authenticated -> {
            // Navigation with authenticated user
            TrueStayNavigation(userRole = authState.user.role)
        }
    }
}
