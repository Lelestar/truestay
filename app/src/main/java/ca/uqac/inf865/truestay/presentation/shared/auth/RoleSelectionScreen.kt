package ca.uqac.inf865.truestay.presentation.shared.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo_truestay),
            contentDescription = "TrueStay Logo"
        )

        Text(
            text = "Créez votre compte",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = LocalAppColors.current.grayDark
        )

        Spacer(modifier = Modifier.height(AppSpacing.xxlarge))

        TrueStayCard(modifier = Modifier.fillMaxWidth()) {
            // Title
            Text(
                text = "Vous êtes :",
                style = MaterialTheme.typography.headlineMedium,
                color = LocalAppColors.current.black
            )

            Spacer(modifier = Modifier.height(AppSpacing.medium))

            Text(
                text = "Sélectionnez votre profil pour continuer",
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.grayDark
            )

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

                // Tuile Locataire
                RoleTile(
                    iconRes = TrueStayIcons.House,
                    iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Locataire",
                    description = "Je recherche un logement à louer et souhaite consulter les avis et réaliser des états des lieux",
                    onClick = { onRoleSelected("locataire") }
                )

                Spacer(modifier = Modifier.height(AppSpacing.large))

                // Tuile Propriétaire
                RoleTile(
                    iconRes = TrueStayIcons.Building,
                    iconBg = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "Propriétaire",
                    description = "Je mets en location un ou plusieurs logements et souhaite gérer mes biens",
                    onClick = { onRoleSelected("proprietaire") }
                )

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = LocalAppColors.current.grayBorder
            )

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

            // Register text and button
            Text(
                text = "Vous avez déjà un compte ?",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.grayDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(AppSpacing.large))
            TrueStayButton(
                text = "Se connecter",
                onClick = onNavigateToLogin,
                variant = ButtonVariant.SECONDARY
            )
        }
    }
}

@Composable
private fun RoleTile(
    iconRes: Int,
    iconBg: Color,
    iconTint: Color,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val borderColor = MaterialTheme.colorScheme.surfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = iconTint
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}