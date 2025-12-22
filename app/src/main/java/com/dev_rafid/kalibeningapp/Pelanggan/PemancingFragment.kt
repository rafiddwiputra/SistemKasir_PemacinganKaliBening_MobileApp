package com.dev_rafid.kalibeningapp.Pelanggan

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dev_rafid.kalibeningapp.Model.Ikan
import com.dev_rafid.kalibeningapp.R
import com.google.firebase.firestore.FirebaseFirestore

class PemancingFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var ikanAdapter: IkanAdapter
    private val listIkan = mutableListOf<Ikan>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pemancing, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()

        val rvJenisIkan = view.findViewById<RecyclerView>(R.id.rvJenisIkan)
        val btnKembali = view.findViewById<Button>(R.id.btn_kembaliPemancing)

        ikanAdapter = IkanAdapter(listIkan) { ikan ->
            Toast.makeText(context, "Memilih: ${ikan.namaIkan}", Toast.LENGTH_SHORT).show()
        }

        rvJenisIkan.layoutManager = GridLayoutManager(context, 3)
        rvJenisIkan.adapter = ikanAdapter

        loadDataFromFirestore()

        btnKembali.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    private fun loadDataFromFirestore() {
        db.collection("produk_pemancing")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(context, "Gagal ambil data: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    listIkan.clear()
                    for (doc in snapshots) {
                        try {
                            val item = doc.toObject(Ikan::class.java)
                            if (item != null) {
                                item.idIkan = doc.id

                                // Gunakan .lowercase() agar filter tidak sensitif huruf besar/kecil
                                if (item.kategoriIkan.lowercase() == "ikan") {
                                    listIkan.add(item)
                                }
                            }
                        } catch (err: Exception) {
                            Log.e("FIRESTORE_ERROR", "Gagal mapping data: ${err.message}")
                        }
                    }
                    ikanAdapter.notifyDataSetChanged()
                }
            }
    }
}