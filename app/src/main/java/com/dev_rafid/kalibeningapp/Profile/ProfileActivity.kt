package com.dev_rafid.kalibeningapp.Profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide // Import library asli untuk pengolahan gambar
import com.dev_rafid.kalibeningapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileActivity : AppCompatActivity() {

    // Variabel untuk menampung data gambar sementara (Bitmap)
    private var selectedBitmap: Bitmap? = null

    // Inisialisasi Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // 1. Launcher untuk mengambil foto dari Galeri HP
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                selectedBitmap = bitmap
                // Tampilkan preview di ImageView profil
                findViewById<ImageView>(R.id.iv_userProfile).setImageBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal mengambil gambar dari galeri", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 2. Launcher untuk mengambil foto langsung dari Kamera
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val bitmap = result.data?.extras?.get("data") as? Bitmap
            if (bitmap != null) {
                selectedBitmap = bitmap
                // Tampilkan preview di ImageView profil
                findViewById<ImageView>(R.id.iv_userProfile).setImageBitmap(bitmap)
            }
        }
    }

    // 3. Launcher untuk meminta izin penggunaan kamera
    private val requestCameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(cameraIntent)
        } else {
            Toast.makeText(this, "Izin kamera diperlukan untuk mengambil foto", Toast.LENGTH_SHORT).show()
        }
    }

    // Fungsi yang digunakan untuk menampilkan foto profil ketika diklik
    private fun tampilkanGambarFull(imageView: ImageView) {
        // Mengambil gambar dari ImageView yang sedang tampil
        val drawable=imageView.drawable?: return

        // Membuat ImageView baru untuk di dalam dialog
        val fullImageView = ImageView(this)
        fullImageView.setImageDrawable(drawable)
        fullImageView.adjustViewBounds=true

        // Membuat Dialog
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setView(fullImageView)

        val dialog = builder.create()

        // Hilangkan background putih bawaan dialog agar terlihat bersih
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialog.show()

        // Tutup dialog saat gambar diklik lagi
        fullImageView.setOnClickListener {
            dialog.dismiss()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_profile)

        // Mengatur warna status bar agar senada dengan tema aplikasi
        window.statusBarColor = ContextCompat.getColor(this, R.color.warna_utama)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inisialisasi layanan Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        // Inisialisasi komponen UI dari XML
        val btnkembali = findViewById<Button>(R.id.btn_Kembali)
        val btnKeluar = findViewById<Button>(R.id.btn_keluarAkun)
        val btnSimpan = findViewById<Button>(R.id.btn_simpanProfile)
        val ivEdit = findViewById<ImageView>(R.id.iv_editProfile)
        val tvNamaHeader = findViewById<TextView>(R.id.tv_namaAdminProfile)
        val etNamaUser = findViewById<EditText>(R.id.et_namaUserProfile)
        val tvEmailUser = findViewById<TextView>(R.id.tv_emailUserProfile)
        val ivKamera = findViewById<ImageView>(R.id.iv_kamera)
        val ivProfile = findViewById<ImageView>(R.id.iv_userProfile)

        // Mengambil data profil terbaru dari Firestore saat halaman dibuka
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nama = document.getString("nama")
                        val email = document.getString("email")
                        val photoBase64 = document.getString("fotoUrl")

                        tvNamaHeader.text = nama
                        etNamaUser.setText(nama)
                        tvEmailUser.text = email

                        // Jika ada data foto (Base64), muat menggunakan library Glide
                        if (!photoBase64.isNullOrEmpty()) {
                            try {
                                val imageBytes = Base64.decode(photoBase64, Base64.DEFAULT)
                                Glide.with(this)
                                    .asBitmap()
                                    .load(imageBytes)
                                    .placeholder(R.drawable.ikon_user)
                                    .into(ivProfile)
                            } catch (e: Exception) {
                                ivProfile.setImageResource(R.drawable.ikon_user)
                            }
                        }
                    }
                }
        }

        val ivProfil = findViewById<ImageView>(R.id.iv_userProfile)
        ivProfile.setOnClickListener {
            tampilkanGambarFull(ivProfil)
        }


        // Aksi saat ikon "Pensil" atau Edit diklik
        ivEdit.setOnClickListener {
            etNamaUser.isEnabled = true
            etNamaUser.requestFocus()

            // Menampilkan keyboard secara otomatis
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(etNamaUser, InputMethodManager.SHOW_IMPLICIT)

            // Mengatur visibilitas tombol
            btnkembali.visibility = View.GONE
            btnKeluar.visibility = View.GONE
            btnSimpan.visibility = View.VISIBLE

            Toast.makeText(this, "Mode Edit Aktif", Toast.LENGTH_SHORT).show()
        }

        // Aksi saat tombol Simpan diklik
        btnSimpan.setOnClickListener {
            val namaBaru = etNamaUser.text.toString().trim()
            if (namaBaru.isNotEmpty() && userId != null) {
                saveProfileToFirestore(namaBaru, userId, tvNamaHeader, etNamaUser, btnSimpan, btnkembali, btnKeluar)
            } else {
                Toast.makeText(this, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            }
        }

        btnkembali.setOnClickListener { finish() }

        // Aksi saat ikon kamera kecil diklik untuk mengganti foto
        ivKamera.setOnClickListener {
            val opsi = arrayOf("Kamera", "Galeri")
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Pilih Sumber Foto")
                .setItems(opsi) { _, which ->
                    when(which) {
                        0 -> { // Memilih Kamera
                            val permission = android.Manifest.permission.CAMERA
                            if (ContextCompat.checkSelfPermission(this, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                cameraLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
                            } else {
                                requestCameraPermissionLauncher.launch(permission)
                            }
                        }
                        1 -> galleryLauncher.launch("image/*") // Memilih Galeri
                    }
                }
                .show()
        }

        // Fungsi untuk keluar dari halaman profile dengan klik button
        btnKeluar.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Keluar Akun")
                .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
                .setPositiveButton("Ya") { _, _ ->
                    auth.signOut()
                    val intent = Intent(this, com.dev_rafid.kalibeningapp.Login.LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Batal", null)
                .show()
        }
    }


    // Fungsi utama untuk mengunggah perubahan ke Firestore
    private fun saveProfileToFirestore(
        nama: String,
        uid: String,
        header: TextView,
        input: EditText,
        btnSimpan: View,
        btnKembali: View,
        btnKeluar: View
    ) {
        val progressDialog = android.app.ProgressDialog(this)
        progressDialog.setMessage("Sedang menyimpan...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val dataUpdates = mutableMapOf<String, Any>("nama" to nama)

        // Jika user memilih foto baru, konversi ke Base64 sebelum disimpan
        selectedBitmap?.let { bitmap ->
            val base64String = encodeBitmapToBase64(bitmap)
            dataUpdates["fotoUrl"] = base64String
        }

        db.collection("users").document(uid).update(dataUpdates)
            .addOnSuccessListener {
                progressDialog.dismiss()
                header.text = nama
                input.isEnabled = false
                btnSimpan.visibility = View.GONE
                btnKembali.visibility = View.VISIBLE
                btnKeluar.visibility = View.VISIBLE
                Toast.makeText(this, "Profil berhasil diperbarui!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Mengonversi objek Gambar (Bitmap) menjadi Teks (Base64 String)
    private fun encodeBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        // Kompres gambar ke 50% untuk efisiensi penyimpanan Firestore (limit 1MB)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }
}