package com.zelspeno.deliverytesttask.ui.cart

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.zelspeno.deliverytesttask.databinding.FragmentCartBinding
import com.zelspeno.deliverytesttask.source.CartDish
import com.zelspeno.deliverytesttask.ui.home.HomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@AndroidEntryPoint
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null

    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var adapter: CartListRecyclerAdapter

    private lateinit var launcherGPS: ActivityResultLauncher<Array<String>>

    @Inject
    @Named("GpsPermissions")
    lateinit var permList: Array<String>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCartBinding.inflate(inflater, container, false)

        binding.cartUserDate.text = viewModel.getUIDateTimeFromUnix(viewModel.getCurrentUnixTime())

        binding.cartSwipeRefreshLayout.setOnRefreshListener {
            onSwipeUpdateList()
        }

        fillUI()

        initGPSLauncher()
        launcherGPS.launch(permList)

        return binding.root
    }

    /** Init settings for CartRecyclerView */
    private fun sendDataToCartRecyclerView(adapterRV: CartListRecyclerAdapter) {
        val recyclerView = binding.cartRecyclerView
        with(recyclerView) {
            adapter = adapterRV
            layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.VERTICAL, false)
        }
        adapterRV.updateSumPay(binding.cartPay)
        adapterRV.setOnItemClickListener(object : CartListRecyclerAdapter.onItemClickListener {
            override fun onItemClick(cartDish: CartDish) {
                adapterRV.updateSumPay(binding.cartPay)
                val list = adapterRV.getList() as MutableList
                list.removeIf { it.count!! <= 0 }
                adapterRV.updateList(list)
            }
        })
    }

    /** Fill user's interface */
    private fun fillUI() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.getDishesList()
                viewModel.dishesList.collect {
                    val dishes = it
                    if (dishes != null) {
                        val cartDishes = viewModel.prepareDishesToCartRecyclerView(
                            viewModel.checkDishesOnErrors(dishes)
                        )
                        adapter = CartListRecyclerAdapter(cartDishes)
                        sendDataToCartRecyclerView(adapter)
                        binding.cartRecyclerView.visibility = View.VISIBLE

                    } else {
                        binding.cartRecyclerViewNotFound.visibility = View.VISIBLE
                    }
                }
            }
        }
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
                binding.cartUserCity.text = cityName

            }
        }
    }

    /** Update data on RecyclerView when user pull-to-refresh */
    private fun onSwipeUpdateList() {
        viewModel.updateSharedPreferences(adapter)
        with(binding) {
            cartRecyclerView.visibility = View.GONE
            cartRecyclerViewNotFound.visibility = View.GONE
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    viewModel.getDishesList()
                    viewModel.dishesList.collect {
                        val dishes = it
                        if (dishes != null) {
                            val cartDishes = viewModel.prepareDishesToCartRecyclerView(
                                viewModel.checkDishesOnErrors(dishes)
                            )
                            adapter.updateList(cartDishes)
                            cartSwipeRefreshLayout.isRefreshing = false
                            cartRecyclerView.visibility = View.VISIBLE

                        } else {
                            cartSwipeRefreshLayout.isRefreshing = false
                            cartRecyclerViewNotFound.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.updateSharedPreferences(adapter)
    }
}