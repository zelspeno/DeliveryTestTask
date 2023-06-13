package com.zelspeno.deliverytesttask.components

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.zelspeno.deliverytesttask.MainActivity
import com.zelspeno.deliverytesttask.MainApp
import com.zelspeno.deliverytesttask.source.Const
import com.zelspeno.deliverytesttask.source.Coordinates
import com.zelspeno.deliverytesttask.source.DeliveryApi
import dagger.*
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule

@Module
@InstallIn(SingletonComponent::class)
class SharedPreferencesModule {

    @Provides
    @Singleton
    fun providesCartSharedPreferences(@ApplicationContext context: Context?): SharedPreferences? =
        context?.getSharedPreferences("cart", Context.MODE_PRIVATE)

}



@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun providesBaseRetrofit(client: OkHttpClient): Retrofit {
        val retrofit = Retrofit.Builder()
            .baseUrl(Const.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
        return retrofit.build()
    }

    @Provides
    @Singleton
    fun providesHttpClient() = OkHttpClient.Builder().build()

    @Provides
    @Singleton
    fun providesDeliveryApi(retrofit: Retrofit) : DeliveryApi {
        return retrofit.create(DeliveryApi::class.java)
    }

}

@Module
@InstallIn(SingletonComponent::class)
class GpsModule {

    @SuppressLint("MissingPermission")
    @Provides
    fun provideLocationManager(@ApplicationContext context: Context?): Location? {
        val lm = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        var location = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        return location
    }

    @Provides
    @Named("cityName")
    fun provideCityName(@ApplicationContext context: Context?, coordinates: Coordinates): String {
        val latitude = coordinates.latitude
        val longitude = coordinates.longitude
        val geocoder = Geocoder(context!!, Locale.forLanguageTag("ru-RU"))
        if (latitude != null && longitude != null) {
            val adresses = geocoder.getFromLocation(latitude, longitude, 1)
            return adresses?.get(0)?.locality!!
        } else {
            return "null"
        }
    }

    @SuppressLint("MissingPermission")
    @Provides
    fun provideUsersCoordinates(location: Location?): Coordinates {
        val latitude = location?.latitude
        val longitude = location?.longitude
        return Coordinates(latitude, longitude)
    }

    @Named("GpsPermissions")
    @Provides
    fun gpsPermissionList() = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

}