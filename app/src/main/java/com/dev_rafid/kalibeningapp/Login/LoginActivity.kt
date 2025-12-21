package com.dev_rafid.kalibeningapp.Login

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dev_rafid.kalibeningapp.FragmentActivity
import com.dev_rafid.kalibeningapp.R
import com.dev_rafid.kalibeningapp.Register.RegisterActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.serialization.builtins.IntArraySerializer


class LoginActivity : AppCompatActivity() {

    // Inisialisasi Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient // Ini yang baru

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        window.statusBarColor = ContextCompat.getColor(this, R.color.warna_utama)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        // 1. Menghubungkan variabel dengan ID yang ada/sudah dibuat di bagian file XML (activity_login.xml)
        val etEmail = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnMasuk = findViewById<Button>(R.id.btn_masuk)
        val tvRegister = findViewById<TextView>(R.id.tv_register)
        val tvLupaPassword = findViewById<TextView>(R.id.tv_lupaPassword)

        // Kode Bagian yang menangani Lupa Password
        tvLupaPassword.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(
                    this,
                    "Masukkan email Anda di kolom Username/Email untuk reset password",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                // Fungsi Firebase untuk kirim email reset password
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task -> 
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Email reset password telah di kirim ke: ${email}",
                                Toast.LENGTH_LONG
                            ).show()
                        } else{
                            Toast.makeText(this,
                                "Gagal ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        // Konfigurasi Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Ini otomatis ada setelah ganti google-services.json
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val btnGoogle = findViewById<ImageView>(R.id.btn_google) // Pastikan ID di XML sudah sesuai
        btnGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            googleLauncher.launch(signInIntent)
        }

        // 2. Memberikan aksi ketika tombol Masuk di klik
        btnMasuk.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validasi sederhan: Cek apakah input kosong
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan Password tidak boleh kosong!!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
                // Login Menggunakan Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Login Berhasil!",
                            Toast.LENGTH_SHORT
                        ).show()
                        // Pindah ke halaman utama (KASIR) jika login sukses
                        val intent = Intent(this, FragmentActivity::class.java)
                        startActivity(intent)
                        finish()
                    }  else {
                        Toast.makeText(this,
                            "Login Gagal: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
        // 3. Aksi ketika user belum memiliki akun
        tvRegister.setOnClickListener {
            // Kode untuk pindah halaman ke halaman register
            Toast.makeText(this,
                "Pindah Halaman Register",
                Toast.LENGTH_SHORT
            ).show()
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }



    }
    // Launcher untuk menangani hasil pilihan akun Google user
    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                // Kirim token Google ke Firebase
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fungsi untuk menukarkan token Google dengan kredensial Firebase
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid
                    val db = FirebaseFirestore.getInstance()

                    if (userId != null) {
                        // 1. Cek terlebih dahulu apakah data user sudah ada di firestore
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                if (!document.exists()) {
                                    // 2 Jika belum ada (user baru via google), simpan datanya
                                    val userData = hashMapOf(
                                        "nama" to user.displayName, // Digunakan untuk mengambil nama dari akun Google
                                        "email" to user.email,
                                        "role" to "admin kasir:" // Default Role
                                    )

                                    db.collection("users").document(userId).set(userData)
                                        .addOnSuccessListener {
                                            pindahKeDashboard()
                                        }
                                } else {
                                    // 3. Jika sudah ada, langsung masuk aja
                                    pindahKeDashboard()
                                }
                            }
                    }else {
                        Toast.makeText(this, "Autentikasi Firebase Gagal.", Toast.LENGTH_SHORT).show()

                }
                }
            }

    }
    // Fungsi tambahan agar kode lebih rapi
    private fun pindahKeDashboard(){
        Toast.makeText(
            this,
            "Login Berhasil",
            Toast.LENGTH_SHORT
        ).show()
        val intent = Intent(
            this,
            FragmentActivity::class.java
        )
        startActivity(intent)
        finish()
    }

    // Kode ini untuk ketika User sudah melakukan Registrasi dan data dari User tersebut sudah tersimpan di Firebase maka +
    // User tidak perlu untuk melakukan Login ulang jadi langsung masuk ke dalam aplikasinya (halaman Dashboard/Kasir)
    override fun onStart() {
        super.onStart()
        // Cek apakah ada user yang sedang login
        val currentUser = auth.currentUser
        if (currentUser !=null) {
            // Jika ada User (sudah login sebelumnya), maka langsung pindah ke halaman Dashboard/Kasir
            val intent = Intent(this, FragmentActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}