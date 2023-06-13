package com.zelspeno.deliverytesttask.ui.home

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.zelspeno.deliverytesttask.R
import com.zelspeno.deliverytesttask.databinding.FragmentHomeBinding
import com.zelspeno.deliverytesttask.source.Category
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapter: CategoriesListRecyclerAdapter

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var launcherGPS: ActivityResultLauncher<Array<String>>

    @Inject
    @Named("GpsPermissions")
    lateinit var permList: Array<String>


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        binding.homeUserDate.text = viewModel.getUIDateTimeFromUnix(viewModel.getCurrentUnixTime())

        binding.homeSwipeRefreshLayout.setOnRefreshListener {
            onSwipeUpdateList()
        }

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
                        binding.homeRecyclerView.visibility = View.VISIBLE
                    } else {
                        binding.homeRecyclerViewNotFound.visibility = View.VISIBLE
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
                val cityName = viewModel.getCityName().get() ?: "Неизвестно"
                binding.homeUserCity.text = cityName

            }
        }
    }

    /** Update data on RecyclerView when user pull-to-refresh */
    private fun onSwipeUpdateList() {
        with(binding) {
            homeRecyclerView.visibility = View.GONE
            homeRecyclerViewNotFound.visibility = View.GONE
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    viewModel.getCategoriesList()
                    viewModel.categoriesList.collect {
                        val categories = it
                        if (categories != null) {
                            adapter.updateList(categories)
                            homeSwipeRefreshLayout.isRefreshing = false
                            homeRecyclerView.visibility = View.VISIBLE
                        } else {
                            homeSwipeRefreshLayout.isRefreshing = false
                            homeRecyclerViewNotFound.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }


}