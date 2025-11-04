package ca.uqac.inf865.truestay.presentation.shared.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors

@Composable
fun ForgotPasswordEmailSentScreen(
    email: String,
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
            contentDescription = stringResource(R.string.app_name),
        )
        Spacer(modifier = Modifier.height(AppSpacing.small))
        Text(
            text = stringResource(R.string.forgot_password_tagline),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = LocalAppColors.current.grayDark
        )

        Spacer(modifier = Modifier.height(AppSpacing.xxlarge))

        TrueStayCard(modifier = Modifier.fillMaxWidth()) {
            // Success icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(LocalAppColors.current.success.copy(alpha = 0.1f))
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = TrueStayIcons.Check),
                    contentDescription = null,
                    tint = LocalAppColors.current.success,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.large))

            // Title
            Text(
                text = stringResource(R.string.forgot_password_email_sent_title),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                color = LocalAppColors.current.black
            )

            Spacer(modifier = Modifier.height(AppSpacing.medium))

            // Subtitle
            Text(
                text = stringResource(R.string.forgot_password_email_sent_subtitle),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.grayDark
            )

            Spacer(modifier = Modifier.height(AppSpacing.small))

            // Email address
            Text(
                text = email,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = LocalAppColors.current.primary
            )

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

            // Steps card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(LocalAppColors.current.primaryLight.copy(alpha = 0.1f))
                    .padding(AppSpacing.large)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.forgot_password_email_sent_steps_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = LocalAppColors.current.black
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.medium))

                    // Step 1
                    StepRow(
                        number = 1,
                        text = stringResource(R.string.forgot_password_email_sent_step_1)
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.small))

                    // Step 2
                    StepRow(
                        number = 2,
                        text = stringResource(R.string.forgot_password_email_sent_step_2)
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.small))

                    // Step 3
                    StepRow(
                        number = 3,
                        text = stringResource(R.string.forgot_password_email_sent_step_3)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

            // Note
            Text(
                text = stringResource(R.string.forgot_password_email_sent_note),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall,
                color = LocalAppColors.current.grayDark,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

            // Back to login button
            TrueStayButton(
                text = stringResource(R.string.forgot_password_email_sent_button),
                onClick = onNavigateToLogin
            )
        }
    }
}

@Composable
private fun StepRow(
    number: Int,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.bodyMedium,
            color = LocalAppColors.current.primary
        )
        Spacer(modifier = Modifier.padding(horizontal = AppSpacing.small))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = LocalAppColors.current.grayDark,
            modifier = Modifier.weight(1f)
        )
    }
}