package com.zelspeno.deliverytesttask.source

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Category (
    val id: Long,
    val name: String,
    @SerializedName("image_url")
    val imageUrl: String
): Serializable

data class CategoriesJSONObject(
    @SerializedName("сategories")
    val categories: List<Category>
)

data class Dish (
    val id: Long,
    val name: String,
    val price: Double,
    val weight: Double,
    val description: String,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("tegs")
    val tags: List<String>
): Serializable

data class DishesJSONObject(
    @SerializedName("dishes")
    val dishes: List<Dish>
)