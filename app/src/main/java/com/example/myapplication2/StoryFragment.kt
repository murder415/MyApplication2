package com.example.myapplication2

import android.widget.Toast
import android.app.Dialog
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.graphics.BitmapFactory
import android.util.Base64
import android.graphics.Bitmap
import android.content.Context
import android.text.Editable
import androidx.activity.OnBackPressedCallback
import android.widget.LinearLayout

import android.content.res.Resources

import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import me.relex.circleindicator.CircleIndicator3
import android.graphics.Color
import android.widget.EditText
import android.widget.ImageView
import kotlin.collections.joinToString
import kotlin.math.max
import java.io.File
import androidx.appcompat.app.AlertDialog
import android.util.TypedValue
import android.content.SharedPreferences



class StoryFragment : Fragment() {
    private val oPENAI_API_KEY = MainActivity.apiKey
    var toastJob: Job? = null
    private var backButtonPressedTime: Long = 0
    private val BACK_BUTTON_INTERVAL = 2000L // 뒤로 가기 버튼 간격 설정 (2초)


    /*companion object {
        const val TAG = "StoryFragment"
        fun newInstance(): StoryFragment {
            return StoryFragment()
        }
    }


     */
    companion object {
        var description: String = ""
        var commands: Map<String, String> = emptyMap()
        var attributes: String = ""
        var previousDescription: String = ""
        var previousChoice: Map<String, String> = emptyMap()
    }
    var choice: String = ""

    private lateinit var imgGenFragment: ImgGenFragment
    private lateinit var choiceFragment: ChoiceFragment

    var saveApi:String =""
    var saveMsg: MutableList<String> = mutableListOf()
    var saveAtt: String = ""

    private var listener: StoryFragmentListener? = null



    private lateinit var descriptionTextView: TextView
    private lateinit var storyTextView: TextView
    private lateinit var wearingTextView: TextView
    private lateinit var wieldingTextView: TextView

    private lateinit var inventoryButton: ImageButton
    private lateinit var talkButton: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var conditionButton : ImageButton
    private lateinit var closeButton : ImageButton



    private var difficulty: String = ""
    private var abilities: Map<String, String> = emptyMap()
    private var level: String = ""
    private var xp: String = "0"
    private var health: String = ""
    private var weather: String = ""
    private var currentDay: String = ""
    private var timePeriod: String = ""
    private var turnNumber: String = ""
    private var ac: String = ""

    var messages: MutableList<String> = mutableListOf()
    private var location: String = ""
    //private var description: String = ""
    private var gold: String = ""
    private var inventory: Array<String> = arrayOf()
    private var inventoryE: Array<String> = arrayOf()
    private var wearingItems: Array<String> = arrayOf()
    private var wieldingItems: Array<String> = arrayOf()


    private var quest: String = ""
    //private var commands: Map<String, String> = emptyMap()



