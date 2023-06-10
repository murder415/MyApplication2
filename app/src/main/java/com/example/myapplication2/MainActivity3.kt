/*
package com.example.myapplication2



import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import coil.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import androidx.lifecycle.lifecycleScope
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import android.content.Intent


class MainActivity3 : AppCompatActivity(){

    private lateinit var imageView: ImageView
    private lateinit var newImageButton: Button
    private lateinit var startOverButton: Button
    private lateinit var titleTextView: TextView

    private val apiKey = "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"
    private val url = "https://api.openai.com/v1/images/generations"
    private val prompt = "A portal to another world"
    private val size = "256x512"
    private val numImages = 1
    private val responseFormat = "url"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        imageView = findViewById(R.id.imageView)
        newImageButton = findViewById(R.id.newImageButton)
        startOverButton = findViewById(R.id.startButton)
        titleTextView = findViewById(R.id.titleTextView)

        // 초기 이미지 로드
        loadImage()

        // 새 이미지 버튼 클릭 시
        newImageButton.setOnClickListener {
            loadImage()
        }

        // 시작하기 버튼 클릭 시
        startOverButton.setOnClickListener {
            val intent = Intent(this, StoryFragment::class.java)
            startActivity(intent)
        }
    }


    private fun loadImage() {
        lifecycleScope.launch {
            try {
                val responseUrl = generateImage()
                if (!responseUrl.isNullOrEmpty()) {
                    imageView.load(responseUrl)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun generateImage(): String? = withContext(Dispatchers.IO) {
        val jsonString = """
            { "model": "image-alpha-001",
              "prompt": "$prompt",
              "num_images": $numImages,
              "size": "$size",
              "response_format": "$responseFormat" }
        """.trimIndent()

        val client = OkHttpClient()

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = jsonString.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .method("POST", body)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful && response.body != null) {
            val responseString = response.body!!.string()
            return@withContext JSONUtil.getUrlFromJson(responseString)
        } else {
            throw IOException("Failed to get response from API")
        }
    }

    object JSONUtil {

        fun getUrlFromJson(jsonString: String): String? {
            return try {
                val jsonObject = JSONObject(jsonString)
                val jsonArray = jsonObject.getJSONArray("data")
                val dataObject = jsonArray.getJSONObject(0)
                dataObject.getString("url")
            } catch (e: JSONException) {
                e.printStackTrace()
                null
            }
        }
    }

}
*/