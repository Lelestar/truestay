package ca.uqac.inf865.truestay.presentation.shared.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.components.ButtonVariant
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

@Composable
fun RoleSelectionScreen(
    onRoleSelected: (String) -> Unit,
    onNavigateToLogin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppSpacing.large),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.logo_truestay),
            contentDescription = stringResource(R.string.app_name)
        )
        Spacer(modifier = Modifier.height(AppSpacing.small))
        Text(
            text = stringResource(R.string.role_create_account),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = LocalAppColors.current.grayDark
        )

        Spacer(modifier = Modifier.height(AppSpacing.xxlarge))

        TrueStayCard(modifier = Modifier.fillMaxWidth()) {
            // Title
            Text(
                text = stringResource(R.string.role_you_are),
                style = MaterialTheme.typography.headlineMedium,
                color = LocalAppColors.current.black
            )

            Spacer(modifier = Modifier.height(AppSpacing.small))

            Text(
                text = stringResource(R.string.role_profile_selection),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.grayDark
            )

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

                // Tenant Tile
                RoleTile(
                    iconRes = TrueStayIcons.House,
                    iconBg = LocalAppColors.current.primarySurface,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = stringResource(R.string.role_tenant),
                    description = stringResource(R.string.role_tenant_description),
                    onClick = { onRoleSelected("tenant") }
                )

                Spacer(modifier = Modifier.height(AppSpacing.large))

                // Landlord Tile
                RoleTile(
                    iconRes = TrueStayIcons.Building,
                    iconBg = LocalAppColors.current.primaryVariantSurface,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = stringResource(R.string.role_landlord) ,
                    description = stringResource(R.string.role_landlord_description),
                    onClick = { onRoleSelected("landlord") }
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
                text = stringResource(R.string.role_already_have_account),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.grayDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(AppSpacing.large))
            TrueStayButton(
                text = stringResource(R.string.login_button),
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
            .clip(MaterialTheme.shapes.medium)
            .border(width = 2.dp, color = borderColor, shape = MaterialTheme.shapes.medium)
            .clickable { onClick() }
            .padding(AppSpacing.large),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            TrueStayIcon(
                iconRes = iconRes,
                tint = iconTint,
                contentDescriptionRes = null,
            )
        }

        Spacer(modifier = Modifier.size(AppSpacing.large))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = LocalAppColors.current.black
            )
            Spacer(modifier = Modifier.height(AppSpacing.small))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.grayDark
            )
        }
    }
}

// ==========================================
// Previews
// ==========================================
@Preview(name = "Role Selection", showBackground = true)
@Composable
private fun RoleSelectionScreenPreview() {
    TrueStayTheme {
        RoleSelectionScreen(
            onRoleSelected = {},
            onNavigateToLogin = {}
        )
    }
}
