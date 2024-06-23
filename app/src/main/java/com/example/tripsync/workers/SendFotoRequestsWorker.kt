package com.example.tripsync.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tripsync.api.AddFotosRequest
import com.example.tripsync.api.ApiClient
import com.google.gson.Gson
import java.io.IOException

class SendFotoRequestsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val sharedPreferences = applicationContext.getSharedPreferences("local_fotos_requests", Context.MODE_PRIVATE)
            val json = sharedPreferences.getString("unsent_fotos_request", null) ?: return Result.success()

            val request = Gson().fromJson(json, AddFotosRequest::class.java)
            uploadFotos(request)

            sharedPreferences.edit().remove("unsent_fotos_request").apply()
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading fotos: ${e.message}")
            Result.failure()
        }
    }

    private fun uploadFotos(request: AddFotosRequest) {
        try {
            val response = ApiClient.apiService.addFotos(request).execute()
            if (response.isSuccessful) {
                Log.d(TAG, "uploadFotos onResponse: Success")
            } else {
                Log.e(TAG, "uploadFotos onResponse: Failed with code ${response.code()}")
            }
        } catch (e: IOException) {
            Log.e(TAG, "uploadFotos onFailure: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "UploadFotosWorker"
    }
}
