package com.example.supabaseauth.ui

import android.R.attr.scaleX
import android.R.attr.scaleY
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supabaseauth.R
import com.example.supabaseauth.viewmodel.AuthUiState
import com.example.supabaseauth.viewmodel.AuthViewModel

// ───── COLORS ─────
private val Black = Color(0xFF181725)
private val Gray = Color(0xFF7C7C7C)
private val Blue = Color(0xFF00657E)
private val WhiteText = Color(0xFFF9F9F9)

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    authUiState: AuthUiState,
    onNavigateToLogin: () -> Unit
) {

    var nama by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible by remember { mutableStateOf(false) }

    val isValid =
        nama.isNotBlank() &&
                email.contains("@") &&
                password.length >= 8 &&
                password == confirmPassword

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp)
    ) {


        /* ===========LOGO=========== */
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)          // keep layout space small
                    .graphicsLayer {
                        scaleX = 1.5f      // visually bigger
                        scaleY = 1.5f
                    }
            )
        }


        // ───── TITLE ─────
        Text(
            text = "Register",
            color = Black,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Create your new account",
            color = Gray,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal
        )

        Spacer(Modifier.height(32.dp))

        // ───── NAME ─────
        Text(
            text = "Name",
            color = Gray,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))

        TextField(
            value = nama,
            onValueChange = { nama = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(
                color = Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Blue,
                unfocusedIndicatorColor = Gray,
                cursorColor = Blue
            )
        )

        Spacer(Modifier.height(20.dp))

        // ───── EMAIL ─────
        Text(
            text = "Email",
            color = Gray,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = TextStyle(
                color = Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Blue,
                unfocusedIndicatorColor = Gray,
                cursorColor = Blue
            )
        )

        Spacer(Modifier.height(20.dp))

        // ───── PASSWORD ─────
        Text(
            text = "Password",
            color = Gray,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation =
                if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),

            trailingIcon = {
                Icon(
                    imageVector =
                        if (passwordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        passwordVisible = !passwordVisible
                    }
                )
            },

            textStyle = TextStyle(
                color = Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),

            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Blue,
                unfocusedIndicatorColor = Gray,
                cursorColor = Blue
            )
        )

        Spacer(Modifier.height(20.dp))

        // ───── CONFIRM PASSWORD ─────
        Text(
            text = "Confirm Password",
            color = Gray,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.height(6.dp))

        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation =
                if (confirmVisible) VisualTransformation.None
                else PasswordVisualTransformation(),

            trailingIcon = {
                Icon(
                    imageVector =
                        if (confirmVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    modifier = Modifier.clickable {
                        confirmVisible = !confirmVisible
                    }
                )
            },

            textStyle = TextStyle(
                color = Black,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            ),

            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Blue,
                unfocusedIndicatorColor = Gray,
                cursorColor = Blue
            )
        )

        Spacer(Modifier.height(30.dp))

        // ───── REGISTER BUTTON ─────
        Button(
            onClick = {
                authViewModel.register(email, password, nama)
            },
            enabled = isValid && authUiState !is AuthUiState.Loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Blue
            )
        ) {
            if (authUiState is AuthUiState.Loading) {
                CircularProgressIndicator(color = WhiteText)
            } else {
                Text(
                    text = "Register",
                    color = WhiteText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ───── BACK TO LOGIN ─────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Already have an account? ",
                color = Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Login",
                color = Blue,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable {
                    onNavigateToLogin()
                }
            )
        }
    }
}