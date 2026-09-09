package com.afrimedia.crm.data.repo

import com.afrimedia.crm.data.model.ApiFailure
import com.afrimedia.crm.data.remote.ApiClient
import com.afrimedia.crm.data.remote.ApiService
import com.afrimedia.crm.data.remote.SessionManager
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException

/** Simple success/failure envelope so ViewModels don't deal with exceptions directly. */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure(val error: ApiFailure) : ApiResult<Nothing>()
}

/**
 * Thin wrapper around ApiService that turns network/HTTP errors into
 * ApiResult.Failure with a human-readable message, so every screen handles
 * errors the same way (a snackbar / inline banner).
 */
class Repository(private val session: SessionManager) {

    val api: ApiService get() = ApiClient.service(session)

    suspend fun <T> call(block: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) ApiResult.Success(body)
                else ApiResult.Success(Unit as T)
            } else {
                ApiResult.Failure(ApiFailure(extractError(response.errorBody()), response.code()))
            }
        } catch (e: IOException) {
            ApiResult.Failure(ApiFailure("Can't reach ${session.siteUrl.ifBlank { "the CRM" }}. Check your connection and the site address."))
        } catch (e: Exception) {
            ApiResult.Failure(ApiFailure(e.message ?: "Something went wrong."))
        }
    }

    private fun extractError(errorBody: ResponseBody?): String {
        val text = errorBody?.string() ?: return "Request failed."
        return try {
            val obj = Json { ignoreUnknownKeys = true }.parseToJsonElement(text).let { it as? JsonObject }
            obj?.get("message")?.jsonPrimitive?.content ?: text
        } catch (e: Exception) {
            text.ifBlank { "Request failed." }
        }
    }
}
