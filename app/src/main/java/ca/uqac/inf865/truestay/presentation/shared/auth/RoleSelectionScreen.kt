package ca.uqac.inf865.truestay.presentation.shared.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.large),
        //horizontalAlignment = Alignment.CenterHorizontally,
        //verticalArrangement = Arrangement.Center
    ) {
        Text("Role Selection Screen - TODO")
    }
}