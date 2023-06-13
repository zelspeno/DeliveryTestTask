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
    val id: Long?,
    val name: String?,
    val price: Double?,
    val weight: Double?,
    val description: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("tegs")
    val tags: List<String>?
): Serializable

data class DishesJSONObject(
    @SerializedName("dishes")
    val dishes: List<Dish>
)

data class Tag(
    val name: String,
    val onClick: Boolean
)

data class CartDish(
    val id: Long?,
    val name: String?,
    val price: String?,
    val weight: String?,
    val imageUrl: String?,
    var count: Int?
)

data class Coordinates(
    val latitude: Double?,
    val longitude: Double?
)