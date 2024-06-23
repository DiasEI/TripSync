package com.example.tripsync.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.tripsync.api.ApiClient
import com.example.tripsync.api.Trip
import com.google.gson.Gson
import java.io.IOException

class SendViagemRequestsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            val sharedPreferences = applicationContext.getSharedPreferences("local_viagens_requests", Context.MODE_PRIVATE)
            val json = sharedPreferences.getString("unsent_viagens_requests", null) ?: return Result.success()

            val request = Gson().fromJson(json, Trip::class.java)
            sendAddViagemRequest(request)

            sharedPreferences.edit().remove("unsent_viagens_requests").apply()
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error sending viagem request: ${e.message}")
            return Result.failure()
        }
    }

    private fun sendAddViagemRequest(request: Trip) {
        try {
            val response = ApiClient.apiService.adicionarViagem(request).execute()
            if (response.isSuccessful) {
                Log.d(TAG, "addViagem onResponse: Success")
            } else {
                Log.e(TAG, "addViagem onResponse: Failed with code ${response.code()}")
            }
        } catch (e: IOException) {
            Log.e(TAG, "addViagem onFailure: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "SendLocalRequestsWorker"
    }
}
