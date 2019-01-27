package com.gauravgoyal.flickrsearch.repo

import android.util.Log
import com.gauravgoyal.flickrsearch.model.PhotoModel
import com.gauravgoyal.flickrsearch.model.PhotoServiceResponse
import com.gauravgoyal.flickrsearch.util.utility.API_URL
import com.gauravgoyal.flickrsearch.util.utility.Utility
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object PhotosRepo {
    fun getPhotos(key: String, page: Int): PhotoServiceResponse? {
        var reader: BufferedReader? = null

        try {

            val urlString = String.format(API_URL, key, page)
            val myUrl = URL(urlString)

//            Log.d("PhotosRepo", urlString)

            val conn = myUrl
                .openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.connect()

            val statusCode = conn.getResponseCode()
            if (statusCode != 200) {
                return null
            }

            val inputStream = conn.inputStream

            if (inputStream == null) {
                return null
            }

            reader = BufferedReader(InputStreamReader(inputStream))
            val response = deserializeServiceResponse(Utility.toString(reader))
            return response

        } catch (e: IOException) {
            return null

        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (e: Exception) {
                }

            }
        }
    }

    private fun deserializeServiceResponse(response: String): PhotoServiceResponse? {

        var photoServiceResponse: PhotoServiceResponse? = null

        try {
            val responseJsonObj = JSONObject(response)
            val responseJson = JSONObject(responseJsonObj.getString("photos"))
            val currentPage = responseJson.getInt("page")
            val totalPages = responseJson.getInt("pages")
            val itemsPerPage = responseJson.getInt("perpage")


            val dataJson = responseJson.getJSONArray("photo")

            val photos = ArrayList<PhotoModel>()

            for (i in 0 until dataJson.length()) {

                val photoJson = dataJson.getJSONObject(i)
                val secret = photoJson.getString("secret")
                val server = photoJson.getString("server")
                val id = photoJson.getString("id")
                val farm = photoJson.getInt("farm")


                photos.add(PhotoModel(secret, farm, id, server))
            }

            photoServiceResponse = PhotoServiceResponse(
                data = photos,
                currentPage = currentPage,
                totalPages = totalPages,
                itemsPerPage = itemsPerPage
            )
        } catch (e: JSONException) {
            Log.d("PhotoRepo", "Exception while parsing")
        }

        return photoServiceResponse
    }
}