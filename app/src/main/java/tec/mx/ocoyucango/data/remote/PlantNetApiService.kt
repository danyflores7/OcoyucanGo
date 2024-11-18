// data/remote/PlantNetApiService.kt
package tec.mx.ocoyucango.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface PlantNetApiService {

    @Multipart
    @POST("v2/identify/all")
    suspend fun identifyPlant(
        @Query("api-key") apiKey: String,
        @Query("lang") lang: String = "es", // Establecer "es" como valor predeterminado
        @Part parts: List<MultipartBody.Part>
    ): Response<PlantIdentificationResponse>
}
