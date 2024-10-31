// data/remote/PlantNetApiClient.kt
package tec.mx.ocoyucango.data.remote

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PlantNetApiClient {

    private const val BASE_URL = "https://my-api.plantnet.org/"

    private val client = OkHttpClient.Builder().build()

    val apiService: PlantNetApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlantNetApiService::class.java)
    }
}
