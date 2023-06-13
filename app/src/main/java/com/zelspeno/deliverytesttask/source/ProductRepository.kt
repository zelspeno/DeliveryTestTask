package com.zelspeno.deliverytesttask.source

import retrofit2.Call
import javax.inject.Inject

class ProductRepository @Inject constructor(private val api: DeliveryApi) {

    fun getCategoriesJson(): Call<CategoriesJSONObject> {
        return api.getCategoriesList(Const.CATEGORIES_URL)
    }

    fun getDishesJson(): Call<DishesJSONObject> {
        return api.getDishesList(Const.DISHES_URL)
    }

}