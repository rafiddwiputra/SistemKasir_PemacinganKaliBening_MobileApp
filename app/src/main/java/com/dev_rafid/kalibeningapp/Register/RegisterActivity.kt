package com.dev_rafid.kalibeningapp.Register

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dev_rafid.kalibeningapp.R

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 1. Inisialisasi View Berdasarkan ID pada layout XML file (activity_registrasi.xml)
        val etEmail = findViewById<EditText>(R.id.et_reg_email)
        val etPassword = findViewById<EditText>(R.id.et_reg_password)
        val etKonfirmPassword = findViewById<EditText>(R.id.et_reg_konfirmPassword)
        val etUsername = findViewById<EditText>(R.id.et_reg_namaPengguna)
        val btnDaftar = findViewById<Button>(R.id.btn_daftar_akun)

        // 2. Logika Ketika Button Daftar Di Klik
        btnDaftar.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()
            val konfirmPassword = etKonfirmPassword.text.toString()
            val username = etUsername.text.toString()

            // Validasi Untuk Mengecek Apakah Ada Kolom Yang Kosong
            if (email.isEmpty() || password.isEmpty() || konfirmPassword.isEmpty() || username.isEmpty()) {
                Toast.makeText(
                    this,
                    "Semua kolom harus diisi!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            // Validasi: Cek apakah password dan konfirmasi cocok
            else if (password != konfirmPassword) {
                Toast.makeText(
                    this,
                    "Password tidak cocok!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                // Simulasi pendaftaran berhasil
                Toast.makeText(
                    this,
                    "Pendaftaran Berhasil untuk $username",
                    Toast.LENGTH_LONG
                ).show()

                finish() // Digunakan untuk kembali ke halaman login
            }
        }
    }
}