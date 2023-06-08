package com.zelspeno.deliverytesttask.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
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
import com.zelspeno.deliverytesttask.R
import com.zelspeno.deliverytesttask.databinding.FragmentCategoriesBinding
import com.zelspeno.deliverytesttask.source.Category
import com.zelspeno.deliverytesttask.source.Dish
import com.zelspeno.deliverytesttask.source.Tag
import com.zelspeno.deliverytesttask.utils.viewModelCreator
import kotlinx.coroutines.launch

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null

    private val binding get() = _binding!!

    private lateinit var adapterDishes: DishesListRecyclerAdapter
    private lateinit var adapterTags: TagsListRecyclerAdapter

    private val viewModel by viewModelCreator { HomeViewModel() }

    private lateinit var allDishesList: List<Dish>

    private var sharedPref: SharedPreferences? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        val category = this.arguments?.getSerializable("category") as Category

        sharedPref = activity?.getSharedPreferences("cart", Context.MODE_PRIVATE)

        binding.categoriesHeaderName.text = category.name

        binding.categoriesBackButton.setOnClickListener {
            viewModel.moveToFragment(view, R.id.navigation_home)
        }

        binding.categoriesSwipeRefreshLayout.setOnRefreshListener {
            onSwipeUpdateList()
        }

        fillUI()

        return binding.root
    }

    /** Fill user's interface */
    private fun fillUI() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.getDishesList()
                viewModel.dishesList.collect {
                    val dishes = it
                    if (dishes != null) {
                        allDishesList = viewModel.checkDishesOnErrors(dishes)
                        val tags = viewModel.getTagsList(dishes)
                        adapterDishes = DishesListRecyclerAdapter(viewModel.checkDishesOnErrors(dishes))
                        adapterTags = TagsListRecyclerAdapter(tags)
                        sendDataToDishesRecyclerView(view, adapterDishes)
                        sendDataToTagsRecyclerView(view, adapterTags, adapterDishes)
                        binding.categoriesRecyclerViewsContainer.visibility = View.VISIBLE
                    } else {
                        binding.categoriesRecyclerViewNotFound.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    /** Init settings for DishesRecyclerView */
    private fun sendDataToDishesRecyclerView(v: View?, adapterRV: DishesListRecyclerAdapter) {
        val recyclerView = binding.categoriesRecyclerView
        with(recyclerView) {
            adapter = adapterRV
            layoutManager = GridLayoutManager(recyclerView.context, 3, GridLayoutManager.VERTICAL, false)
        }

        adapterRV.setOnItemClickListener(object :
            DishesListRecyclerAdapter.onItemClickListener {
            override fun onItemClick(dish: Dish) {
                viewModel.showDishDialog(v!!.context, dish, sharedPref)
            }
        })
    }

    /** Init settings for TagsRecyclerView */
    private fun sendDataToTagsRecyclerView(v: View?,
                                           adapterTags: TagsListRecyclerAdapter,
                                           adapterDishes: DishesListRecyclerAdapter) {
        val recyclerView = binding.tagsRecyclerView
        with(recyclerView) {
            adapter = adapterTags
            layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.HORIZONTAL, false)
        }

        adapterTags.setOnItemClickListener(object :
            TagsListRecyclerAdapter.onItemClickListener {
            override fun onItemClick(tag: Tag) {
                viewModel.onTagClick(tag, allDishesList, adapterTags, adapterDishes)
            }
        })
    }

    /** Update data on RecyclerView when user pull-to-refresh */
    private fun onSwipeUpdateList() {
        with(binding) {
            categoriesRecyclerViewsContainer.visibility = View.GONE
            categoriesRecyclerViewNotFound.visibility = View.GONE
            viewLifecycleOwner.lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    viewModel.getDishesList()
                    viewModel.dishesList.collect {
                        val dishes = it
                        if (dishes != null) {
                            allDishesList = viewModel.checkDishesOnErrors(dishes)
                            val tags = viewModel.getTagsList(dishes)
                            adapterDishes.updateList(allDishesList)
                            adapterTags.updateList(tags)
                            categoriesSwipeRefreshLayout.isRefreshing = false
                            categoriesRecyclerViewsContainer.visibility = View.VISIBLE
                        } else {
                            categoriesSwipeRefreshLayout.isRefreshing = false
                            categoriesRecyclerViewNotFound.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }
}