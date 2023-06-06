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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.zelspeno.deliverytesttask.R
import com.zelspeno.deliverytesttask.databinding.FragmentCategoriesBinding
import com.zelspeno.deliverytesttask.source.Category
import com.zelspeno.deliverytesttask.source.Dish
import com.zelspeno.deliverytesttask.utils.viewModelCreator
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapter: DishesListRecyclerAdapter

    private val viewModel by viewModelCreator { HomeViewModel() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        val category = this.arguments?.getSerializable("category") as Category

        binding.categoriesHeaderName.text = category.name

        binding.categoriesBackButton.setOnClickListener {
            viewModel.moveToFragment(view, R.id.navigation_home)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.getDishesList()
                viewModel.dishesList.collect {
                    val dishes = it
                    if (dishes != null) {
                        adapter = DishesListRecyclerAdapter(dishes)
                        sendDataToRecyclerView(view, adapter)
                    } else {
                        Toast
                            .makeText(context, getString(R.string.checkInternetConnection), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        return binding.root
    }

    /** Init settings for RecyclerView */
    private fun sendDataToRecyclerView(v: View?, adapterRV: DishesListRecyclerAdapter) {
        val recyclerView = binding.categoriesRecyclerView
        with(recyclerView) {
            adapter = adapterRV
            layoutManager = GridLayoutManager(recyclerView.context, 3, GridLayoutManager.VERTICAL, false)
        }

        adapterRV.setOnItemClickListener(object :
            DishesListRecyclerAdapter.onItemClickListener {
            override fun onItemClick(dish: Dish) {
                Log.d("dish", dish.toString())
            }
        })
    }
}