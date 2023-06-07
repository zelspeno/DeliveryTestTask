package com.zelspeno.deliverytesttask.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val curTime = System.currentTimeMillis() / 1000

        binding.homeUserDate.text = viewModel.getUIDateTimeFromUnix(curTime)

        fillUI()

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
}