package com.emeris.forkful.ui.auth

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.compose.runtime.collectAsState
import com.emeris.forkful.BuildConfig
import com.emeris.forkful.core.designsystem.DarkAuthBackground
import com.emeris.forkful.core.designsystem.DarkAuthBorder
import com.emeris.forkful.core.designsystem.ForestGreen
import com.emeris.forkful.core.designsystem.PureWhite
import com.emeris.forkful.core.logging.ForkfulLogger
import com.emeris.forkful.core.util.Validators
import com.emeris.forkful.ui.di.containerViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun ForkfulLogoBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(96.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(ForestGreen),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(44.dp)) {
            val strokeW = 4f
            val forkX = size.width * 0.35f
            val knifeX = size.width * 0.65f

            drawLine(Color.White, Offset(forkX - 8f, 10f), Offset(forkX - 8f, 32f), strokeW)
            drawLine(Color.White, Offset(forkX, 10f), Offset(forkX, 32f), strokeW)
            drawLine(Color.White, Offset(forkX + 8f, 10f), Offset(forkX + 8f, 32f), strokeW)
            drawLine(Color.White, Offset(forkX - 8f, 32f), Offset(forkX + 8f, 32f), strokeW)
            drawLine(Color.White, Offset(forkX, 32f), Offset(forkX, size.height - 10f), strokeW + 2f)

            val knifePath = Path().apply {
                moveTo(knifeX, 10f)
                cubicTo(knifeX + 16f, 12f, knifeX + 16f, 34f, knifeX, 40f)
                close()
            }
            drawPath(knifePath, Color.White)
            drawLine(Color.White, Offset(knifeX, 38f), Offset(knifeX, size.height - 10f), strokeW + 2f)
        }
    }
}

@Composable
fun LoginScreen(
    onAuthenticated: (isNewUser: Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        ForkfulLogger.logLifecycle("LoginScreen", "ON_CREATE")
    }

    val viewModel = containerViewModel { LoginViewModel(it.authRepository) }
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val coroutineScope = rememberCoroutineScope()

    var showEmailDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        val current = uiState
        if (current is LoginUiState.Authenticated) {
            onAuthenticated(current.isNewUser)
            viewModel.consumeState()
        }
    }

    val launchGoogleSignIn: () -> Unit = {
        val act = activity
        if (act == null) {
            ForkfulLogger.logAction("LOGIN", "Credential Manager needs an Activity context")
        } else {
            coroutineScope.launch {
                try {
                    val credentialManager = CredentialManager.create(act)
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setServerClientId(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()
                    val response = credentialManager.getCredential(act, request)
                    val credential = response.credential
                    if (credential is CustomCredential &&
                        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                    ) {
                        val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                        viewModel.signInWithGoogle(
                            idToken = googleCredential.idToken,
                            email = googleCredential.id,
                            displayName = googleCredential.displayName
                        )
                    }
                } catch (_: GetCredentialCancellationException) {
                    ForkfulLogger.logAction("LOGIN", "Google sign-in cancelled by user")
                } catch (e: Exception) {
                    ForkfulLogger.logAction("LOGIN", "Credential Manager error: ${e.message}")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkAuthBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1.0f))

        ForkfulLogoBadge()

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Forkful",
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            color = Color(0xFFC7EED8)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Swipe your way to your next\nfavourite meal.",
            fontSize = 16.sp,
            lineHeight = 24.sp,
            color = Color(0xFFB5BEB7),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.weight(1.2f))

        Button(
            onClick = {
                ForkfulLogger.logAction("LOGIN", "Continue with Google tapped")
                launchGoogleSignIn()
            },
            enabled = uiState !is LoginUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(27.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PureWhite,
                contentColor = Color(0xFF1E211E)
            )
        ) {
            if (uiState is LoginUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color(0xFF1E211E),
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "G",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF4285F4)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Continue with Google",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(27.dp))
                .background(Color(0xFF171A16))
                .border(1.dp, DarkAuthBorder, RoundedCornerShape(27.dp))
                .clickable {
                    ForkfulLogger.logAction("LOGIN", "Continue with Email tapped")
                    showEmailDialog = true
                },
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email",
                    tint = PureWhite,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Continue with Email",
                    color = PureWhite,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        if (uiState is LoginUiState.Error) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = (uiState as LoginUiState.Error).message,
                color = Color(0xFFE5989B),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(36.dp))

        Text(
            text = "By continuing, you agree to our Terms of Service\nand Privacy Policy.",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            color = Color(0xFF6B736D),
            textAlign = TextAlign.Center,
            textDecoration = TextDecoration.Underline
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showEmailDialog) {
        EmailAuthDialog(
            isLoading = uiState is LoginUiState.Loading,
            errorMessage = (uiState as? LoginUiState.Error)?.message,
            onDismiss = {
                showEmailDialog = false
                viewModel.consumeState()
            },
            onSignIn = { email, password ->
                viewModel.signInWithEmail(email, password)
            },
            onSignUp = { email, password, displayName ->
                viewModel.signUpWithEmail(email, password, displayName)
            }
        )
    }
}

@Composable
private fun EmailAuthDialog(
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String) -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun submit() {
        when {
            isSignUpMode && displayName.isBlank() ->
                validationError = "Please enter your name."
            !Validators.isValidEmail(email) ->
                validationError = "Please enter a valid email address."
            !Validators.isValidPassword(password) ->
                validationError = "Password must be at least 6 characters."
            else -> {
                validationError = null
                if (isSignUpMode) onSignUp(email.trim(), password, displayName.trim())
                else onSignIn(email.trim(), password)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1B1E1A),
        titleContentColor = PureWhite,
        textContentColor = Color(0xFFB5BEB7),
        title = {
            Text(
                text = if (isSignUpMode) "Create your account" else "Welcome back",
                fontFamily = FontFamily.Serif,
                fontSize = 22.sp,
                color = PureWhite
            )
        },
        text = {
            Column {
                if (isSignUpMode) {
                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        isError = validationError?.contains("name", ignoreCase = true) == true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = ForestGreen,
                            cursorColor = ForestGreen
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    isError = validationError?.contains("email", ignoreCase = true) == true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = ForestGreen,
                        cursorColor = ForestGreen
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = validationError?.contains("Password", ignoreCase = true) == true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        focusedBorderColor = ForestGreen,
                        cursorColor = ForestGreen
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                val message = validationError ?: errorMessage
                if (message != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = message, color = Color(0xFFE5989B), fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { submit() },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ForestGreen,
                    contentColor = PureWhite
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = PureWhite,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isSignUpMode) "Sign up" else "Sign in",
                        fontSize = 14.sp
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { isSignUpMode = !isSignUpMode }) {
                Text(
                    text = if (isSignUpMode) "I already have an account" else "Create an account",
                    color = Color(0xFFC7EED8),
                    fontSize = 13.sp
                )
            }
        }
    )
}
