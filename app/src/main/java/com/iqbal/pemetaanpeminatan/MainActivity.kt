package com.iqbal.pemetaanpeminatan // Sesuaikan dengan package Anda

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.iqbal.pemetaanpeminatan.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentMenuId = 0 // Memori untuk melacak menu mana yang sedang aktif

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setOnItemSelectedListener { item ->
            // Hanya pindah halaman JIKA ikon yang ditekan bukan halaman saat ini (Mencegah reload ganda)
            if (item.itemId != navController.currentDestination?.id) {
                navController.navigate(
                    item.itemId,
                    null,
                    androidx.navigation.NavOptions.Builder()
                        .setPopUpTo(navController.graph.startDestinationId, false)
                        .setLaunchSingleTop(true)
                        .build()
                )
            }
            true
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // ================= AREA MENU SISWA =================
                R.id.siswaFragment, R.id.profileFragment -> {
                    binding.bottomNav.visibility = View.VISIBLE

                    // Ganti struktur menu HANYA jika sebelumnya bukan menu siswa
                    if (currentMenuId != R.menu.bottom_nav_menu) {
                        binding.bottomNav.menu.clear()
                        binding.bottomNav.inflateMenu(R.menu.bottom_nav_menu)
                        currentMenuId = R.menu.bottom_nav_menu
                    }

                    // Kunci warna (highlight) pada ikon yang sedang aktif
                    binding.bottomNav.menu.findItem(destination.id)?.isChecked = true
                }

                // ================= AREA MENU ADMIN =================
                R.id.adminHomeFragment, R.id.adminKuotaFragment, R.id.adminKelasFragment, R.id.adminProfileFragment -> {
                    binding.bottomNav.visibility = View.VISIBLE

                    // Ganti struktur menu HANYA jika sebelumnya bukan menu admin
                    if (currentMenuId != R.menu.bottom_nav_admin) {
                        binding.bottomNav.menu.clear()
                        binding.bottomNav.inflateMenu(R.menu.bottom_nav_admin)
                        currentMenuId = R.menu.bottom_nav_admin
                    }

                    // Kunci warna (highlight) pada ikon yang sedang aktif
                    binding.bottomNav.menu.findItem(destination.id)?.isChecked = true
                }

                // ================= AREA TANPA BOTTOM NAV (Login/Register) =================
                else -> {
                    binding.bottomNav.visibility = View.GONE
                    currentMenuId = 0 // Reset memori
                }
            }
        }
    }
}