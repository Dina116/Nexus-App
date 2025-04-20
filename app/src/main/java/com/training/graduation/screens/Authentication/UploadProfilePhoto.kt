
import android.content.Context
import android.content.Intent
import android.net.Uri
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

fun uploadImageToCloudinary(
    context: Context,
    imageUri: Uri,
    onSuccess: (String) -> Unit,
    onFailure: (String) -> Unit
) {
    val contentResolver = context.contentResolver
    val inputStream = contentResolver.openInputStream(imageUri)
    val bytes = inputStream?.readBytes()

    val requestBody = bytes?.let {
        MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                "image.jpg",
                RequestBody.create("image/*".toMediaTypeOrNull(), it)
            )
            .addFormDataPart("upload_preset", "default_image")
            .build()
    }


    val request = Request.Builder()
        .url("https://api.cloudinary.com/v1_1/daclkwzzc/image/upload")
        .post(requestBody!!)
        .build()

    val client = OkHttpClient()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            (context as? android.app.Activity)?.runOnUiThread {
                onFailure(e.message ?: "Upload failed")
            }
        }

        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            val jsonObject = JSONObject(responseBody ?: "{}")
            val url = jsonObject.optString("secure_url")
            (context as? android.app.Activity)?.runOnUiThread {
                if (url.isNotEmpty()) {
                    onSuccess(url)
                } else {
                    onFailure("Upload failed: no URL returned")
                }
            }
        }
    })
}
