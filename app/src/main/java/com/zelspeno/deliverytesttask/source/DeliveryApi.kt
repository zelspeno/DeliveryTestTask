package com.zelspeno.deliverytesttask.source

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface DeliveryApi {

    @GET("{hashPath}")
    fun getCategoriesList(
        @Path("hashPath") hashPath: String
    ): Call<CategoriesJSONObject>

    @GET("{hashPath}")
    fun getDishesList(
        @Path("hashPath") hashPath: String
    ): Call<DishesJSONObject>



}