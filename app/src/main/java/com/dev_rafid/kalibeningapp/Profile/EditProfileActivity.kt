package com.dev_rafid.kalibeningapp.Profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.dev_rafid.kalibeningapp.R

class EditProfileActivity : AppCompatActivity() {

    private lateinit var imageUri: android.net.Uri
    private val galleryLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            findViewById<ImageView>(R.id.iv_userProfile).setImageURI(it)
            // Nanti di sini kita akan tambahkan fungsi upload ke Firebase storage
        }
    }

    private val cameraLauncher = registerForActivityResult (androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode ==android.app.Activity.RESULT_OK) {
            val bitmap=result.data?.extras?.get("data") as android.graphics.Bitmap
            findViewById<ImageView>(R.id.iv_userProfile).setImageBitmap(bitmap)
            // Nanti di sini kita akan tambahkan fungsi upload ke Firebase storage
        }
    }
    private val requestCameraPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Jika diizinkan, langsung buka kamera
            val cameraIntent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        } else {
            Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)
        window.statusBarColor = ContextCompat.getColor(this, R.color.warna_utama)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi Firebase
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        // Hubungkan dengan View XML
        val btnkembali = findViewById<Button>(R.id.btn_Kembali)
        val btnKeluar = findViewById<Button>(R.id.btn_keluarAkun)
        val btnSimpan = findViewById<Button>(R.id.btn_simpanProfile)
        val ivEdit = findViewById<ImageView>(R.id.iv_editProfile)
        val tvNamaHeader = findViewById<TextView>(R.id.tv_namaAdminProfile)
        val etNamaUser = findViewById<TextView>(R.id.et_namaUserProfile)
        val tvEmailUser = findViewById<TextView>(R.id.tv_emailUserProfile)
        val ivKamera = findViewById<ImageView>(R.id.iv_kamera)

        // Ambil data di Firebase
        if (userId != null){
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document !=null && document.exists()) {
                        val nama = document.getString("nama")
                        val email = document.getString("email")

                        tvNamaHeader.text = nama
                        etNamaUser.text = nama
                        tvEmailUser.text = email
                    }
                }
        }

        // Saat ikon Edit di klik
        ivEdit.setOnClickListener {

            // Aktifkan EditText agar bisa diketik
            etNamaUser.isEnabled = true
            etNamaUser.requestFocus() // otomatis fokus ke kotak teks

            // Tampilkan keyboard otomatis (opsional)
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(etNamaUser, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)

            // Tukar Tombol
            btnkembali.visibility=View.GONE
            btnKeluar.visibility = View.GONE
            // Tampilkan tombol simpan
            btnSimpan.visibility = View.VISIBLE

            Toast.makeText(
                this,
                "Mode Edit Aktif",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Aksi saat tombol Simpan di Klik
        btnSimpan.setOnClickListener {
            val namaBaru = etNamaUser.text.toString().trim()

            if (namaBaru.isNotEmpty()){
                if (userId !=null ) {
                    db.collection("users").document(userId)
                        .update("nama", namaBaru)
                        .addOnSuccessListener {
                            // Update header profil di bagian atas
                            findViewById<TextView>(R.id.tv_namaAdminProfile).text = namaBaru

                            // Kembalikan ke mode baca aja
                            etNamaUser.isEnabled = false
                            btnSimpan.visibility = View.GONE
                            btnkembali.visibility = View.VISIBLE
                            btnKeluar.visibility = View.VISIBLE

                            Toast.makeText(
                                this,
                                "Data berhasil di simpan!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Gagal: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
        }

        btnkembali.setOnClickListener {
            finish()
        }

        // Fungsi Kamera
        ivKamera.setOnClickListener {
            val opsi = arrayOf("Kamera", "Galeri")
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Pilih Foto Profil")
                .setItems(opsi) { _, which ->
                    when(which) {
                        0 -> { // Pilih Kamera
                            val permission = android.Manifest.permission.CAMERA
                            if (androidx.core.content.ContextCompat.checkSelfPermission(this, permission)
                                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                // Jika sudah pernah diizinkan
                                val cameraIntent = android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
                                cameraLauncher.launch(cameraIntent)
                            } else {
                                // Jika belum, minta izin ke user
                                requestCameraPermissionLauncher.launch(permission)
                            }
                        }
                        1 -> { // Pilih Galeri
                            galleryLauncher.launch("image/*")
                        }
                    }
                }
                .show()
        }
        btnKeluar.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Keluar Akun")
                .setMessage("Apakah Anda yakin ingin keluar?")
                .setPositiveButton("Ya") { _, _ ->
                    auth.signOut()
                    val intent = android.content.Intent(this,com.dev_rafid.kalibeningapp.Login.LoginActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Tidak") {dialog, _ ->
                    dialog.dismiss() //Menutup dialog jika batal keluar akun
                }
                .show()
        }
        }
    }
