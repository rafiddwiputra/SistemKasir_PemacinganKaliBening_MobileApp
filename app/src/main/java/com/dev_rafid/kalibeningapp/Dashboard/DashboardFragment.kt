package com.dev_rafid.kalibeningapp.Dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.dev_rafid.kalibeningapp.Profile.ProfileActivity
import com.dev_rafid.kalibeningapp.R
import android.util.Base64
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Merubah warna status bar
    override fun onResume() {
        super.onResume()

        val window = requireActivity().window

        // Warna background status bar
        window.statusBarColor =
            resources.getColor(R.color.warna_utama, null)

        // IKON STATUS BAR JADI PUTIH
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 1. Simpan hasil inflate ke dalam variabel 'view'
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        // Button Kategori Pemancing
        val btnPemancing = view.findViewById<View>(R.id.cv_KategoriPemancing)

        // Aksi Ketika Kategori Pemancing Di Klik
        btnPemancing?.setOnClickListener {

            findNavController().navigate(R.id.action_dashboardFragment_to_pemancingFragment)
        }

        // 2. Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        // 3. Hubungkan ke ID di XML (Pastikan ID tv_namaAdminKasir benar)
        val tvRole = view.findViewById<TextView>(R.id.tv_role)
        val tvNama = view.findViewById<TextView>(R.id.tv_namaAdminKasir)
        val btnEditProfile = view.findViewById<Button>(R.id.btn_editProfile)
        val ivProfileDashboard = view.findViewById<ImageView>(R.id.iv_profileDashboard)

        // Menggunakan SnapshootListener agar data bisa Update secara Real-Time
        if (userId !=null) {
            db.collection("users").document(userId)
                .addSnapshotListener { snapshoot, e ->
                    if (e !=null ) {
                        // Tangani error jika ada
                        return@addSnapshotListener
                    }

                    if (snapshoot !=null && snapshoot.exists() ) {
                        val nama = snapshoot.getString("nama")
                        val role = snapshoot.getString("role")
                        val photoBase64 =snapshoot.getString("fotoUrl") // Digunakan untuk mengambil data foto

                        // Update tampilan secara otomatis saat data di Firebase berubah
                        tvNama.text = nama
                        tvRole.text = role?.replaceFirstChar { it.uppercase() }

                        // Update foto profile menggunakan Glide
                        if (!photoBase64.isNullOrEmpty()) {
                            try {
                                val imageBytes = Base64.decode(photoBase64, Base64.DEFAULT)

                                Glide.with(this)
                                    .asBitmap()
                                    .load(imageBytes)
                                    .placeholder(R.drawable.ikon_user) // Gambar default ketika loading
                                    .error(R.drawable.logo_profile) // Gambar jika error
                                    .into(ivProfileDashboard) // ID ImageView yang ada di bagian dashboard
                        }catch (e: Exception){
                        ivProfileDashboard.setImageResource(R.drawable.ikon_user)
                        }
                        }else{
                            // Jika tidak ada foto di database, tampilkan ikon secara default
                            ivProfileDashboard.setImageResource(R.drawable.ikon_user)
                        }

                    }

                }
        }
        // Menghubungkan variabel dengan ID tombol di XML

        // 2. Tambahkan Listener untuk klik
        btnEditProfile.setOnClickListener {
            // Pindah ke ProfileActivity
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }
        // 4. Pindahkan return view ke baris paling terakhir
        return view
    }
}