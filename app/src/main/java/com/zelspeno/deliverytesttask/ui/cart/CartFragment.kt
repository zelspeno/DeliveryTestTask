package com.zelspeno.deliverytesttask.ui.cart

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.zelspeno.deliverytesttask.R
import com.zelspeno.deliverytesttask.databinding.FragmentCartBinding
import com.zelspeno.deliverytesttask.databinding.FragmentCategoriesBinding
import com.zelspeno.deliverytesttask.source.CartDish
import com.zelspeno.deliverytesttask.source.Category
import com.zelspeno.deliverytesttask.source.Dish
import com.zelspeno.deliverytesttask.ui.home.CategoriesListRecyclerAdapter
import com.zelspeno.deliverytesttask.ui.home.DishesListRecyclerAdapter
import com.zelspeno.deliverytesttask.ui.home.HomeViewModel
import com.zelspeno.deliverytesttask.ui.home.TagsListRecyclerAdapter
import com.zelspeno.deliverytesttask.utils.viewModelCreator
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null

    private val binding get() = _binding!!

    private val viewModel by viewModelCreator { HomeViewModel() }

    private var sharedPref: SharedPreferences? = null

    private lateinit var adapter: CartListRecyclerAdapter

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

        _binding = FragmentCartBinding.inflate(inflater, container, false)

        sharedPref = activity?.getSharedPreferences("cart", Context.MODE_PRIVATE)

        binding.cartUserDate.text = viewModel.getUIDateTimeFromUnix(viewModel.getCurrentUnixTime())

        fillUI()

        initGPSLauncher()
        launcherGPS.launch(permList)

        return binding.root
    }

    /** Init settings for CartRecyclerView */
    private fun sendDataToCartRecyclerView(v: View?, adapterRV: CartListRecyclerAdapter) {
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
                            sharedPref, requireView(), viewModel.checkDishesOnErrors(dishes)
                        )
                        adapter = CartListRecyclerAdapter(cartDishes)
                        sendDataToCartRecyclerView(view, adapter)

                    } else {
                        Toast
                            .makeText(context, getString(R.string.checkInternetConnection), Toast.LENGTH_SHORT)
                            .show()
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
                val cityName = viewModel.getCityName(requireContext()) ?: "Неизвестно"
                binding.cartUserCity.text = cityName

            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.updateSharedPreferences(activity, adapter)
    }
}