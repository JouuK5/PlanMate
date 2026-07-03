package com.example.todolist.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.repository.TodoRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Lớp đại diện cho các trạng thái của màn hình Login
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val todoRepo: TodoRepo
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)

    private val _currentUser = MutableStateFlow(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    // Kiểm tra xem user đã đăng nhập trước đó chưa
    fun isUserAuthenticated(): Boolean {
        return auth.currentUser != null
    }

    // Hàm Đăng nhập
    fun login(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            _authState.value = AuthState.Error("Email và mật khẩu không được để trống!")
            return
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModelScope.launch {
                        _currentUser.value = auth.currentUser
                        todoRepo.syncDataFromFirestore() // Kéo data về máy
                        _authState.value = AuthState.Success // Đẩy sang màn hình chính
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Đăng nhập thất bại")
                }
            }
    }

    // Hàm Đăng ký
    fun register(email: String, pass: String) {
        if (email.isEmpty() || pass.isEmpty()) {
            _authState.value = AuthState.Error("Email và mật khẩu không được để trống!")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModelScope.launch {
                        _currentUser.value = auth.currentUser
                        todoRepo.syncDataFromFirestore()
                        _authState.value = AuthState.Success
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Đăng ký thất bại")
                }
            }
    }

    // Reset trạng thái sau khi hiển thị lỗi xong
    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun logout(){
        auth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Idle

        viewModelScope.launch {
            todoRepo.clearLocalDatabase()
        }
    }
    // Hàm xử lý Đăng nhập bằng Google
    fun loginWithGoogle(idToken: String) {
        _authState.value = AuthState.Loading

        // Đổi ID Token của Google lấy Credential của Firebase
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    viewModelScope.launch {
                        // Kế thừa luồng cũ: Đẩy dữ liệu Khách lên Cloud trước
                    //    todoRepo.syncGuestDataToCloud()

                        // Kéo dữ liệu trên Cloud (nếu có) về máy
                        todoRepo.syncDataFromFirestore()

                        // Cập nhật trạng thái User cho Drawer biết
                        _currentUser.value = auth.currentUser
                        _authState.value = AuthState.Success
                    }
                } else {
                    _authState.value = AuthState.Error(task.exception?.localizedMessage ?: "Lỗi Google Sign In")
                }
            }
    }

}