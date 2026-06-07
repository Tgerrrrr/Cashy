package com.example.supabaseauth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.supabaseauth.R
import com.example.supabaseauth.viewmodel.AuthUiState
import com.example.supabaseauth.viewmodel.AuthViewModel

private val Black    = Color(0xFF181725)
private val Gray     = Color(0xFF7C7C7C)
private val Blue     = Color(0xFF00657E)
private val WhiteText = Color(0xFFF9F9F9)

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onSignup: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val authUiState by authViewModel.authUiState.collectAsStateWithLifecycle()
    val isLoading = authUiState is AuthUiState.Loading
    val errorMessage = (authUiState as? AuthUiState.Error)?.message

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WhiteText)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                    contentDescription = "Logo",
                    modifier = Modifier.size(220.dp)
                )
            }

            Text("Login", color = Black, fontSize = 26.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Enter your email and password", color = Gray, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(32.dp))

            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text("Email", color = Gray, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = email,
                onValueChange = {
                    email = it
                    authViewModel.resetUiState()
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = TextStyle(color = Black, fontSize = 18.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Blue,
                    unfocusedIndicatorColor = Gray
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Password", color = Gray, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            TextField(
                value = password,
                onValueChange = {
                    password = it
                    authViewModel.resetUiState()
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                    )
                },
                textStyle = TextStyle(color = Black, fontSize = 18.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Blue,
                    unfocusedIndicatorColor = Gray
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    authViewModel.login(email.trim(), password)
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Log In", color = WhiteText, fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Center,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text("Don't have an account? ", color = Black, fontSize = 14.sp)
//                Text(
//                    text = "Signup",
//                    color = Blue,
//                    fontSize = 14.sp,
//                    modifier = Modifier.clickable { onSignup() }
//                )
//            }
        }
    }
}