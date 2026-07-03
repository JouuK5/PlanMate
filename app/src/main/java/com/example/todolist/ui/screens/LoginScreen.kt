package com.example.todolist.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.ui.theme.CyanPrimary
import com.example.todolist.ui.viewmodels.AuthState
import com.example.todolist.ui.viewmodels.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.play.integrity.internal.al
import com.example.todolist.R

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToHome: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isLoginMode by remember { mutableStateOf(true) }

    val context = LocalContext.current

    // 1. Cấu hình yêu cầu lấy ID Token từ Google
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // default_web_client_id tự động được tạo từ file google-services.json
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    // 2. Trình khởi chạy cửa sổ Google Sign-In
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { token ->
                    // Gửi token về ViewModel để đăng nhập Firebase
                    authViewModel.loginWithGoogle(token)
                }
            } catch (e: ApiException) {
                // Xử lý lỗi nếu người dùng hủy hoặc lỗi mạng
                authViewModel.resetState() // Trả UI về bình thường
            }
        }
    }

    // Theo dõi trạng thái từ Firebase
    val authState by authViewModel.authState.collectAsState()

    // Nếu đăng nhập/đăng ký thành công thì chuyển trang
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onNavigateToHome()
            authViewModel.resetState() // Reset lại để an toàn
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "PlanMate xin chào,",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.8f)
        )
        Text(
            text = "Đăng nhập để đồng bộ dữ liệu",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Ô nhập Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("User@gmail.com") },
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Ô nhập Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            visualTransformation = PasswordVisualTransformation(), // Ẩn pass thành dấu *
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Hiển thị thông báo lỗi (Nếu có)
        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = Color.Red,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nút Đăng nhập
        Button(
            onClick = {
                if (isLoginMode) {
                authViewModel.login(email, password)
                } else {
                authViewModel.register(email, password)
                }
                      },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary),
            enabled = authState !is AuthState.Loading // Khóa nút khi đang load
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = if (isLoginMode) "Đăng nhập" else "Tạo tài khoản",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // Xóa trạng thái đăng nhập cũ (nếu có) để ép máy hiện lại bảng chọn Tài khoản
                googleSignInClient.signOut().addOnCompleteListener {
                    launcher.launch(googleSignInClient.signInIntent)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Đăng nhập bằng Google")
        }

        // Nút Đăng ký (TextButton cho nhẹ nhàng)
        TextButton(
            onClick = { isLoginMode = !isLoginMode // Đảo ngược trạng thái
                authViewModel.resetState() },
            enabled = authState !is AuthState.Loading
        ) {
            Text(text = if (isLoginMode) "Chưa có tài khoản? Đăng ký ngay" else "Đã có tài khoản? Đăng nhập",)
        }
    }
}