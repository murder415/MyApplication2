package com.example.myapplication2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.chaquo.python.Python
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.os.Handler
import android.os.Looper
import android.widget.Toast



class ChoiceViewModel : ViewModel() {
    private val translatedCommandsLiveData: MutableLiveData<Map<String, String>> = MutableLiveData()

    private var isAutoExecutionRunning = false


    val translatedCommands: LiveData<Map<String, String>>
        get() = translatedCommandsLiveData

    fun translateCommands(commands: Map<String, String>) {
        // 백그라운드 스레드에서 번역 작업 수행
        // 작업이 완료되면 LiveData를 업데이트하여 UI에 알림

        viewModelScope.launch {
            val translatedMap = withContext(Dispatchers.Default) {
                val py = Python.getInstance()
                val pyObject = py.getModule("translate")

                commands.mapValues { (_, value) ->
                    pyObject.callAttr("en2ko", value).toString()
                }
            }

            translatedCommandsLiveData.postValue(translatedMap)
        }
    }

    fun startAutoExecution() {
        // 주기적으로 검사할 간격 (밀리초 단위)

        val interval = 10000L // 1초

        // 이전의 possible commands
        var previousPossibleCommands = StoryFragment.commands

        // 핸들러를 사용하여 주기적으로 실행
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                if (isAutoExecutionRunning) {
                    println("감지중입니다.")

                    // possible commands가 변경되었을 경우에만 translatedCommands 업데이트
                    if (StoryFragment.commands != previousPossibleCommands) {
                        println("감지 성공!!")

                        translateCommands(StoryFragment.commands)
                        previousPossibleCommands = StoryFragment.commands
                    }
                }

                // 일정 간격 후에 다시 실행
                handler.postDelayed(this, interval)
            }
        })
    }



}
