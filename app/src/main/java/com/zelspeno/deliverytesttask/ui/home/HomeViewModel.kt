package com.zelspeno.deliverytesttask.ui.home

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.squareup.picasso.Picasso
import com.zelspeno.deliverytesttask.R
import com.zelspeno.deliverytesttask.source.*
import com.zelspeno.deliverytesttask.ui.cart.CartListRecyclerAdapter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import dagger.Lazy

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ProductRepository,
    @Named("cityName") private val cityName: Lazy<String?>,
    private val cartSharedPref: SharedPreferences?
    ) : ViewModel() {

    private val _categoriesList = MutableSharedFlow<List<Category>?>()
    val categoriesList = _categoriesList.asSharedFlow()

    private val _dishesList = MutableSharedFlow<List<Dish>?>()
    val dishesList = _dishesList.asSharedFlow()

    /** Emit values to [categoriesList] */
    fun getCategoriesList() {

        val response: Call<CategoriesJSONObject> = repository.getCategoriesJson()

        response.enqueue(object: Callback<CategoriesJSONObject> {
            override fun onResponse(call: Call<CategoriesJSONObject>, response: Response<CategoriesJSONObject>) {
                viewModelScope.launch {
                    _categoriesList.emit(response.body()!!.categories)
                }
            }

            override fun onFailure(call: Call<CategoriesJSONObject>, t: Throwable) {
                viewModelScope.launch {
                    Log.d("getCategoriesListOnFailureLoad", t.toString())
                    _categoriesList.emit(null)
                }
            }
        })
    }

    /** Emit values to [dishesList] */
    fun getDishesList() {

        val response: Call<DishesJSONObject> = repository.getDishesJson()

        response.enqueue(object: Callback<DishesJSONObject> {
            override fun onResponse(call: Call<DishesJSONObject>, response: Response<DishesJSONObject>) {
                viewModelScope.launch {
                    _dishesList.emit(response.body()!!.dishes)
                }
            }

            override fun onFailure(call: Call<DishesJSONObject>, t: Throwable) {
                viewModelScope.launch {
                    Log.d("getDishesListOnFailureLoad", t.toString())
                    _dishesList.emit(null)
                }
            }
        })
    }

    /** Check [dishes] on null-objects */
    fun checkDishesOnErrors(dishes: List<Dish>): List<Dish> {
        val res = dishes.toMutableList()
        for (i in dishes.indices) {
            if (dishes[i].id == null || dishes[i].name == null || dishes[i].price == null ||
                dishes[i].weight == null || dishes[i].description == null ||
                dishes[i].imageUrl == null || dishes[i].tags == null) {
                res.removeAt(i)
            }
        }
        return res
    }

    /** Move to Fragment by [resID] */
    fun moveToFragment(v: View?, resID: Int, bundle: Bundle? = null) {
        if (bundle == null) {
            v?.findNavController()?.navigate(resID)
        } else {
            v?.findNavController()?.navigate(resID, bundle)
        }
    }

    /** Get unix-type datetime (ex.1672531200) and convert it
     * to Human-type datetime(ex. 1 января, 2023) */
    fun getUIDateTimeFromUnix(unix: Long): String {
        val sdf = SimpleDateFormat(
            "dd MMMM, yyyy",
            Locale.forLanguageTag("ru-RU")
        )
        return sdf.format(unix * 1000)
    }

    /** Get [List] of unique [Dish.tags] */
    fun getTagsList(dishes: List<Dish>): List<Tag> {
        val res = mutableListOf<Tag>()
        for (i in dishes) {
            val tag = i.tags
            for (j in tag!!) {
                if (j !in res.map { it.name }) {
                    if (j == "Все меню") {
                        res.add(Tag(j, true))
                    } else {
                        res.add(Tag(j, false))
                    }
                }
            }
        }
        return res
    }

    /** Get List<[Dish]> by [tag] */
    private fun getDishesByTag(dishes: List<Dish>, tag: Tag): List<Dish> {
        val res = mutableListOf<Dish>()
        for (i in dishes) {
            for (j in i.tags!!) {
                if (tag.name == j) {
                    if (i !in res) {
                        res.add(i)
                    }
                }
            }
        }
        return res
    }

    /** Prepare [dishes] to send on RecyclerView */
    fun prepareDishesToCartRecyclerView(dishes: List<Dish>)
        : List<CartDish> {
        val res = mutableListOf<CartDish>()
        if (cartSharedPref != null) {
            for (i in cartSharedPref.all) {
                for (j in dishes) {
                    if (i.key.toLong() == j.id) {
                        res.add(
                            CartDish(
                                id = j.id,
                                name = j.name,
                                price = j.price?.toUIText(),
                                weight = j.weight?.toUIText(),
                                imageUrl = j.imageUrl,
                                count = i.value.toString().toInt()
                            )
                        )
                    }
                }
            }
        }
        return res
    }

     private fun Double.toUIText(): String {
        return if (this.toInt() - this > 0.0) {
            this.toString()
        } else this.toInt().toString()
    }

    /** Update shared preferences */
    fun updateSharedPreferences(adapter: CartListRecyclerAdapter) {
        cartSharedPref?.edit()?.clear()?.apply()
        val list = adapter.getList()
        for (i in list) {
            cartSharedPref?.edit()?.putInt("${i.id}", i.count!!)?.apply()
        }
    }

    /** Show dish Dialog */
    fun showDishDialog(context: Context, dish: Dish) {
        val dialog = Dialog(context)
        with(dialog) {
            setContentView(R.layout.dialog_dish)
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(true)
        }

        val buttonAdd = dialog.findViewById<Button>(R.id.dialogDishAddToCart)
        val buttonClose = dialog.findViewById<ImageView>(R.id.dialogDishClose)
        val dishImage = dialog.findViewById<ImageView>(R.id.dialogDishImage)
        val name = dialog.findViewById<TextView>(R.id.dialogDishName)
        val cost = dialog.findViewById<TextView>(R.id.dialogDishCost)
        val weight = dialog.findViewById<TextView>(R.id.dialogDishWeight)
        val description = dialog.findViewById<TextView>(R.id.dialogDishDescription)

        buttonAdd.setOnClickListener {
            cartSharedPref?.edit()?.putInt("${dish.id}", 1)?.apply()
            Toast
                .makeText(context, "Блюдо '${dish.name}' добавлено в корзину", Toast.LENGTH_SHORT)
                .show()
        }

        buttonClose.setOnClickListener {
            dialog.dismiss()
        }

        if (dish.imageUrl == null || dish.imageUrl == "null") {
            dishImage.setImageDrawable(context.resources.getDrawable(R.drawable.empty_image))
        } else {
            Picasso.get().load(dish.imageUrl)
                .error(R.drawable.empty_image)
                .into(dishImage)
        }
        name.text = dish.name
        cost.text = "${dish.price!!.toUIText()} ₽"
        weight.text = "· ${dish.weight!!.toUIText()}г"
        description.text = dish.description

        dialog.show()
    }

    /** On [tag] clicked logic */
    fun onTagClick(
        tag: Tag,
        allDishesList: List<Dish>,
        adapterTags: TagsListRecyclerAdapter,
        adapterDishes: DishesListRecyclerAdapter,
    ) {
        val list = adapterTags.getList()
        val newList = mutableListOf<Tag>()
        for (i in list) {
            if (i !in newList) {
                if (i == tag) {
                    newList.add(Tag(i.name, true))
                } else {
                    newList.add(Tag(i.name, false))
                }
            }
        }
        adapterTags.updateList(newList)
        val dishes = getDishesByTag(allDishesList, tag)
        adapterDishes.updateList(dishes)
    }

    /** Return user's cityName
     * onFail - return null */
    fun getCityName(): Lazy<String?> = cityName

    /** Get GPS Dialog */
    fun getGPSDialog(context: Context): Dialog {
        val dialog = Dialog(context)
        with(dialog) {
            setContentView(R.layout.dialog_turn_gps)
            window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setCanceledOnTouchOutside(false)
        }
        return dialog
    }

    /** Return current UNIX-time */
    fun getCurrentUnixTime() = System.currentTimeMillis() / 1000
}