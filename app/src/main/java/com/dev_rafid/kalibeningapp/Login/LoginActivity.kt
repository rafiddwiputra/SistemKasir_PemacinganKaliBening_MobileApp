package com.dev_rafid.kalibeningapp.Login

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dev_rafid.kalibeningapp.R


class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // 1. Menghubungkan variabel dengan ID yang ada/sudah dibuat di bagian file XML (activity_login.xml)
        val etUsername = findViewById<EditText>(R.id.et_username)
        val etPassword = findViewById<EditText>(R.id.et_password)
        val btnMasuk = findViewById<Button>(R.id.btn_masuk)
        val tvRegister = findViewById<TextView>(R.id.tv_register)

        // 2. Memberikan aksi ketika tombol Masuk di klik
        btnMasuk.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            // Validasi sederhan: Cek apakah input kosong
            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan Password tidak boleh kosong!!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // Contoh logika login yang sederhana (nanti bisa dihubungkan ke database/API)
                if (username == "admin" && password == "123123") {
                    Toast.makeText(this,
                        "Login Berhasil",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Pindah ke halaman utama (KASIR) jika login sukses
                    // val intent = Intent(this, MainActivity::class.java)
                    // startActivity(intent)
                } else {
                    Toast.makeText(this,
                        "Username atau Password salah!",
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
        }

    }
}