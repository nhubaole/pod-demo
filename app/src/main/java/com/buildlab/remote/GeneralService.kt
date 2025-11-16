import com.buildlab.common.concurrency.AppDispatchers
import com.buildlab.pod_demo.DatasetItem
import com.buildlab.pod_demo.DatasetsResponse
import com.buildlab.pod_demo.PrintResponse
import com.buildlab.remote.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

class GeneralService(
    private val httpClient: OkHttpClient = ApiClient.client
) {

    // ---------------------- GET /datasets ----------------------
    suspend fun getDatasets(): DatasetsResponse = withContext(AppDispatchers.IO) {

        val request = Request.Builder()
            .url(ApiClient.BASE_URL + "datasets")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            val jsonStr = response.body?.string() ?: "{}"
            val json = JSONObject(jsonStr)

            val datasetsArray = json.getJSONArray("datasets")

            val items = mutableListOf<DatasetItem>()

            for (i in 0 until datasetsArray.length()) {
                val item = datasetsArray.getJSONObject(i)
                items.add(
                    DatasetItem(
                        dataSetName = item.getString("dataset_name"),
                        originalName = item.getString("original_name"),
                        path = item.getString("path"),
                    )
                )
            }

            return@withContext DatasetsResponse(items)
        }
    }

    // ---------------------- POST /add ----------------------
    suspend fun addDataset(
        datasetName: String,
        regMale: File,
        regFemale: File,
        origMale: File,
        origFemale: File,
        color: String = "#64634A",
        tolerance: Int = 30
    ): String = withContext(Dispatchers.IO) {

        val form = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("dataset_name", datasetName)
            .addFormDataPart("color", color)
            .addFormDataPart("tolerance", tolerance.toString())
            .addFormDataPart(
                "regzone_male_image",
                regMale.name,
                regMale.asRequestBody("image/png".toMediaType())
            )
            .addFormDataPart(
                "regzone_female_image",
                regFemale.name,
                regFemale.asRequestBody("image/png".toMediaType())
            )
            .addFormDataPart(
                "origin_male_image",
                origMale.name,
                origMale.asRequestBody("image/png".toMediaType())
            )
            .addFormDataPart(
                "origin_female_image",
                origFemale.name,
                origFemale.asRequestBody("image/png".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(ApiClient.BASE_URL + "add")
            .post(form)
            .build()

        httpClient.newCall(request).execute().use { res ->
            res.body?.string() ?: ""
        }
    }

    // ---------------------- POST /print ----------------------

    suspend fun printSticker(
        datasetName: String,
        sticker: File,
        gender: String = "both",
        keepWhite: Boolean = false
    ): PrintResponse = withContext(Dispatchers.IO) {

        val form = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("dataset_name", datasetName)
            .addFormDataPart("gender", gender)
            .addFormDataPart("keep_white", keepWhite.toString())
            .addFormDataPart(
                "sticker_image",
                sticker.name,
                sticker.asRequestBody("image/png".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url(ApiClient.BASE_URL + "print")
            .post(form)
            .build()

        httpClient.newCall(request).execute().use { response ->
            val jsonStr = response.body?.string() ?: "{}"
            val json = JSONObject(jsonStr)

            return@withContext PrintResponse(
                success = json.getBoolean("success"),
                datasetName = json.getString("dataset_name"),
                gender = json.getString("gender"),
                maleImageUrl = json.optString("male_image_url", null),
                femaleImageUrl = json.optString("female_image_url", null),
                imageUrl = json.optString("image_url", null)
            )
        }
    }

    // ---------------------- Download Image ----------------------
    suspend fun downloadImage(url: String): ByteArray = withContext(AppDispatchers.IO) {
        val request = Request.Builder().url(url).build()

        httpClient.newCall(request).execute().use { response ->
            response.body?.bytes() ?: ByteArray(0)
        }
    }
}
