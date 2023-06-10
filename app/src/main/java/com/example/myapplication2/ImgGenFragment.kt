package com.example.myapplication2

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.PyObject
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*


import androidx.viewpager2.widget.ViewPager2

class ImgGenFragment : Fragment() {
    private var description: String = ""
    private val oPENAI_API_KEY = MainActivity.apiKey

    private var previousD: String = ""


    private lateinit var imageView: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var refreshButton: Button

    private lateinit var viewModel: ImgGenViewModel

    private var myBitmap: Bitmap? = null
    private var previousmyBitmap: Bitmap? = null

    private var autoExecutionJob: Job? = null



    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.img_gen_fragment, container, false)
        viewModel = ViewModelProvider(this).get(ImgGenViewModel::class.java)


        super.onViewCreated(view, savedInstanceState)


        imageView = view.findViewById(R.id.imageView)
        progressBar = view.findViewById(R.id.progressBar)
        refreshButton = view.findViewById(R.id.refreshButton)

        // 이미지 생성 로직을 실행하는 코드
        description = StoryFragment.description



        //startAutoExecution()
        if (myBitmap==previousmyBitmap || myBitmap == null) {
            Toast.makeText(requireContext(), "데이터가 아직 준비되지 않았습니다.", Toast.LENGTH_SHORT).show()
        } else {
            imageView.setImageBitmap(myBitmap)
            previousmyBitmap = myBitmap
            imageView.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }


        refreshButton.setOnClickListener {
            description = StoryFragment.description
            if (description==previousD || description == "") {
                Toast.makeText(requireContext(), "텍스트 데이터가 아직 준비되지 않았습니다.", Toast.LENGTH_SHORT).show()
            } else {
                // updateDescription 메소드 호출
                refreshButton.isEnabled = false

                Toast.makeText(requireContext(), "이미지 생성 준비중입니다.", Toast.LENGTH_SHORT).show()


                imageView.visibility = View.GONE
                progressBar.visibility = View.VISIBLE

                lifecycleScope.launch {
                    myBitmap = getImageFromDescription(description)
                    if (myBitmap==previousmyBitmap || myBitmap == null) {
                        Toast.makeText(requireContext(), "이미지 데이터가 아직 준비되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        previousD = description

                        imageView.setImageBitmap(myBitmap)
                        println("이미지 셋")

                        previousmyBitmap = myBitmap
                        refreshButton.isEnabled = true

                        imageView.visibility = View.VISIBLE
                        progressBar.visibility = View.GONE
                    }

                }
            }
        }


        return view
    }

    fun updateDescription(newDescription: String, bitmap: Bitmap?) {
        println("업데이트 되었습니다!!!!!!!!!!!!")
        description = newDescription
        myBitmap = bitmap
    }


    suspend fun getImageFromDescription(description: String): Bitmap? {
        return withContext(Dispatchers.Default) {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(requireContext()))
            }

            val py = Python.getInstance()
            val pyObject = py.getModule("script")
            pyObject.callAttr("set_openai_api_key", oPENAI_API_KEY)
            val returnValue = pyObject.callAttr("main", description)
            val objString = returnValue.toString()

            val imageBytes = Base64.decode(objString, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

            // 바이트 코드를 사용하는 추가 작업 수행

            return@withContext bitmap
        }
    }

    fun reset() {
        // 변수 및 상태 초기화

        description = ""
        previousD = ""
        myBitmap = null
        previousmyBitmap = null

        refreshButton.isEnabled = true

        // UI 초기화
        if (::imageView.isInitialized) {
            imageView.visibility = View.GONE
        }
        if (::progressBar.isInitialized) {
            progressBar.visibility = View.GONE
        }
        if (::refreshButton.isInitialized) {
            refreshButton.isEnabled = true
        }
    }


    /*
    override fun onPause() {
        super.onPause()
        isAutoExecutionRunning = false
    }

    private fun startAutoExecution() {
        // 주기적으로 검사할 간격 (밀리초 단위)
        val interval = 1000L // 1초

        autoExecutionJob = CoroutineScope(Dispatchers.Default).launch {
            while (isAutoExecutionRunning) {
                withContext(Dispatchers.Main) {
                    if (StoryFragment.description.isNotEmpty()) {
                        // description2가 비어있지 않은 경우 실행
                        updateDescription(StoryFragment.description)
                    } else {
                        // description2가 비어있는 경우 토스트 메시지 출력
                        Toast.makeText(requireContext(), "데이터가 아직 준비되지 않았습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

                delay(interval)
            }
        }
    }

    private fun stopAutoExecution() {
        autoExecutionJob?.cancel()
    }

     */


}
