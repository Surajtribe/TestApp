package com.test.emp.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import com.test.emp.domain.model.RandomText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class RandomTextRepository(
    private val contentResolver: ContentResolver
) {

    private val providerUri = Uri.parse("content://com.iav.contestdataprovider/text")

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getRandomText(maxLength: Int): RandomText? {
        return withContext(Dispatchers.IO) {
            try {
                val queryArgs = bundleOf(ContentResolver.QUERY_ARG_LIMIT to maxLength)
                val cursor = contentResolver.query(
                    providerUri,
                    null,
                    queryArgs,
                    null
                )

                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val jsonData = c.getString(c.getColumnIndexOrThrow("data"))
                        val jsonObject = JSONObject(jsonData)
                            .getJSONObject("randomText")

                        val value = jsonObject.optString("value", "")
                        val length = jsonObject.optInt("length", 0)
                        val created = jsonObject.optString("created", "")

                        return@withContext RandomText(
                            value = value,
                            length = length,
                            created = created
                        )
                    }
                }
                null
            } catch (se: SecurityException) {
                Log.e("RandomTextRepository", "Permission denied: ${se.message}")
                null
            } catch (e: Exception) {
                Log.e("RandomTextRepository", "Error querying content provider: ${e.message}")
                null
            }
        }
    }
}

