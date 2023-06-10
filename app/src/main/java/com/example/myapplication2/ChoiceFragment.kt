package com.example.myapplication2

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import android.app.Dialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.Window
import android.os.Parcelable
import android.util.Base64
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.viewpager.widget.ViewPager
import android.graphics.drawable.GradientDrawable
import androidx.core.content.ContextCompat


class ChoiceFragment : Fragment() {
    private val oPENAI_API_KEY = MainActivity.apiKey
    private lateinit var storyFragment: StoryFragment

    companion object {
        var choiceYour: String = ""
    }

    private lateinit var command1Button: Button
    private lateinit var command2Button: Button
    private lateinit var command3Button: Button
    private lateinit var choiceButton: ImageButton
    private lateinit var refreshButton: Button

    private lateinit var viewModel: ChoiceViewModel

    private var commands: Map<String, String> = emptyMap()
    private var previouscommands: Map<String, String> = emptyMap()

    private var translatedCommands: Map<String, String> = emptyMap()
    private var isFirstFunctionExecuted = false
    private var isFirst = false





    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.choice_fragment, container, false)


        command1Button = view.findViewById(R.id.button1)
        command2Button = view.findViewById(R.id.button2)
        command3Button = view.findViewById(R.id.button3)
        choiceButton = view.findViewById(R.id.chocieButton)
        refreshButton = view.findViewById(R.id.refreshButton)


        command1Button.isEnabled = false
        command2Button.isEnabled = false
        command3Button.isEnabled = false


        refreshButton.setOnClickListener {
            commands = StoryFragment.commands
            previouscommands = StoryFragment.previousChoice
            if(commands==previouscommands || commands.isEmpty()){
                Toast.makeText(requireContext(), "텍스트 데이터가 아직 준비되지 않았습니다.", Toast.LENGTH_SHORT).show()

            }
            else {


                command1Button.isEnabled = true
                command2Button.isEnabled = true
                command3Button.isEnabled = true

                if (StoryFragment.commands.containsKey("3")) {
                    choiceButton.visibility = View.VISIBLE

                }

                refreshButton.isEnabled = false

                command1Button.text = commands["1"]
                command2Button.text = commands["2"]


                Toast.makeText(requireContext(), "버튼 번역중입니다.", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    translatedCommands = translateCommands(commands)

                    command1Button.text = translatedCommands["1"]
                    command2Button.text = translatedCommands["2"]
                    previouscommands = commands

                    refreshButton.isEnabled = true

                }
            }
        }



        // ViewModel 초기화
        /*
        viewModel = ViewModelProvider(this).get(ChoiceViewModel::class.java)
        viewModel.translateCommands(StoryFragment.commands)
        viewModel.startAutoExecution()

        viewModel.translatedCommands.observe(viewLifecycleOwner) { translatedCommands ->
            // Update UI or perform actions using the translated commands
            // For example, you can update the text of buttons:
            command1Button.text = translatedCommands["1"]
            command2Button.text = translatedCommands["2"]
            command3Button.text = translatedCommands["3"]
        }
        */

        // ...

        // 클릭 리스너 설정
        command1Button.setOnClickListener {
            gostart(command1Button.text.toString())
        }

        command2Button.setOnClickListener {
            gostart(command2Button.text.toString())
        }

        command3Button.setOnClickListener {
            if (!isFirstFunctionExecuted) {
                showEditCommandsDialog()
            } else{
                gostart(command3Button.text.toString())
            }
        }

        choiceButton.setOnClickListener {
            showChoiceDialog()
        }

        return view
    }

    private fun translateCommands() {
        viewModel.translateCommands(StoryFragment.commands)
    }

    private fun showEditCommandsDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.edit_commands_dialog)

        val editCommandsEditText = dialog.findViewById<EditText>(R.id.editCommandsEditText)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)

        dialog.setCancelable(false)
        dialog.show()

        btnConfirm.setOnClickListener{
            val newCommand = editCommandsEditText.text.toString()
            val confirmDialog = AlertDialog.Builder(requireContext())
                .setTitle("확인")
                .setMessage("당신의 선택을 후회할 때가 올 수도 있습니다. 이대로 가시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    command3Button.text = newCommand
                    dialog.dismiss()
                    isFirstFunctionExecuted = true
                }
                .setNegativeButton("아니요") { _, _ ->
                    // 아무 작업도 수행하지 않고 대화 상자를 닫습니다.
                    dialog.dismiss()
                }
                .setCancelable(false)
                .create()

            confirmDialog.show()
        }

        btnCancel.setOnClickListener{
            dialog.dismiss()
        }

        /*val dialogView = dialog.window?.decorView?.findViewById<View>(android.R.id.content)
        dialogView?.setOnTouchListener { view, event ->
            val x = event.rawX.toInt()
            val y = event.rawY.toInt()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dialogView.tag = Pair(x, y)
                }
                MotionEvent.ACTION_MOVE -> {
                    val (prevX, prevY) = dialogView.tag as? Pair<Int, Int> ?: return@setOnTouchListener false
                    val deltaX = x - prevX
                    val deltaY = y - prevY

                    dialog.window?.let {
                        val params = it.attributes
                        params.x += deltaX
                        params.y += deltaY
                        it.attributes = params
                    }

                    dialogView.tag = Pair(x, y)
                }
                MotionEvent.ACTION_UP -> {
                    view.performClick()
                }
            }

            true
        }
        */

    }

    // ...

    private fun showChoiceDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 다이얼로그 제목 제거
        dialog.setContentView(R.layout.choice_dialog) // 커스텀 다이얼로그 레이아웃 설정

        val dialogButton = dialog.findViewById<Button>(R.id.dialogButton)
        val closeButton = dialog.findViewById<ImageButton>(R.id.closeButton)

        val thirdCommandValue = translatedCommands.values.elementAtOrNull<String>(2)

        if (StoryFragment.commands.containsKey("3")) {
            dialogButton.text = StoryFragment.commands["3"]
            refreshButton.setOnClickListener{
                if (!isFirst) {
                    isFirst = true
                } else{
                    dialogButton.text = translatedCommands["3"]
                }
            }

            dialogButton.isEnabled = true
            choiceButton.visibility = View.VISIBLE
        } else {
            dialogButton.text = "아직 세상을 바꿀 힘은 충분하지 않나 봅니다."
            dialogButton.isEnabled = false
            choiceButton.visibility = View.GONE
        }

        closeButton.setOnClickListener{
            dialog.dismiss()
        }

        dialogButton.setOnClickListener {
            dialog.dismiss() // 선택 후 다이얼로그 닫기
            gostart(dialogButton.text.toString())
        }

        dialog.show()
    }

    private fun showDialogAndNavigateToStoryFragment(selectedText: String) {
        val confirmDialog = AlertDialog.Builder(requireContext())
            .setTitle("확인")
            .setMessage("당신의 선택을 후회할 때가 올 수도 있습니다. 이대로 가시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                command1Button.isEnabled = false
                command2Button.isEnabled = false
                command3Button.isEnabled = false
                choiceButton.isEnabled = false

            }
            .setNegativeButton("아니요", null)
            .setCancelable(false)
            .create()

        confirmDialog.show()
    }

    fun reset() {
        command1Button.isEnabled = true
        command2Button.isEnabled = true
        command3Button.isEnabled = true
        choiceButton.isEnabled = true
        isFirstFunctionExecuted = false
        isFirst = false
        translatedCommands = emptyMap() // Reset translated commands
        previouscommands = emptyMap() // Reset previous commands
    }

    private fun gostart(selectedText: String) {
        val confirmDialog = AlertDialog.Builder(requireContext())
            .setTitle("확인")
            .setMessage("당신의 선택을 후회할 때가 올 수도 있습니다. 이대로 가시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                val storyActivity = requireActivity() as? StoryActivity
                val storyAdapter = storyActivity?.getStoryAdapter()
                val storyFragment = storyAdapter?.getStoryFragment()
                val viewPager = storyActivity?.getViewPager()

                storyFragment?.let {
                    // 화면 전환을 위해 StoryFragment로 이동
                    viewPager?.setCurrentItem(1, true)

                    // 데이터 전달
                    storyActivity.receiveData(selectedText)

                    it.choiceNext()

                }



                println("=================================")
                println("=================================")
                println("=================================")
                println("=================================")

            }
            .setNegativeButton("아니요", null)
            .setCancelable(false)
            .create()

        confirmDialog.show()
    }



    suspend fun translateCommands(commands: Map<String, String>): Map<String, String> {
        return withContext(Dispatchers.Default) {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(requireContext()))
            }

            val py = Python.getInstance()
            val pyObject = py.getModule("translate")
            pyObject.callAttr("set_openai_api_key", oPENAI_API_KEY)

            val translatedCommands = commands.mapValues { (_, value) ->
                val returnValue = pyObject.callAttr("en2ko", value)
                returnValue.toString()
            }

            return@withContext translatedCommands
        }
    }

    interface ChoiceListener {
        fun onChoiceSelected(choice: String)
    }

}
