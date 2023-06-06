package com.zelspeno.deliverytesttask.ui.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationToken
import com.zelspeno.deliverytesttask.source.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.function.Consumer


class HomeViewModel : ViewModel() {

    private val _categoriesList = MutableSharedFlow<List<Category>?>()
    val categoriesList = _categoriesList.asSharedFlow()

    private val _dishesList = MutableSharedFlow<List<Dish>?>()
    val dishesList = _dishesList.asSharedFlow()

    private val _userCity = MutableSharedFlow<String?>()
    val userCity = _userCity.asSharedFlow()

    /** Emit values to [categoriesList] */
    fun getCategoriesList() {
        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(Const.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(DeliveryApi::class.java)

        val response: Call<CategoriesJSONObject> = api.getCategoriesList(Const.CATEGORIES_URL)

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
        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl(Const.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val api = retrofit.create(DeliveryApi::class.java)

        val response: Call<DishesJSONObject> = api.getDishesList(Const.DISHES_URL)

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

//    @RequiresApi(Build.VERSION_CODES.R)
//    fun getUserLocation(context: Context, activity: Activity) {
//        var locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
//        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
//        val locationListener = object : LocationListener {
//            override fun onLocationChanged(location: Location) {
//                viewModelScope.launch {
//                    var lat = location.latitude
//                    var long = location.longitude
//                    Log.d("location", location.toString())
//                    val geocoder = Geocoder(context, Locale.forLanguageTag("ru-RU"))
//                    val adresses = geocoder.getFromLocation(lat, long, 1)
//                    Log.d("adress", adresses.toString())
//                    _userCity.emit(adresses?.get(0)?.locality)
//                }
//            }
//        }
//        viewModelScope.launch {
//            if (ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                    context,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                val arr = arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                )
//                ActivityCompat.requestPermissions(
//                    activity,
//                    arr,
//                    100
//                )
//            }
//            locationManager.getCurrentLocation(
//                LocationManager.GPS_PROVIDER,
//                null,
//                activity.mainExecutor,
//                Consumer<Location>() {
//                    fun accept(location: Location) {
//                        viewModelScope.launch {
//                            var lat = location.latitude
//                            var long = location.longitude
//                            Log.d("location", location.toString())
//                            val geocoder = Geocoder(context, Locale.forLanguageTag("ru-RU"))
//                            val adresses = geocoder.getFromLocation(lat, long, 1)
//                            Log.d("adress", adresses.toString())
//                            _userCity.emit(adresses?.get(0)?.locality)
//                        }
//                    }
//                }
//            )
//        }
//    }
}