    private var job: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.story_fragment, container, false)





        descriptionTextView = view.findViewById(R.id.descriptionTextView)
        inventoryButton = view.findViewById(R.id.inventoryButton)
        talkButton = view.findViewById(R.id.talkButton)
        progressBar = view.findViewById(R.id.progressBar)
        conditionButton =  view.findViewById(R.id.conditionButton)
        storyTextView = view.findViewById(R.id.storyTextView)
        wearingTextView = view.findViewById(R.id.wearingText)
        wieldingTextView = view.findViewById(R.id.wieldingText)




        // Inventory 아이콘 클릭 시 InventoryFragment로 이동
        inventoryButton.setOnClickListener {
            showInventoryDialog()
        }

        // Talk 아이콘 클릭 시 ChatbotFragment로 이동
        talkButton.setOnClickListener {
            Kaotalk()
        }

        conditionButton.setOnClickListener {
            showConditionDialog()
        }

        return view
    }


    private fun showInventoryDialog() {
        println("inventory Loop inner")
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.fragment_inventory)

        val closeButton = dialog.findViewById<ImageButton>(R.id.closeButton)
        val inventoryLeftListView = dialog.findViewById<ListView>(R.id.inventoryLeftListView)
        val inventoryRightListView = dialog.findViewById<ListView>(R.id.inventoryRightListView)
        val useButton = dialog.findViewById<Button>(R.id.useButton)
        val discardButton = dialog.findViewById<Button>(R.id.discardButton)
        val emptyTextView = dialog.findViewById<TextView>(R.id.emptyTextView)
        val goldTextView = dialog.findViewById<TextView>(R.id.gold)
        val goldImageView = dialog.findViewById<ImageView>(R.id.goldimage)
        var finalText: String = ""
        val stringBuilder = StringBuilder(finalText)


        goldTextView.text = "50"


        val finalItems = mutableListOf<String>()
        val finalItemscnt = mutableListOf<Int>()

        goldImageView.setOnClickListener {
            val maxGoldQuantity = goldTextView.text.toString().toInt()
            var selectedGoldQuantity = maxGoldQuantity
            if(maxGoldQuantity==0){
                Toast.makeText(requireContext(), "단 한 푼도 얻지 못하였습니다.", Toast.LENGTH_SHORT).show()
            }
            else {
                val dialogGold = Dialog(requireContext())
                dialogGold.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialogGold.setContentView(R.layout.dialog_gold)

                val buttonIncrease = dialogGold.findViewById<ImageButton>(R.id.buttonIncrease)
                val buttonDecrease = dialogGold.findViewById<ImageButton>(R.id.buttonDecrease)
                val buttonConfirm = dialogGold.findViewById<Button>(R.id.buttonConfirm)
                val buttonCancel = dialogGold.findViewById<Button>(R.id.buttonCancel)
                val textViewQuantity = dialogGold.findViewById<EditText>(R.id.textViewQuantity)
                val editTextGold = dialogGold.findViewById<EditText>(R.id.editTextGold)


                val editableFactory = Editable.Factory.getInstance()
                var editableText = editableFactory.newEditable(selectedGoldQuantity.toString())
                textViewQuantity.text = editableText


                buttonIncrease.setOnClickListener {
                    if (selectedGoldQuantity < maxGoldQuantity) {
                        selectedGoldQuantity++
                        editableText = editableFactory.newEditable(selectedGoldQuantity.toString())
                        textViewQuantity.text = editableText
                    }
                    else{
                        Toast.makeText(requireContext(), "당신은 무엇이든 해낼 수 있지만 돈은..", Toast.LENGTH_SHORT).show()

                    }
                }

                buttonDecrease.setOnClickListener {
                    if (selectedGoldQuantity > 0) {
                        selectedGoldQuantity--
                        editableText = editableFactory.newEditable(selectedGoldQuantity.toString())
                        textViewQuantity.text = editableText
                    }
                    else{
                        Toast.makeText(requireContext(), "0골드 이하로 사용하신다고요..?", Toast.LENGTH_SHORT).show()

                    }
                }

                textViewQuantity.setOnClickListener {
                    val inputQuantity = textViewQuantity.text.toString().toIntOrNull()

                    if (inputQuantity != null) {
                        val quantity = when {
                            inputQuantity < 0 -> {
                                // 입력된 값이 0보다 작은 경우
                                Toast.makeText(requireContext(), "최소 0 이상의 값을 입력하세요.", Toast.LENGTH_SHORT).show()
                                0
                            }
                            inputQuantity == 0 -> {
                                // 입력된 값이 0인 경우
                                Toast.makeText(requireContext(), "0골드를 쓰신다고요..?", Toast.LENGTH_SHORT).show()
                                0
                            }
                            inputQuantity > maxGoldQuantity -> {
                                // 입력된 값이 maxGoldQuantity를 초과하는 경우
                                Toast.makeText(requireContext(), "최대 $maxGoldQuantity 까지 입력 가능합니다.", Toast.LENGTH_SHORT).show()
                                maxGoldQuantity
                            }
                            else -> {
                                // 정상적인 범위 내의 값인 경우
                                inputQuantity
                            }
                        }

                        editableText = editableFactory.newEditable(quantity.toString())
                        textViewQuantity.text = editableText
                    }
                }

                var howtouse = editTextGold.text.toString()

                buttonConfirm.setOnClickListener {
                    choice = textViewQuantity.text.toString() + "의 골드를" + howtouse
                    choiceNext()
                    dialogGold.dismiss()
                    dialog.dismiss()
                }

                buttonCancel.setOnClickListener {
                    dialogGold.dismiss()
                }

                dialogGold.show()
            }
        }


        discardButton.setOnClickListener{
            dialog.dismiss()
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        if (inventory.isEmpty() || inventory.all { it.isEmpty() }) {
            Toast.makeText(requireContext(), "인벤토리에 남아있는 것은 없습니다.", Toast.LENGTH_SHORT).show()
            emptyTextView.visibility = View.VISIBLE
            emptyTextView.text = "소지품에 남아있는 것은 아무것도 없습니다.."
            inventoryLeftListView.visibility = View.GONE
            inventoryRightListView.visibility = View.GONE
        } else {
            emptyTextView.visibility = View.GONE
            inventoryLeftListView.visibility = View.VISIBLE
            inventoryRightListView.visibility = View.VISIBLE


            for (item in inventory) {
                println(item)
            }
            val inventoryList = inventoryE.toMutableList() // 불변 리스트를 가변 리스트로 변환

            val leftItems = mutableListOf<String>()
            val rightItems = mutableListOf<String>()

            val leftItemsCnt = mutableListOf<Int>()
            val rightItemsCnt = mutableListOf<Int>()

            var cntList = mutableListOf<Int>()
            val inventoryListE: List<String> = inventoryE.toList()
            cntList = convertToNumbers(inventoryListE)



            println(inventoryList)
            println(cntList)

            inventoryList.forEachIndexed { index, item ->
                if ((index + 1) % 2 == 0) {
                    rightItems.add(item)
                } else {
                    leftItems.add(item)
                }
            }


            cntList.forEachIndexed { index, item ->
                if ((index + 1) % 2 == 0) {
                    rightItemsCnt.add(item)
                } else {
                    leftItemsCnt.add(item)
                }
            }




            println(rightItems)
            println(leftItems)

            val leftAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, leftItems)
            val rightAdapter =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, rightItems)

            inventoryLeftListView.adapter = leftAdapter
            inventoryRightListView.adapter = rightAdapter

            leftAdapter.notifyDataSetChanged()
            rightAdapter.notifyDataSetChanged()


            leftItems.forEachIndexed { index, item ->
                Log.d("Inventory", "Left Item ${index + 1}: $item")
            }

            rightItems.forEachIndexed { index, item ->
                Log.d("Inventory", "Right Item ${index + 1}: $item")
            }

            inventoryLeftListView.setOnItemClickListener { _, _, position, _ ->
                val selectedItem = leftItems[position]
                val selectedItemcnt = leftItemsCnt[position]
                println(finalItems)
                println("-=-------------------------------**")
                println(finalItemscnt)

                if (finalItems.contains(selectedItem)) {
                    // 이미 선택된 아이템이면 선택 취소
                    finalItems.remove(selectedItem)
                    finalItemscnt.remove(selectedItemcnt)

                    // 선택 취소 효과 처리 (기존 효과를 제거하거나 원하는 효과 추가)
                    val selectedView = inventoryLeftListView.getChildAt(position)
                    selectedView.setBackgroundColor(Color.TRANSPARENT) // 예시로 배경을 투명하게 변경
                    val textView = selectedView.findViewById<TextView>(android.R.id.text1)
                    textView.setTextColor(Color.BLACK) // 예시로 글자를 검정색으로 변경
                } else {
                    // 선택되지 않은 아이템이면 선택 처리
                    finalItems.add(selectedItem)
                    finalItemscnt.add(selectedItemcnt)

                    // 선택 효과 처리 (원하는 효과 추가)
                    val selectedView = inventoryLeftListView.getChildAt(position)
                    selectedView.setBackgroundColor(Color.GRAY) // 예시로 회색 배경 적용
                    val textView = selectedView.findViewById<TextView>(android.R.id.text1)
                    textView.setTextColor(Color.WHITE) // 예시로 밝은 글자 적용
                }
            }

            inventoryRightListView.setOnItemClickListener { _, _, position, _ ->
                val selectedItem = rightItems[position]
                val selectedItemcnt = rightItemsCnt[position]

                println(finalItems)
                println("-=-------------------------------**")
                println(finalItemscnt)

                if (finalItems.contains(selectedItem)) {
                    // 이미 선택된 아이템이면 선택 취소
                    finalItems.remove(selectedItem)
                    finalItemscnt.remove(selectedItemcnt)


                    // 선택 취소 효과 처리 (기존 효과를 제거하거나 원하는 효과 추가)
                    val selectedView = inventoryRightListView.getChildAt(position)
                    selectedView.setBackgroundColor(Color.TRANSPARENT) // 예시로 배경을 투명하게 변경
                    val textView = selectedView.findViewById<TextView>(android.R.id.text1)
                    textView.setTextColor(Color.BLACK) // 예시로 글자를 검정색으로 변경
                } else {
                    // 선택되지 않은 아이템이면 선택 처리
                    finalItems.add(selectedItem)
                    finalItemscnt.add(selectedItemcnt)


                    // 선택 효과 처리 (원하는 효과 추가)
                    val selectedView = inventoryRightListView.getChildAt(position)
                    selectedView.setBackgroundColor(Color.GRAY) // 예시로 회색 배경 적용
                    val textView = selectedView.findViewById<TextView>(android.R.id.text1)
                    textView.setTextColor(Color.WHITE) // 예시로 밝은 글자 적용
                }
            }




            useButton.setOnClickListener {
                for (i in finalItems.indices) {
                    if(finalItemscnt[i]==1){
                        if(i!=1) {
                            stringBuilder.append("and")
                        }

                        if (i == finalItems.size - 1) {
                            finalText = stringBuilder.toString()
                            choice = finalText
                            choiceNext()
                            dialog.dismiss()
                        }

                        stringBuilder.append("  use a ${finalItems[i]}")
                    }else {
                        val dialogItemQuantity = Dialog(requireContext())
                        dialogItemQuantity.requestWindowFeature(Window.FEATURE_NO_TITLE)
                        dialogItemQuantity.setContentView(R.layout.dialog_item_quantity)

                        val buttonIncrease =
                            dialogItemQuantity.findViewById<ImageButton>(R.id.buttonIncrease)
                        val buttonDecrease =
                            dialogItemQuantity.findViewById<ImageButton>(R.id.buttonDecrease)
                        val buttonConfirm =
                            dialogItemQuantity.findViewById<Button>(R.id.checkdButton)
                        val buttonCancel =
                            dialogItemQuantity.findViewById<Button>(R.id.cancelButton)
                        val textViewQuantity =
                            dialogItemQuantity.findViewById<TextView>(R.id.textViewQuantity)
                        val editQuantity =
                            dialogItemQuantity.findViewById<EditText>(R.id.editQuantity)

                        var selectedItemCount = 1 // 선택된 아이템의 개수 변수 초기화

                        val editableFactory = Editable.Factory.getInstance()
                        var editableText = editableFactory.newEditable(selectedItemCount.toString())
                        editQuantity.text = editableText

                        textViewQuantity.text = finalItems[i] + "의 최대 갯수 : " + finalItemscnt[i]

                        // 최댓값을 선택된 아이템에 대한 개수로 설정
                        val maxQuantity = finalItemscnt[i]

                        textViewQuantity.text = selectedItemCount.toString()

                        buttonIncrease.setOnClickListener {
                            if (selectedItemCount < maxQuantity) {
                                selectedItemCount++
                                editableText =
                                    editableFactory.newEditable(selectedItemCount.toString())
                                editQuantity.text = editableText
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "최대 사용 개수 이상으로 사용할 수 없습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }

                        buttonDecrease.setOnClickListener {
                            if (selectedItemCount > 1) {
                                selectedItemCount--
                                editableText =
                                    editableFactory.newEditable(selectedItemCount.toString())
                                editQuantity.text = editableText
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "사용을 취소하시려면 취소 버튼을 눌러주세요",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                        }

                        editQuantity.setOnClickListener {
                            val inputQuantity = editQuantity.text.toString().toIntOrNull()

                            if (inputQuantity != null) {
                                val quantity = when {
                                    inputQuantity < 0 -> {
                                        // 입력된 값이 0보다 작은 경우
                                        Toast.makeText(
                                            requireContext(),
                                            "최소 0 이상의 값을 입력하세요.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        0
                                    }

                                    inputQuantity == 0 -> {
                                        // 입력된 값이 0인 경우
                                        Toast.makeText(
                                            requireContext(),
                                            "0골드를 쓰신다고요..?",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        0
                                    }

                                    inputQuantity > maxQuantity -> {
                                        // 입력된 값이 maxGoldQuantity를 초과하는 경우
                                        Toast.makeText(
                                            requireContext(),
                                            "최대 $maxQuantity 까지 입력 가능합니다.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        maxQuantity
                                    }

                                    else -> {
                                        // 정상적인 범위 내의 값인 경우
                                        inputQuantity
                                    }
                                }

                                editableText = editableFactory.newEditable(quantity.toString())
                                editQuantity.text = editableText
                            }
                        }

                        buttonConfirm.setOnClickListener {
                            if(i!=1) {
                                stringBuilder.append("and")
                            }
                            stringBuilder.append("use ${finalItems[i]} x $selectedItemCount")
                            finalText = stringBuilder.toString()

                            // 최종 아이템 처리 (finalText를 사용해야 함)
                            if (i == finalItems.size - 1) {
                                finalText = stringBuilder.toString()
                                choice = finalText
                                choiceNext()

                                dialogItemQuantity.dismiss()
                                dialog.dismiss()
                            }

                            dialogItemQuantity.dismiss()
                        }

                        buttonCancel.setOnClickListener {
                            dialogItemQuantity.dismiss()
                        }
                        dialogItemQuantity.show()

                    }
                }
            }
        }


        dialog.show()
    }


    private fun convertToNumbers(inventoryListE: List<String>): MutableList<Int> {
        val wordToNumberMap = mapOf(
            "one" to 1,
            "two" to 2,
            "three" to 3,
            "four" to 4,
            "five" to 5,
            "six" to 6,
            "seven" to 7,
            "eight" to 8,
            "nine" to 9,
            "ten" to 10
        )

        val finalList = mutableListOf<Int>()

        for (item in inventoryListE) {
            val words = item.split(" ")
            var numberFound = false
            for (word in words) {
                val lowerCaseWord = word.toLowerCase()
                if (lowerCaseWord in wordToNumberMap) {
                    val number = wordToNumberMap[lowerCaseWord]!!
                    finalList.add(number)
                    numberFound = true
                    break
                }
                if (lowerCaseWord.toIntOrNull() != null) {
                    val number = lowerCaseWord.toInt()
                    finalList.add(number)
                    numberFound = true
                    break
                }
            }
            if (!numberFound) {
                finalList.add(1)
            }
        }

        return finalList
    }



    private fun showConditionDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // 다이얼로그 제목 제거
        dialog.setContentView(R.layout.condition) // 커스텀 다이얼로그 레이아웃 설정



        // 커스텀 다이얼로그 내부의 뷰들을 찾아서 초기화
        val difficultyTextView = dialog.findViewById<TextView>(R.id.difficultyTextView)
        val persuasionTextView = dialog.findViewById<TextView>(R.id.persuasionTextView)
        val strengthTextView = dialog.findViewById<TextView>(R.id.strengthTextView)
        val intelligenceTextView = dialog.findViewById<TextView>(R.id.intelligenceTextView)
        val dexterityTextView = dialog.findViewById<TextView>(R.id.dexterityTextView)
        val luckTextView = dialog.findViewById<TextView>(R.id.luckTextView)
        val levelTextView = dialog.findViewById<TextView>(R.id.levelTextView)
        val xpProgressBar = dialog.findViewById<ProgressBar>(R.id.xpProgressBar)
        val healthTextView = dialog.findViewById<TextView>(R.id.healthTextView)
        val weatherTextView = dialog.findViewById<TextView>(R.id.weatherTextView)
        val weatherTextView2 = dialog.findViewById<TextView>(R.id.weatherTextView2)

        val dayNumberTextView = dialog.findViewById<TextView>(R.id.dayNumberTextView)
        val timePeriodTextView = dialog.findViewById<TextView>(R.id.timePeriodTextView)
        val turnNumberTextView = dialog.findViewById<TextView>(R.id.turnNumberTextView)
        val closeButton = dialog.findViewById<ImageButton>(R.id.closeButton)

        // closeButton 클릭 시 다이얼로그 닫기




        // 다이얼로그에 데이터 설정 및 표시
        difficultyTextView.text = "Difficulty : " + difficulty
        persuasionTextView.text = "리더십   "+abilities["Persuasion"]
        strengthTextView.text = "힘   " + abilities["Strength"]
        intelligenceTextView.text = "지능   " +abilities["Intelligence"]
        dexterityTextView.text = "손재주   "+abilities["Dexterity"]
        luckTextView.text = "행운   "+abilities["Luck"]
        levelTextView.text = level
        xpProgressBar.progress = xp.toInt()
        healthTextView.text = health
        weatherTextView2.text = ""
        weatherTextView.text = weather
        dayNumberTextView.text = currentDay
        if(timePeriod.length > 10){
            timePeriodTextView.textSize = 8.0.toFloat()
        }
        timePeriodTextView.text = timePeriod
        if( weather.length > 10){
            if(weather.length > 20 ){
                weatherTextView2.text = weather
                weatherTextView.text = ""
            }
            weatherTextView.textSize = 7.0.toFloat()
        }

        turnNumberTextView.text = turnNumber

        dialog.setCancelable(false) // 다이얼로그가 취소되지 않도록 설정

        dialog.show()


        closeButton.setOnClickListener {
            dialog.dismiss()
        }

// 다이얼로그 레이아웃의 최상위 뷰에 터치 이벤트 처리기 설정
        val dialogView = dialog.window?.decorView?.findViewById<View>(android.R.id.content)
        dialogView?.setOnTouchListener { view, event ->
            val x = event.rawX.toInt()
            val y = event.rawY.toInt()

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 터치가 시작되면 현재 위치를 저장합니다.
                    dialogView.tag = Pair(x, y)
                }
                MotionEvent.ACTION_MOVE -> {
                    // 이동한 거리를 계산하여 다이얼로그 창의 위치를 업데이트합니다.
                    val (prevX, prevY) = dialogView.tag as? Pair<Int, Int> ?: return@setOnTouchListener false
                    val deltaX = x - prevX
                    val deltaY = y - prevY

                    dialog.window?.let {
                        val params = it.attributes
                        params.x += deltaX
                        params.y += deltaY
                        it.attributes = params
                    }

                    dialogView.tag = Pair(x, y) // 현재 위치를 업데이트합니다.
                }
                MotionEvent.ACTION_UP -> {
                    // 클릭 이벤트를 처리합니다.
                    view.performClick()
                }
            }

            true
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (isDataFileExists()) {
            val data = readDataFromFile()
            val (loadedAttributes, loadedMessages) = parseData(data)

            attributes = loadedAttributes
            handleResult(loadedAttributes)
            messages = loadedMessages
        } else {
            startGame()
        }
    }
    private fun isDataFileExists(): Boolean {
        val file = File(requireContext().filesDir, "data.txt")
        return file.exists()
    }

    private fun readDataFromFile(): String {
        val file = File(requireContext().filesDir, "data.txt")
        return file.readText()
    }

    private fun parseData(data: String): Pair<String, MutableList<String>> {
        val delimiter = "<!@!>"
        val parts = data.split(delimiter)
        val loadedAttributes = parts[2]
        val loadedMessages = parts.subList(1, 2)
        return Pair(loadedAttributes, loadedMessages.toMutableList())
    }


    override fun onDestroy() {
        super.onDestroy()
        job?.cancel() // Coroutine 종료
    }


    private fun startGame() {
        if (job?.isActive == true) return

        job = CoroutineScope(Dispatchers.Main).launch {
            progressBar.visibility = View.VISIBLE

            // MakeStoryFragment에서 전달한 주제를 storySummary로 전달
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(requireContext()))
            }


            val obj = withContext(Dispatchers.IO) {
                val py = Python.getInstance()
                val pyObject = py.getModule("textAdventure")
                val pyResult = pyObject.callAttr("main", MakeStoryFragment.gameOutput)
                val messagesList = pyResult.asList().map { it.toString() }
                messages = messagesList.toMutableList()
                attributes = pyObject.callAttr("return_attributes").toString()
                val answer = pyObject.callAttr("return_answer").toString()
                Pair(attributes, answer)
            }

            attributes = obj.first

            println("attributes\n")
            handleResult(attributes)
            val answer = obj.second
            println("answer\n")
            println(answer)

        }
    }



    fun setListener(listener: StoryFragmentListener) {
        this.listener = listener
    }

    fun choiceNext() {
        println("시작한다!!")
        listener?.initializeAndNavigateToStoryFragment()

        job = CoroutineScope(Dispatchers.Main).launch {
            progressBar.visibility = View.VISIBLE

            // MakeStoryFragment에서 전달한 주제를 storySummary로 전달
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(requireContext()))
            }

            val obj = withContext(Dispatchers.IO) {
                val py = Python.getInstance()
                val pyObject = py.getModule("textAdventure")
                val pyMessages = messages.toTypedArray()
                val pyResult = pyObject.callAttr("get_input", choice , pyMessages)
                val messagesList = pyResult.asList().map { it.toString() }
                messages = messagesList.toMutableList()
                attributes = pyObject.callAttr("return_attributes").toString()
                val answer = pyObject.callAttr("return_answer").toString()
                Pair(attributes, answer)
            }

            attributes = obj.first
            handleResult(attributes)
            val answer = obj.second
            println("answer\n")
            println(answer)

            progressBar.visibility = View.GONE

        }
    }

    fun handleResult(result: String) {
        previousChoice = commands
        previousDescription = description
        commands = emptyMap()
        description = ""
        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {

                // JSON 문자열을 Map으로 변환
                val data: Map<String, Any> = parseJson(result)


                // 필요한 데이터 추출
                turnNumber = data["Turn number"] as? String ?: ""
                difficulty = data["Difficulty"] as? String ?: ""
                timePeriod = data["Time period of the day"] as? String ?: ""
                currentDay = data["Current day number"] as? String ?: ""
                weather = data["Weather"] as? String ?: ""
                health = data["Health"] as? String ?: ""
                xp = data["XP"] as? String ?: ""
                ac = data["AC"] as? String ?: ""
                level = data["Level"] as? String ?: ""

                location = data["Location"] as? String ?: ""
                gold = data["Gold"] as? String ?: ""

                description = data["Description"] as? String ?: ""

                val inventoryData = data["Inventory"] as? List<String>
                inventory = inventoryData?.toTypedArray() ?: emptyArray()
                wearingItems = inventory.filter { it.contains("wearing", ignoreCase = true) }.toTypedArray()
                wieldingItems = inventory.filter { it.contains("wielding", ignoreCase = true) }.toTypedArray()
                quest = data["Quest"] as? String ?: ""
                abilities = data["Abilities"] as? Map<String, String> ?: emptyMap()
                commands = data["Possible Commands"] as? Map<String, String> ?: emptyMap()
                val story: String = data["Story"] as? String ?: ""
                println(inventory)


                val item = "three bottle of water"
                inventoryE
                inventoryE = inventory + item
                if (wearingItems.isEmpty() || wearingItems.all { it.isBlank() }) {
                    // wearingItems가 비어 있거나 모든 요소가 빈 문자열일 때의 처리
                    println("wearingItems is empty or contains only empty strings")
                } else {
                    val wearingText = wearingItems.firstOrNull() ?: ""
                    animateTextWear(wearingText)
                }

                if (wieldingItems.isEmpty() || wieldingItems.all { it.isBlank() }) {
                    // wearingItems가 비어 있거나 모든 요소가 빈 문자열일 때의 처리
                    println("wieldingItems is empty or contains only empty strings")
                } else {
                    val wieldingText = wieldingItems.firstOrNull() ?: ""
                    animateTextWield(wieldingText)
                }



                val py = Python.getInstance()
                val pyObject = py.getModule("translate")

                var typeText2 = description
                println("typeText2")

                println(typeText2)
                val obj4 = pyObject.callAttr("en2ko", typeText2)
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "배경 이야기를 생성중입니다.", Toast.LENGTH_SHORT).show()
                }
                typeText2 = obj4.toString()
                animateTextD(typeText2)



                var typedText = story
                println("typeText")

                if ("None".equals(typedText)) {
                    // typedText가 "None"인 경우에 실행할 코드
                    println("typedText is None");
                } else {
                    // typedText가 "None"이 아닌 경우에 실행할 코드
                    println("typedText is not None")
                    val obj = pyObject.callAttr("en2ko", typedText)
                    typedText = obj.toString()
                    animateText(typedText)
                }




                val obj = pyObject.callAttr("en2ko", timePeriod)



                val obj2 = pyObject.callAttr("en2ko", location)



                val obj3 = pyObject.callAttr("en2ko", weather)



                val translations = mutableListOf<String>()
                for (item in inventory) {
                    val translation = pyObject.callAttr("en2ko", item).toString()

                    translations.add(translation)
                }

                println("+++++++++++++++++++++++++++++++++++++++++++")
                for (item in inventory) {
                    println(item)
                }
                println("+++++++++++++++++++++++++++++++++++++++++++")

                val translatedInventory = translations.toTypedArray()


                // 결과를 가지고 추가 작업 수행

                // UI 업데이트는 Dispatchers.Main을 사용하여 메인 스레드에서 수행


                // UI 업데이트 등을 위해 메인 스레드로 전환하여 실행하는 코드 작성
                timePeriod = obj.toString()
                location = obj2.toString()
                weather = obj3.toString()

                inventory = translatedInventory


                // 추출된 데이터 사용
                // ...

                // 예시: 데이터 출력


                conditionButton.setOnClickListener {

                    showConditionDialog()

                }

                inventoryButton.setOnClickListener {

                    showInventoryDialog()

                }

            }
        }


    }

    fun startorstop(action: String, number: Int, context: Context) {
        when (action) {
            "start" -> {
                // Check if the toastJob is already running
                if (toastJob == null || toastJob?.isActive != true) {
                    toastJob = CoroutineScope(Dispatchers.Main).launch {
                        while (isActive) {
                            showToast(number, context)
                            delay(10000L) // Delay for 10 seconds
                        }
                    }
                }
            }
            "stop" -> {
                // Cancel the toastJob if it is running
                toastJob?.cancel()
                toastJob = null
            }
        }
    }

    fun showToast(number: Int, context: Context) {
        val message = when (number) {
            1 -> "누군가 살아남길 시도했지만... 실패한 이야기"
            else -> ""
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun parseJson(jsonString: String): Map<String, Any> {
        val gson = Gson()
        val type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(jsonString, type)
    }





    private fun animateText(script: String) {

        var text = script
        println(script)


        val typingDelay: Long = 50
        val mediaPlayer = MediaPlayer.create(requireContext(), R.raw.typing_sound)

        var currentIndex = 0
        var isTyping = false
        lifecycleScope.launch(Dispatchers.Main) {

            progressBar.visibility = View.GONE
        }


        val handler = Handler(Looper.getMainLooper())
        val spannableBuilder = SpannableStringBuilder()

        fun startTyping() {
            isTyping = true // 초기화 위치 변경

            if (currentIndex < text.length) {
                spannableBuilder.append(text[currentIndex].toString())
                lifecycleScope.launch(Dispatchers.Main) {

                    storyTextView.text = spannableBuilder
                }
                currentIndex++
                if (currentIndex == 1) {
                    mediaPlayer.start() // 타이핑 사운드 재생
                }

                handler.postDelayed({
                    startTyping()
                }, typingDelay)
            } else {
                isTyping = false
                mediaPlayer.pause() // 타이핑 사운드 일시 정지
            }
        }

        startTyping()
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


            imgGenFragment = ImgGenFragment()
            imgGenFragment.updateDescription(description, bitmap)


            return@withContext bitmap
        }
    }



    fun setData(data: String) {
        choice = data
    }

    private fun animateTextD(script: String) {

        println(script)

        var text = script

        println(text)

        val typingDelay: Long = 50
        val mediaPlayer = MediaPlayer.create(requireContext(), R.raw.typing_sound)

        var currentIndex = 0
        var isTyping = false

        val handler = Handler(Looper.getMainLooper())
        val spannableBuilder = SpannableStringBuilder()
        lifecycleScope.launch(Dispatchers.Main) {

            progressBar.visibility = View.GONE
        }


        fun startTyping() {
            isTyping = true // 초기화 위치 변경

            if (currentIndex < text.length) {
                spannableBuilder.append(text[currentIndex].toString())
                lifecycleScope.launch(Dispatchers.Main) {

                    descriptionTextView.text = spannableBuilder
                }
                currentIndex++
                if (currentIndex == 1) {
                    mediaPlayer.start() // 타이핑 사운드 재생
                }

                handler.postDelayed({
                    startTyping()
                }, typingDelay)
            } else {
                isTyping = false
                mediaPlayer.pause() // 타이핑 사운드 일시 정지
            }
        }

        startTyping()
    }

    private fun animateTextWield(script: String) {

        println(script)

        var text = script

        println(text)

        val typingDelay: Long = 50
        val mediaPlayer = MediaPlayer.create(requireContext(), R.raw.typing_sound)

        var currentIndex = 0
        var isTyping = false

        val handler = Handler(Looper.getMainLooper())
        val spannableBuilder = SpannableStringBuilder()
        lifecycleScope.launch(Dispatchers.Main) {

            progressBar.visibility = View.GONE
        }


        fun startTyping() {
            isTyping = true // 초기화 위치 변경

            if (currentIndex < text.length) {
                spannableBuilder.append(text[currentIndex].toString())
                lifecycleScope.launch(Dispatchers.Main) {

                    wieldingTextView.text = spannableBuilder
                }
                currentIndex++
                if (currentIndex == 1) {
                    mediaPlayer.start() // 타이핑 사운드 재생
                }

                handler.postDelayed({
                    startTyping()
                }, typingDelay)
            } else {
                isTyping = false
                mediaPlayer.pause() // 타이핑 사운드 일시 정지
            }
        }

        startTyping()
    }

    private fun animateTextWear(script: String) {

        println(script)

        var text = script

        println(text)

        val typingDelay: Long = 50
        val mediaPlayer = MediaPlayer.create(requireContext(), R.raw.typing_sound)

        var currentIndex = 0
        var isTyping = false

        val handler = Handler(Looper.getMainLooper())
        val spannableBuilder = SpannableStringBuilder()
        lifecycleScope.launch(Dispatchers.Main) {

            progressBar.visibility = View.GONE
        }


        fun startTyping() {
            isTyping = true // 초기화 위치 변경

            if (currentIndex < text.length) {
                spannableBuilder.append(text[currentIndex].toString())
                lifecycleScope.launch(Dispatchers.Main) {

                    wearingTextView.text = spannableBuilder
                }
                currentIndex++
                if (currentIndex == 1) {
                    mediaPlayer.start() // 타이핑 사운드 재생
                }

                handler.postDelayed({
                    startTyping()
                }, typingDelay)
            } else {
                isTyping = false
                mediaPlayer.pause() // 타이핑 사운드 일시 정지
            }
        }

        startTyping()
    }

    fun reset() {
        // 프로퍼티 초기화
        description = ""
        commands = emptyMap()
        attributes = ""
        choice = ""
        saveApi = ""
        saveMsg.clear()
        saveAtt = ""
        difficulty = ""
        abilities = emptyMap()
        level = ""
        xp = "0"
        health = ""
        weather = ""
        currentDay = ""
        timePeriod = ""
        turnNumber = ""
        ac = ""
        messages.clear()
        location = ""
        gold = ""
        inventory = emptyArray()
        inventoryE = emptyArray()
        wearingItems = emptyArray()
        wieldingItems = emptyArray()
        quest = ""

        // 뷰 상태 초기화
        descriptionTextView.text = ""
        storyTextView.text = ""
        wearingTextView.text = ""
        wieldingTextView.text = ""
        // 기타 뷰 상태 초기화 작업


        // 기타 초기화 작업
        // ...

        // 작업 중인 코루틴 취소
        job?.cancel()
        toastJob?.cancel()
    }


    override fun onResume() {
        super.onResume()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, onBackPressedCallback)
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onBackPressed()
        }
    }
    private fun onBackPressed() {
        val currentTime = System.currentTimeMillis()

        val newMessages = messages // 현재 Fragment로부터 messages를 가져옴
        val newApiKey = MainActivity.apiKey
        val newAttributes = attributes// MainActivity로부터 apiKey를 가져옴

        if (currentTime - backButtonPressedTime > BACK_BUTTON_INTERVAL) {
            // 뒤로 가기 버튼을 처음 누른 경우
            backButtonPressedTime = currentTime
            Toast.makeText(requireContext(), "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        } else {
            // 뒤로 가기 버튼을 연속으로 두 번 누른 경우
            if (newMessages == saveMsg && newApiKey == saveApi && newAttributes == saveAtt) {
                showExitConfirmationDialog() // messages와 apiKey의 변화가 없으면 종료 여부를 묻는 다이얼로그 창을 보여줌
            } else {
                saveData() // messages나 apiKey가 변경되었으면 데이터 저장 실행
            }
        }

    }

    private fun getSavedViewInfo(): Pair<Int, Int> {
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val imageViewCount = sharedPreferences.getInt("imageViewCount", 0)
        val linearLayoutCount = sharedPreferences.getInt("linearLayoutCount", 0)
        return Pair(imageViewCount, linearLayoutCount)
    }

    // 뷰 정보를 저장하는 함수
    private fun saveViewInfo(imageViewCount: Int, linearLayoutCount: Int) {
        val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("imageViewCount", imageViewCount)
        editor.putInt("linearLayoutCount", linearLayoutCount)
        editor.apply()
    }

    fun Kaotalk() {
        val profilePhoto = "촌장님" // 인물 프로필 사진
        var i = 0

        //val savedViewInfo = getSavedViewInfo()
        //val savedImageViewCount = savedViewInfo.first
        //val savedLinearLayoutCount = savedViewInfo.second


        // Dialog를 생성하여 채팅방 목록을 표시
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.chat_rooms_dialog) // Dialog의 레이아웃 파일 설정


        val parentLayout = dialog.findViewById<LinearLayout>(R.id.parentLayout)
        val chatLayout = dialog.findViewById<LinearLayout>(R.id.chatLayout)
        val normalLayout = dialog.findViewById<LinearLayout>(R.id.normallayout)

        val plusButton = dialog.findViewById<ImageView>(R.id.addChatRoomButton)
        val addChatRoomButton: ImageView = parentLayout.findViewById(R.id.addChatRoomButton)

        plusButton.setOnClickListener {
            val newImageView = ImageView(requireContext())
            newImageView.layoutParams = LinearLayout.LayoutParams(50.dp, 50.dp)
            newImageView.setImageResource(R.drawable.profile_photo)
            newImageView.tag = "newImageView$i" // 태그에 번호 추가
            parentLayout.removeView(addChatRoomButton)
            parentLayout.addView(newImageView)
            parentLayout.addView(addChatRoomButton)

            val newLinearLayout = LinearLayout(requireContext())
            newLinearLayout.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 50.dp, 1f)
            newLinearLayout.orientation = LinearLayout.VERTICAL
            newLinearLayout.tag = "newLinearLayout$i" // 태그에 번호 추가
            chatLayout.addView(newLinearLayout)

            val chatRoomName = TextView(requireContext())
            chatRoomName.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                30.dp
            )
            chatRoomName.text = "채팅방 상대 이름"
            chatRoomName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            chatRoomName.typeface = resources.getFont(R.font.scdream9)
            newLinearLayout.addView(chatRoomName)

            val recentMessage = TextView(requireContext())
            recentMessage.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                70.dp
            )
            recentMessage.text = "가장 최근에 이야기한 내용"
            recentMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            recentMessage.typeface = resources.getFont(R.font.scdream7)
            newLinearLayout.addView(recentMessage)

            newImageView.setOnClickListener {
                showProfileInfo()

                // 이미지 뷰 클릭 시 동작할 내용을 작성하세요.
                // 예: 해당 채팅방 상세 정보 표시 또는 다른 동작 수행
                Toast.makeText(requireContext(), "이미지 뷰 클릭", Toast.LENGTH_SHORT).show()
            }

            newLinearLayout.setOnClickListener {
                // 레이아웃 클릭 시 동작할 내용을 작성하세요.
                // 예: 해당 채팅방으로 이동 또는 다른 동작 수행
                Toast.makeText(requireContext(), "레이아웃 클릭", Toast.LENGTH_SHORT).show()
            }
            i++
        }

        dialog.show()

        // + 이미지 버튼 클릭 시 새로운 채팅방 생성
        normalLayout.setOnClickListener {
            createNewChatRoom()
        }

        // 인물 프로필 사진 클릭 시 프로필 정보 Dialog 실행
        val profilePhotoImageView = dialog.findViewById<ImageView>(R.id.profilePhotoImageView)
        profilePhotoImageView.setOnClickListener {
            showProfileInfo()
        }
    }



    fun openChatRoom(chatRoom: String) {
        // 선택한 채팅방으로 이동하는 로직을 구현
    }

    val Int.dp: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()
    fun createNewChatRoom() {
        // 새로운 채팅방을 생성하는 로직을 구현
    }

    fun showProfileInfo() {
        val profileInfoDialog = Dialog(requireContext())
        profileInfoDialog.setContentView(R.layout.profile_info_dialog) // 프로필 정보 Dialog의 레이아웃 파일 설정

        val ImageView =  profileInfoDialog.findViewById<ImageView>(R.id.characterFaceImageView)

        val NameText =  profileInfoDialog.findViewById<TextView>(R.id.nameTextView2)
        val gender1 =  profileInfoDialog.findViewById<ImageView>(R.id.genderIconImageView1)
        val gender2 =  profileInfoDialog.findViewById<ImageView>(R.id.genderIconImageView2)
        val AgeText =  profileInfoDialog.findViewById<TextView>(R.id.ageTextView2)
        val personalityText =  profileInfoDialog.findViewById<TextView>(R.id.personalityTextView2)

        val roleText =  profileInfoDialog.findViewById<TextView>(R.id.roleTextView2)
        val hobbyText =  profileInfoDialog.findViewById<TextView>(R.id.hobbyTextView2)
        val locationText = profileInfoDialog.findViewById<TextView>(R.id.locationTextView2)

        val closeButton = profileInfoDialog.findViewById<ImageButton>(R.id.closeButton)

        ImageView.setImageResource(R.drawable.example)


        closeButton.setOnClickListener{
            profileInfoDialog.dismiss()
        }

        lifecycleScope.launch {
            if (!Python.isStarted()) {
                Python.start(AndroidPlatform(requireContext()))
            }


            val obj = withContext(Dispatchers.IO) {
                val py = Python.getInstance()
                val pyObject = py.getModule("character")
                pyObject.callAttr("main", MakeStoryFragment.gameOutput)
                val attributes2 = pyObject.callAttr("return_attributes")
                attributes2
            }
            val data: Map<String, Any> = parseJson(obj.toString())

            val Name = data["Name"] as? String ?: ""
            val Age = data["Age"] as? String ?: ""
            val Gender = data["Gender"] as? String ?: ""
            val Hobbies = data["Hobbies"] as? String ?: ""
            val Personality = data["Personality"] as? String ?: ""
            val Role = data["Role"] as? String ?: ""
            val Location = data["Location"] as? String ?: ""


            NameText.text = Name
            AgeText.text = Age
            if(Gender=="female"){
                gender1.visibility = View.VISIBLE
                gender2.visibility = View.GONE
            }
            else{
                gender2.visibility = View.VISIBLE
                gender1.visibility = View.GONE
            }

            hobbyText.text = Hobbies
            personalityText.text = Personality
            roleText.text = Role
            locationText.text = Location
        }


        // 프로필 정보를 표시하는 로직을 구현

        profileInfoDialog.show()
    }

    private fun saveData() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("데이터 저장")
            .setMessage("현재 데이터를 저장하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                val apiKey = MainActivity.apiKey // MainActivity의 apiKey 가져오기
                val messages = messages // 현재 StoryFragment의 messages 가져오기
                val attributes = attributes

                if (apiKey != null && messages != null && attributes != null) {
                    saveApi = apiKey
                    saveMsg = messages
                    saveAtt = attributes

                    // 데이터 저장
                    val delimiter = "<!@!>"

                    val data = "$apiKey$delimiter$messages$delimiter$attributes"

                    saveDataToFile(data)
                    requireActivity().finish() // 전체 앱 종료
                } else {
                    Toast.makeText(requireContext(), "올바른 데이터 형식이 아닙니다.", Toast.LENGTH_SHORT).show()
                }

            }
            .setNegativeButton("아니요") { _, _ ->
                // 앱 종료
                showExitConfirmationDialog()
            }
            .setCancelable(false)
            .create()

        alertDialog.show()
    }

    private fun saveDataToFile(data: String) {
        try {
            val filename = "data.txt"
            val fileContents = data

            val file = File(requireContext().filesDir, filename)
            file.writeText(fileContents)

            Toast.makeText(requireContext(), "데이터가 저장되었습니다.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "데이터 저장에 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun showExitConfirmationDialog() {
        val alertDialog = AlertDialog.Builder(requireContext())
            .setTitle("앱 종료")
            .setMessage("추가적으로 저장된 데이터가 없습니다. 앱을 종료하시겠습니까?")
            .setPositiveButton("예") { _, _ ->
                requireActivity().finish() // 전체 앱 종료
            }
            .setNegativeButton("아니요") { dialog, _ ->
                dialog.dismiss() // 다이얼로그 창만 닫음
            }
            .create()

        alertDialog.show()
    }




}

