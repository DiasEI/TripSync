package com.example.tripsync.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tripsync.api.AddLocalRequest
import com.example.tripsync.api.ApiClient
import com.google.gson.Gson
import java.io.IOException

class SendLocalRequestsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val sharedPreferences = applicationContext.getSharedPreferences("local_requests", Context.MODE_PRIVATE)
            val json = sharedPreferences.getString("unsent_request", null) ?: return Result.success()

            val request = Gson().fromJson(json, AddLocalRequest::class.java)
            sendAddLocalRequest(request)

            sharedPreferences.edit().remove("unsent_request").apply()
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending local request: ${e.message}")
            return Result.failure()
        }
    }

    private fun sendAddLocalRequest(request: AddLocalRequest) {
        try {
            val response = ApiClient.apiService.addLocal(request).execute()
            if (response.isSuccessful) {
                Log.d(TAG, "addLocal onResponse: Success")
            } else {
                Log.e(TAG, "addLocal onResponse: Failed with code ${response.code()}")
            }
        } catch (e: IOException) {
            Log.e(TAG, "addLocal onFailure: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "SendLocalRequestsWorker"
    }
}
