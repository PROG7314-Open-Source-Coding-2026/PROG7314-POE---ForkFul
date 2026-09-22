package com.emeris.forkful.ui.auth

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.emeris.forkful.BuildConfig
import com.emeris.forkful.core.designsystem.DarkAuthBackground
import com.emeris.forkful.core.designsystem.DarkAuthBorder
import com.emeris.forkful.core.designsystem.ForestGreen
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
            .size(80.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(ForestGreen),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(38.dp)) {
            val strokeW = 3.5f
            val forkX = size.width * 0.35f
            val knifeX = size.width * 0.65f

            drawLine(Color.White, Offset(forkX - 7f, 8f), Offset(forkX - 7f, 26f), strokeW)
            drawLine(Color.White, Offset(forkX, 8f), Offset(forkX, 26f), strokeW)
            drawLine(Color.White, Offset(forkX + 7f, 8f), Offset(forkX + 7f, 26f), strokeW)
            drawLine(Color.White, Offset(forkX - 7f, 26f), Offset(forkX + 7f, 26f), strokeW)
            drawLine(Color.White, Offset(forkX, 26f), Offset(forkX, size.height - 8f), strokeW + 2f)

            val knifePath = Path().apply {
                moveTo(knifeX, 8f)
                cubicTo(knifeX + 14f, 10f, knifeX + 14f, 28f, knifeX, 34f)
                close()
            }
            drawPath(knifePath, Color.White)
            drawLine(Color.White, Offset(knifeX, 32f), Offset(knifeX, size.height - 8f), strokeW + 2f)
        }
    }
}

@Composable
fun LoginScreen(
    onAuthenticated: (isNewUser: Boolean) -> Unit,
    onNavigateToSignUp: () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    val viewModel = containerViewModel { LoginViewModel(it.authRepository) }
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        ForkfulLogger.logLifecycle("LoginScreen", "ON_CREATE")
    }

    LaunchedEffect(uiState) {
        val current = uiState
        if (current is LoginUiState.Authenticated) {
            onAuthenticated(current.isNewUser)
            viewModel.consumeState()
        }
    }

    fun submitEmailLogin() {
        validationError = when {
            !Validators.isValidEmail(email) -> "Please enter a valid email address."
            !Validators.isValidPassword(password) -> "Password must be at least 6 characters."
            else -> null
        }
        if (validationError == null) {
            focusManager.clearFocus()
            viewModel.signInWithEmail(email.trim(), password)
        }
    }

    val launchGoogleSignIn: () -> Unit = {
        if (activity == null) {
            ForkfulLogger.logAction("LOGIN", "Credential Manager needs an Activity context")
        } else {
            coroutineScope.launch {
                try {
                    val credentialManager = CredentialManager.create(activity)
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setServerClientId(BuildConfig.GOOGLE_SERVER_CLIENT_ID)
                        .setFilterByAuthorizedAccounts(false)
                        .build()
                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()
                    val response = credentialManager.getCredential(activity, request)
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
            .padding(horizontal = 24.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        ForkfulLogoBadge()
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Welcome back",
            fontFamily = FontFamily.Serif,
            fontSize = 32.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Sign in to access your fridge and recipe decks",
            fontSize = 14.sp,
            color = Color(0xFFB5BEB7),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                validationError = null
            },
            label = { Text("Email address") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = Color(0xFF8B948E)
                )
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = ForestGreen,
                unfocusedBorderColor = DarkAuthBorder,
                focusedLabelColor = ForestGreen,
                unfocusedLabelColor = Color(0xFF8B948E),
                cursorColor = ForestGreen
            ),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                validationError = null
            },
            label = { Text("Password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color(0xFF8B948E)
                )
            },
            trailingIcon = {
                Text(
                    text = if (passwordVisible) "Hide" else "Show",
                    color = Color(0xFFC7EED8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { passwordVisible = !passwordVisible }
                        .padding(horizontal = 12.dp)
                )
            },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = ForestGreen,
                unfocusedBorderColor = DarkAuthBorder,
                focusedLabelColor = ForestGreen,
                unfocusedLabelColor = Color(0xFF8B948E),
                cursorColor = ForestGreen
            ),
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { submitEmailLogin() }
            ),
            modifier = Modifier.fillMaxWidth()
        )

        val activeError = validationError ?: (uiState as? LoginUiState.Error)?.message
        if (activeError != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = activeError,
                color = Color(0xFFE5989B),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { submitEmailLogin() },
            enabled = uiState !is LoginUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ForestGreen,
                contentColor = Color.White
            )
        ) {
            if (uiState is LoginUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Sign in",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = DarkAuthBorder)
            Text(
                text = "or",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color(0xFF8B948E),
                fontSize = 13.sp
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = DarkAuthBorder)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                ForkfulLogger.logAction("LOGIN", "Continue with Google tapped")
                launchGoogleSignIn()
            },
            enabled = uiState !is LoginUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF1E211E)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "G",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = Color(0xFF4285F4)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Continue with Google",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                color = Color(0xFF8B948E),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Sign up",
                color = Color(0xFFC7EED8),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onNavigateToSignUp() }
            )
        }
    }
}