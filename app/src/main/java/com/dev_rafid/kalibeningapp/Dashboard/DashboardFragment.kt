package com.dev_rafid.kalibeningapp.Dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.TextView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import com.dev_rafid.kalibeningapp.Profile.EditProfileActivity
import com.dev_rafid.kalibeningapp.R
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

        // 2. Inisialisasi Firebase
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid

        // 3. Hubungkan ke ID di XML (Pastikan ID tv_namaAdminKasir benar)
        val tvRole = view.findViewById<TextView>(R.id.tv_role)
        val tvNama = view.findViewById<TextView>(R.id.tv_namaAdminKasir)
        val btnEditProfile = view.findViewById<Button>(R.id.btn_editProfile)

        // Menggunakan SnapshootListener agar data bisa Update secara Real-Time
        if (userId !=null) {
            db.collection("users").document(userId)
                .addSnapshotListener { snapshoot, e ->
                    if (e !=null ) {
                        // Tangani error jika ada
                        return@addSnapshotListener
                    }
                    if (snapshoot !=null ) {
                        val nama = snapshoot.getString("nama")
                        val role = snapshoot.getString("role")

                        // Update tampilan secara otomatis saat data di Firebase berubah
                        tvNama.text = nama
                        tvRole.text = role?.replaceFirstChar { it.uppercase() }
                    }

                }
        }
        // Menghubungkan variabel dengan ID tombol di XML

        // 2. Tambahkan Listener untuk klik
        btnEditProfile.setOnClickListener {
            // Pindah ke EditProfileActivity
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }
        // 4. Pindahkan return view ke baris paling terakhir
        return view
    }
}