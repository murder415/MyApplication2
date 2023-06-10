package com.example.myapplication2


import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import android.content.Context


class ImgGenViewModel : ViewModel() {
    private val oPENAI_API_KEY = MainActivity.apiKey

    private val _imageBitmap = MutableLiveData<Bitmap>()
    val imageBitmap: LiveData<Bitmap> get() = _imageBitmap

    fun generateImage(description: String, context: Context) {
        viewModelScope.launch {
            val generatedBitmap = executeChaquopyInBackground(description, context)
            _imageBitmap.postValue(generatedBitmap)
        }
    }

    private suspend fun executeChaquopyInBackground(description: String, context: Context): Bitmap = withContext(Dispatchers.IO) {
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(context))
        }

        val py = Python.getInstance()
        val pyObject = py.getModule("script")
        pyObject.callAttr("set_openai_api_key",oPENAI_API_KEY)
        val returnValue = pyObject.callAttr("main", description)
        val objString = returnValue.toString()

        val imageBytes = Base64.decode(objString, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }
}

