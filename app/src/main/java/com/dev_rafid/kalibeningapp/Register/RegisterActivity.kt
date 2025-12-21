package com.dev_rafid.kalibeningapp.Register

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dev_rafid.kalibeningapp.Login.LoginActivity
import com.dev_rafid.kalibeningapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    // Inisialisasi Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Kode Bagian Yang Menangani Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // 1. Inisialisasi View Berdasarkan ID pada layout XML file (activity_registrasi.xml)
        val etEmail = findViewById<EditText>(R.id.et_reg_email)
        val etPassword = findViewById<EditText>(R.id.et_reg_password)
        val etKonfirmPassword = findViewById<EditText>(R.id.et_reg_konfirmPassword)
        val etUsername = findViewById<EditText>(R.id.et_reg_namaPengguna)
        val btnDaftar = findViewById<Button>(R.id.btn_daftar_akun)

        // 2. Logika Ketika Button Daftar Di Klik
        btnDaftar.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val konfirmPassword = etKonfirmPassword.text.toString()
            val username = etUsername.text.toString()

            // Validasi Untuk Mengecek Apakah Ada Kolom Yang Kosong
            if (email.isEmpty() || password.isEmpty() || konfirmPassword.isEmpty() || username.isEmpty()) {
                Toast.makeText(
                    this,
                    "Semua kolom harus diisi!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Validasi: Cek apakah password dan konfirmasi cocok
            if (password != konfirmPassword) {
                Toast.makeText(
                    this,
                    "Password tidak cocok!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(
                    this,
                    "Password minimal 6 karakter!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Proses Daftar Ke Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Jika Auth sukses, simpan data tambahan ke Firestore
                        val userId = auth.currentUser?.uid
                        val userMap = hashMapOf(
                            "nama" to username,
                            "email" to email,
                            "role" to "admin kasir:"
                        )
                        if (userId != null) {
                            db.collection("users").document(userId)
                                .set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Pendaftaran Berhasil!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    startActivity(Intent(
                                        this, LoginActivity::class.java
                                    ))
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "Gagal simpan data: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Pendaftaran gagal: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}