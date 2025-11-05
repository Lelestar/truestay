package ca.uqac.inf865.truestay.presentation.shared.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ca.uqac.inf865.truestay.R
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayButton
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayCard
import ca.uqac.inf865.truestay.presentation.common.components.TrueStayIcon
import ca.uqac.inf865.truestay.presentation.common.icons.TrueStayIcons
import ca.uqac.inf865.truestay.presentation.theme.AppSpacing
import ca.uqac.inf865.truestay.presentation.theme.LocalAppColors
import ca.uqac.inf865.truestay.presentation.theme.TrueStayTheme

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
                TrueStayIcon(
                    iconRes = TrueStayIcons.Check,
                    tint = LocalAppColors.current.success,
                    contentDescriptionRes = null,
                    size = 32.dp
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

            // Title
            Text(
                text = stringResource(R.string.forgot_password_email_sent_title),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                color = LocalAppColors.current.black
            )

            Spacer(modifier = Modifier.height(AppSpacing.small))

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
                style = MaterialTheme.typography.headlineSmall,
                color = LocalAppColors.current.primary
            )

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

            // Steps
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = LocalAppColors.current.infoSurface,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = LocalAppColors.current.info
                )
            ) {
                Column(
                    modifier = Modifier.padding(AppSpacing.large)
                ) {
                    Text(
                        text = stringResource(R.string.forgot_password_email_sent_steps_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = LocalAppColors.current.info
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.medium))

                    Text(
                        text = "1. ${stringResource(R.string.forgot_password_email_sent_step_1)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalAppColors.current.onInfoSurface
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.small))

                    Text(
                        text = "2. ${stringResource(R.string.forgot_password_email_sent_step_2)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalAppColors.current.onInfoSurface
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.small))

                    Text(
                        text = "3. ${stringResource(R.string.forgot_password_email_sent_step_3)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = LocalAppColors.current.onInfoSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.xlarge))

            // Note
            NoteText()

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
private fun NoteText() {
    val colors = LocalAppColors.current
    val labelStyle = MaterialTheme.typography.titleSmall.toSpanStyle().copy(color = colors.grayDark)
    val textStyle = MaterialTheme.typography.bodySmall.toSpanStyle().copy(color = colors.grayDark)

    val note = buildAnnotatedString {
        withStyle(labelStyle) {
            append(stringResource(R.string.forgot_password_email_sent_note_label))
        }
        append(" ")
        withStyle(textStyle) {
            append(stringResource(R.string.forgot_password_email_sent_note))
        }
    }

    Text(
        text = note,
        modifier = Modifier.fillMaxWidth()
    )
}

// ==========================================
// Previews
// ==========================================
@Preview(name = "Email Sent", showBackground = true)
@Composable
private fun ForgotPasswordEmailSentPreview() {
    TrueStayTheme {
        ForgotPasswordEmailSentScreen(
            email = "user@example.com",
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "Email Sent - Long Email", showBackground = true)
@Composable
private fun ForgotPasswordEmailSentLongEmailPreview() {
    TrueStayTheme {
        ForgotPasswordEmailSentScreen(
            email = "very.long.email.address@example-company.com",
            onNavigateToLogin = {}
        )
    }
}
