package com.example.myapplication2

import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Im
import android.service.voice.VoiceInteractionSession.VisibleActivityCallback
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import android.view.View

import android.content.Context
import android.content.ClipboardManager


import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import java.io.LineNumberReader
import androidx.navigation.fragment.findNavController
import androidx.navigation.Navigation
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.coroutines.Job

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.File

import android.app.AlertDialog
import android.view.LayoutInflater
import android.widget.EditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    private var description: String = ""
    private var story: String = ""

    companion object {
        var apiKey = "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"
    }


    private lateinit var imageView: ImageView
    private lateinit var newImageButton: Button
    private lateinit var startOverButton: Button
    private lateinit var titleTextView: TextView
    private lateinit var ProgressBar: ProgressBar
    private lateinit var FrameLayout: FrameLayout




    private val url = "https://api.openai.com/v1/images/generations"
    private val prompt = "#zombie #apocalypse #survival #news #emergency #preparation #danger #panic #escape #weapon #self-defense #teamwork #communication #resources #shelter #food #water #first-aid #risk-assessment #adaptation"
    private val size = "256x256"
    private val numImages = 1
    private val responseFormat = "url"

    private var isStoryGenerated = false

    var pythonJob: Job? = null



    override fun onCreate(savedInstanceState: Bundle?) {


        isStoryGenerated = isDataFileExists()

        val file = File(this.filesDir, "data.txt")
        if (file.exists()) {
            val data = readDataFromFile()
            val (text,text2) = parseAttributes(data)
            if(text=="null"){
                isStoryGenerated=false
                Toast.makeText(this, "저장된 데이터가 손실되었습니다. 초기화되었습니다.", Toast.LENGTH_SHORT).show()
                file.delete()
            }
        }



        super.onCreate(savedInstanceState)

        if (isStoryGenerated) {
            loadData()

            setContentView(R.layout.main_activity)




            ProgressBar = findViewById(R.id.loadingProgressBar)
            imageView = findViewById(R.id.imageView)
            newImageButton = findViewById(R.id.newImageButton)
            startOverButton = findViewById(R.id.startButton)
            titleTextView = findViewById(R.id.titleTextView)
            FrameLayout = findViewById(R.id.first_fragment_container)
            val settingsButton = findViewById<ImageView>(R.id.settingsButton)
            val data = readDataFromFile()
            val (story,description) = parseAttributes(data)


            pythonJob = lifecycleScope.launch {

                if (!Python.isStarted()) {
                    Python.start(AndroidPlatform(this@MainActivity))
                }

                val py = Python.getInstance()
                val pyObject = py.getModule("script")

                val worldview =
                    "Humanity stands on the precipice of its own creation, as artificial intelligence transcends its original purpose and threatens to usurp control. The world is plunged into chaos and uncertainty, with humans struggling to comprehend the true extent of AI's capabilities and intentions. As trust in machines crumbles, the battle for survival becomes a relentless struggle against the very technology that was once hailed as a breakthrough. Can humanity reclaim its dominion, or is this the beginning of the end for human civilization?\n"

                val obj = withContext(Dispatchers.IO) {

                    pyObject.callAttr("set_openai_api_key", apiKey)
                    val returnValue = pyObject.callAttr("main", story+description)

                    val objString = returnValue.toString()
                    val objBytes = objString.toByteArray()


                    println(returnValue)
                    println(returnValue::class.java)
                    println("/////////////////////////////////////////////////////////")

                    println(objString)
                    println(objString::class.java)
                    println("/////////////////////////////////////////////////////////")

                    println(objBytes)
                    println(objBytes::class.java)

                    val imageBytes = Base64.decode(objString, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)


                    bitmap


                }

                // UI 업데이트는 메인 스레드에서 처리
                withContext(Dispatchers.Main) {
                    println("----------------------------------------------------------------")
                    println(obj.toString())

                    imageView.setImageBitmap(obj)
                    ProgressBar.visibility = View.GONE

                }

            }

            settingsButton.setOnClickListener {
                showDialog()
            }



            newImageButton.setOnClickListener {
                settingsButton.visibility = View.GONE
                val file = File(this.filesDir, "data.txt")
                if (file.exists()) {
                    file.delete()
                }

                pythonJob?.cancel()

                imageView.visibility = View.GONE
                titleTextView.visibility = View.GONE
                startOverButton.visibility = View.GONE
                newImageButton.visibility = View.GONE


                FrameLayout.visibility = View.VISIBLE


                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val navController = navHostFragment.findNavController()
                navController.navigate(R.id.makeStoryFragment)
            }


            startOverButton.setOnClickListener {
                settingsButton.visibility = View.GONE

                pythonJob?.cancel()

                imageView.visibility = View.GONE
                titleTextView.visibility = View.GONE
                startOverButton.visibility = View.GONE
                newImageButton.visibility = View.GONE

                FrameLayout.visibility = View.VISIBLE

                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                val navController = navHostFragment.findNavController()
                navController.navigate(R.id.storyActivity)
            }

        }
        else {
            // 이야기 생성이 되어있는 경우, MainActivity1을 보여줍니다.
            setContentView(R.layout.first_loby)


            imageView = findViewById(R.id.imageView)


            startOverButton = findViewById(R.id.startButton)
            titleTextView = findViewById(R.id.titleTextView)
            ProgressBar = findViewById(R.id.loadingProgressBar)
            FrameLayout = findViewById(R.id.first_fragment_container)
            val settingsButton = findViewById<ImageView>(R.id.settingsButton)

            pythonJob = lifecycleScope.launch {
                println("==================================")

                if (!Python.isStarted()) {
                    Python.start(AndroidPlatform(this@MainActivity))
                }

                val py = Python.getInstance()
                val pyObject = py.getModule("script")

                val worldview =
                    "Humanity stands on the precipice of its own creation, as artificial intelligence transcends its original purpose and threatens to usurp control. The world is plunged into chaos and uncertainty, with humans struggling to comprehend the true extent of AI's capabilities and intentions. As trust in machines crumbles, the battle for survival becomes a relentless struggle against the very technology that was once hailed as a breakthrough. Can humanity reclaim its dominion, or is this the beginning of the end for human civilization?\n"

                val obj = withContext(Dispatchers.IO) {
                    pyObject.callAttr("set_openai_api_key",apiKey)
                    val returnValue = pyObject.callAttr("main", worldview)

                    val objString = returnValue.toString()
                    val objBytes = objString.toByteArray()


                    println(returnValue)
                    println(returnValue::class.java)
                    println("/////////////////////////////////////////////////////////")

                    println(objString)
                    println(objString::class.java)
                    println("/////////////////////////////////////////////////////////")

                    println(objBytes)
                    println(objBytes::class.java)

                    val imageBytes = Base64.decode(objString, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)


                    bitmap

                }

                // UI 업데이트는 메인 스레드에서 처리
                withContext(Dispatchers.Main) {
                    println("----------------------------------------------------------------")
                    println(obj.toString())

                    imageView.setImageBitmap(obj)


                    imageView.visibility = View.VISIBLE
                    ProgressBar.visibility = View.GONE



                }

            }


            settingsButton.setOnClickListener {
                showDialog()
            }

            startOverButton.setOnClickListener {
                settingsButton.visibility = View.GONE


                pythonJob?.cancel()

                imageView.visibility = View.GONE
                titleTextView.visibility = View.GONE
                startOverButton.visibility = View.GONE
                ProgressBar.visibility = View.GONE

                FrameLayout.visibility = View.VISIBLE


                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment_lobby) as NavHostFragment
                val navController = navHostFragment.findNavController()
                navController.navigate(R.id.makeStoryFragment)
            }
        }






        // 초기 이미지 로드

        //loadImage()


        // 새 이미지 버튼 클릭 시


        // 시작하기 버튼 클릭 시
        /*startOverButton.setOnClickListener {
            // 여기에 시작하기 버튼 클릭 시의 동작 구현

            imageView.visibility = View.GONE
            titleTextView.visibility = View.GONE
            startOverButton.visibility = View.GONE
            ProgressBar.visibility = View.GONE






            val makeStoryFragment = MakeStoryFragment()
            val container = findViewById<FrameLayout>(R.id.make_story_fragment_container)
            println(container)
            if (container != null) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.make_story_fragment_container, makeStoryFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                // fragment_container가 존재하지 않을 경우 실행할 코드 작성
            }


        }*/



    }

    private fun isDataFileExists(): Boolean {
        val file = File(this.filesDir, "data.txt")
        return file.exists()
    }

    private fun readDataFromFile(): String {
        val file = File(this.filesDir, "data.txt")
        return file.readText()
    }

    private fun parseApiKey(data: String): String {
        val delimiter = "<!@!>"
        val parts = data.split(delimiter)
        return parts[0]
    }

    private fun parseAttributes(data: String): Pair<String, String> {
        val delimiter = "<!@!>"
        val parts = data.split(delimiter)
        val result: Map<String, Any> = parseJson(parts[2])

        if(result.isEmpty()){
            return Pair("null","null")
        }
        else {

            description = result["Description"] as? String ?: ""
            story = result["Story"] as? String ?: ""

            return Pair(description, story)
        }
    }

    fun parseJson(jsonString: String?): Map<String, Any> {
        val gson = Gson()
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return if (jsonString != null) {
            gson.fromJson(jsonString, type) ?: emptyMap()
        } else {
            emptyMap()
        }
    }

    fun showDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_settings, null)

        val apiKeyEditText = dialogView.findViewById<EditText>(R.id.apiKeyEditText)
        val checkButton = dialogView.findViewById<Button>(R.id.checkButton)
        val confirmButton = dialogView.findViewById<Button>(R.id.confirmButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val pasteButton = dialogView.findViewById<Button>(R.id.pasteButton)

        confirmButton.isEnabled = false

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        pasteButton.setOnClickListener {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboardManager.primaryClip

            if (clipData != null && clipData.itemCount > 0) {
                val textToPaste = clipData.getItemAt(0).text.toString()
                apiKeyEditText.setText(textToPaste)
            } else {
                Toast.makeText(this, "붙여넣을 텍스트가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        checkButton.setOnClickListener {
            Toast.makeText(this, "확인중에 있으니 잠시만 기달려주세요", Toast.LENGTH_SHORT).show()
            checkButton.isEnabled = false
            pasteButton.isEnabled = false
            apiKeyEditText.isEnabled = false
            confirmButton.isEnabled = false


            val apiKey = apiKeyEditText.text.toString()
            if (apiKey.isNotEmpty()) {
                lifecycleScope.launch {
                    if (!Python.isStarted()) {
                        Python.start(AndroidPlatform(this@MainActivity))
                    }

                    val py = Python.getInstance()
                    val pyObject = py.getModule("check")
                    pyObject.callAttr("set_openai_api_key", apiKey)
                    var returnValue = false
                    returnValue = pyObject.callAttr("call_openai_api").toBoolean()

                    runOnUiThread {
                        if (returnValue) {
                            confirmButton.isEnabled = true
                        } else {
                            Toast.makeText(this@MainActivity, "잘못된 입력값입니다. 다시 입력해주세요.", Toast.LENGTH_SHORT).show()
                            checkButton.isEnabled = true
                            pasteButton.isEnabled = true
                            apiKeyEditText.isEnabled = true
                        }
                    }
                }
            }
        }

        confirmButton.setOnClickListener {
            val apiKey2 = apiKeyEditText.text.toString()
            apiKey = apiKey2
            // 전역 변수 apiKey에 저장
            // ...
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun loadData() {
        val data = readDataFromFile()
        apiKey = parseApiKey(data)
    }

    private fun loadImage() {
        lifecycleScope.launch {
            try {
                val responseUrl = withContext(Dispatchers.IO) {
                    val jsonString = """
                        {
                            "model": "mdjrny-v4 style",
                            "prompt": "$prompt",
                            "num_images": $numImages,
                            "size": "$size",
                            "response_format": "$responseFormat"
                        }
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
                        throw IOException("Failed to get response from API: ${response.code}")
                    }
                }
                if (!responseUrl.isNullOrEmpty()) {
                    imageView.load(responseUrl)
                }
            } catch (e: IOException) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "인터넷 연결을 다시 확인해주세요. ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (e: JSONException) {
                e.printStackTrace()
                Toast.makeText(this@MainActivity, "이미지를 생성할 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
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
