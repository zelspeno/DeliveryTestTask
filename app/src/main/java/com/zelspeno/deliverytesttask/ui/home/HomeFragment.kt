package com.zelspeno.deliverytesttask.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.zelspeno.deliverytesttask.R
import com.zelspeno.deliverytesttask.databinding.FragmentHomeBinding
import com.zelspeno.deliverytesttask.source.Category
import com.zelspeno.deliverytesttask.utils.viewModelCreator
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapter: CategoriesListRecyclerAdapter

    private val viewModel by viewModelCreator { HomeViewModel() }

    private lateinit var launcherGPS: ActivityResultLauncher<Array<String>>

    private val permList = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.homeUserDate.text = viewModel.getUIDateTimeFromUnix(viewModel.getCurrentUnixTime())

        fillUI()

        initGPSLauncher()
        launcherGPS.launch(permList)

        return binding.root
    }

    /** Fill user's interface */
    private fun fillUI() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.getCategoriesList()
                viewModel.categoriesList.collect {
                    val categories = it
                    if (categories != null) {
                        adapter = CategoriesListRecyclerAdapter(categories)
                        sendDataToRecyclerView(view, adapter)
                    } else {
                        Toast
                            .makeText(context, getString(R.string.checkInternetConnection), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    /** Init settings for RecyclerView */
    private fun sendDataToRecyclerView(v: View?, adapterRV: CategoriesListRecyclerAdapter) {
        val recyclerView = binding.homeRecyclerView
        with(recyclerView) {
            adapter = adapterRV
            layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.VERTICAL, false)
        }

        adapterRV.setOnItemClickListener(object :
            CategoriesListRecyclerAdapter.onItemClickListener {
            override fun onItemClick(category: Category) {
                val bundle =
                    Bundle().apply { putSerializable("category", category) }
                viewModel.moveToFragment(v, R.id.navigation_categories, bundle)
            }
        })
    }

    /** Init GPS logic */
    private fun initGPSLauncher() {

        launcherGPS = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

            if (it.containsValue(false)) {

                val dialog = viewModel.getGPSDialog(requireContext())
                val buttonClose = dialog.findViewById<Button>(R.id.dialogGPSBodyCloseButton)
                val buttonSettings = dialog.findViewById<Button>(R.id.dialogGPSBodyMoveToSettingsButton)

                buttonClose.setOnClickListener {
                    dialog.dismiss()
                    launcherGPS.launch(permList)
                }

                buttonSettings.setOnClickListener {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }

                dialog.show()

            } else {
                val cityName = viewModel.getCityName(requireContext()) ?: "Неизвестно"
                binding.homeUserCity.text = cityName

            }
        }
    }

}