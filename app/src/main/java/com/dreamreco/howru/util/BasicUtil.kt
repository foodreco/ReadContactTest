package com.dreamreco.howru.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.Window
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
import android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.dreamreco.howru.R
import com.dreamreco.howru.data.entity.CallLogData
import com.dreamreco.howru.data.entity.ContactBase
import java.text.SimpleDateFormat


@SuppressLint("SimpleDateFormat")
fun convertLongToDateString(systemTime: Long): String {
    val simpleDateFormat = SimpleDateFormat("yyyy년 MM월 dd일")
    return simpleDateFormat.format(systemTime)
}

@SuppressLint("SimpleDateFormat")
fun convertLongToMonthLong(systemTime: Long): Long {
    val simpleDateFormat = SimpleDateFormat("yyMM")
    return simpleDateFormat.format(systemTime).toLong()
}

fun convertLongToTimeString(systemTime: Long): String {
    val minutes = systemTime / 60
    val seconds = systemTime % 60
    // 통화시간 1분 미만은 초 단위로만 출력
    return when (systemTime) {
        in 0..1 -> "-"
        in 1..59 -> "${seconds}초"
        else -> "${minutes}분 ${seconds}초"
    }
}

fun convertCallTypeToString(callType: Int): String {
    return when (callType) {
        1 -> "수신"
        2 -> "발신"
        3 -> "부재중"
        else -> "미상"
    }
}

fun setUpImageWithConvertCallType(imageView: ImageView, callType: Int, context: Context) {
    when (callType) {
        1 -> {
            imageView.setImageResource(R.drawable.ic_round_phone_callback_12)
            imageView.setColorFilter(ContextCompat.getColor(context, R.color.hau_bright_blue))
        }
        2 -> {
            imageView.setImageResource(R.drawable.ic_round_phone_forwarded_12)
            imageView.setColorFilter(ContextCompat.getColor(context, R.color.hau_green))
        }
        3 -> {
            imageView.setImageResource(R.drawable.ic_round_phone_missed_12)
            imageView.setColorFilter(ContextCompat.getColor(context, R.color.red))
        }
        else -> {
            imageView.setImageResource(R.drawable.ic_baseline_block_12)
            imageView.setColorFilter(ContextCompat.getColor(context, R.color.black))
        }
    }
}


fun EditText.setFocusAndShowKeyboard(context: Context) {
    this.requestFocus()
    setSelection(this.text.length)
    this.postDelayed({
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_FORCED)
    }, 100)
}

fun EditText.clearFocusAndHideKeyboard(context: Context) {
    this.clearFocus()
    this.postDelayed({
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
    }, 30)
//    val inputMethodManager =
//        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//    inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
}

fun Button.clearFocusAndHideKeyboard(context: Context) {
    this.clearFocus()
    this.postDelayed({
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(this.windowToken, 0)
    }, 30)
}


// 전화 통계, 통화기록을 불러오는 함수(ContactInfo,CallLogData) 에 사용
data class ContactSpl(
    val id: String,
    val name: String,
    val number: String,
    val duration: String
)

// CallLogCalls 데이터를 순회하며, Recommendation 정보를 가져오는 함수
data class RecommendationSpl(
    var name: String,
    var number: String,
    var group: String,
    var recentContact: String?,
    var totalCallTime: String?,
    var numberOfCalling: String?,
)

// groupList Top Data 용 클래스
data class GroupTopData(
    var name: String,
    var number: String,
    var duration: Long,
    var times: Int
)

// CombinedChart 용 전용 Data
data class CombinedChartData(
    var month: Long,
    var times: Int,
    var duration: Long
)


// CallLog, GroupDetail Adapter 에서 헤더용으로 사용된 sealed class
sealed class CallLogItem {
    abstract val callLog: CallLogData
    abstract val layoutId: Int

    // 헤더
    data class Header(
        override val callLog: CallLogData,
        override val layoutId: Int = VIEW_TYPE
    ) : CallLogItem() {

        companion object {
            const val VIEW_TYPE = R.layout.call_log_header
        }
    }

    // view
    data class Item(
        override val callLog: CallLogData,
        override val layoutId: Int = VIEW_TYPE
    ) : CallLogItem() {

        companion object {
            const val VIEW_TYPE = R.layout.fragment_call_log_child
        }
    }
}

// 맞춤 차트 그래프 색
val CUSTOM_CHART_COLORS = arrayListOf<Int>(
    Color.rgb(0, 205, 62), Color.rgb(0, 144, 205), Color.rgb(0, 112, 64), Color.rgb(0, 205, 164),
    Color.rgb(0, 94, 152), Color.rgb(9, 0, 197),
    Color.rgb(0, 113, 0),
    Color.rgb(0, 100, 54),
    Color.rgb(0, 168, 0),
    Color.rgb(0, 147, 97),
    Color.rgb(97, 24, 219)
)

// ListFragment Adapter 에서 헤더용으로 사용된 sealed class
sealed class ContactBaseItem {
    abstract val contactBase: ContactBase
    abstract val layoutId: Int

    // 헤더
    data class Header(
        override val contactBase: ContactBase,
        override val layoutId: Int = VIEW_TYPE
    ) : ContactBaseItem() {

        companion object {
            const val VIEW_TYPE = R.layout.call_log_header
        }
    }

    // view
    data class Item(
        override val contactBase: ContactBase,
        override val layoutId: Int = VIEW_TYPE
    ) : ContactBaseItem() {

        companion object {
            const val VIEW_TYPE = R.layout.contact_child
        }
    }
}


// 한글 텍스트 초성을 추출하는 함수
fun transformingToInitialSpell(targetText: String): String {
    var returnText = ""
    // 한글 초성 가져오기를 위한 초성 배열코드
    val korConsonant = arrayOf(
        "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ",
        "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ",
        "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ",
        "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    )
    // textContent 를 Char 형태로 변환
    val text = targetText.toCharArray()[0]
    // 텍스트 언어 판별
    when (checkTextType(text)) {
        // 한글일 때
        "kor" -> {
            if (text.code >= 0xAC00) {
                val uniVal = text.code - 0xAC00
                val cho = (uniVal - uniVal % 28) / 28 / 21
                val result = korConsonant[cho]
                returnText = result
            }
        }
        // 한글이 아닐 때
        else -> {
            returnText = text.toString()
        }
    }
    return returnText
}

// 텍스트 언어를 판별하는 함수
fun checkTextType(ch: Char): String {
    var returnText: String = ""

    if (ch in 'A'..'Z' || ch in 'a'..'z') {
        returnText = "eng"
    }
    if (ch in 'ㄱ'..'ㅣ' || ch in '가'..'힣') {
        returnText = "kor"
    }
    if (ch in '0'..'9') {
        returnText = "num"
    }
    if (ch in '!'..'/' || ch in ':'..'@' || ch in '['..'`' || ch in '{'..'~') {
        returnText = "spe"
    }
    return returnText
}


// windowSoftInputMode 를 제어하는 코드
// manifest 지정 모드 : adjustPan

fun Window?.getSoftInputMode(): Int {
    return this?.attributes?.softInputMode ?: SOFT_INPUT_ADJUST_PAN
}

class InputModeLifecycleHelper(
    private var window: Window?,
    private val mode: Mode = Mode.ADJUST_RESIZE
) : LifecycleObserver {

    private var originalMode: Int = SOFT_INPUT_ADJUST_PAN

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun setNewSoftInputMode() {
        window?.let {
            originalMode = it.getSoftInputMode()

            it.setSoftInputMode(
                when (mode) {
                    Mode.ADJUST_RESIZE -> WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                    Mode.ADJUST_PAN -> WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
                }
            )
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun restoreOriginalSoftInputMode() {
        if (originalMode != SOFT_INPUT_ADJUST_UNSPECIFIED) {
            window?.setSoftInputMode(originalMode)
        }
        window = null
    }

    enum class Mode {
        ADJUST_RESIZE, ADJUST_PAN
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////
// // windowSoftInputMode 를 제어하는 코드 deprecated 대비형
//fun Window?.getSoftInputMode(): Int {
//    return this?.attributes?.softInputMode ?: SOFT_INPUT_ADJUST_PAN
//}
//
//class InputModeLifecycleHelper(
//    private var window: Window?,
//    private val mode: Mode = Mode.ADJUST_RESIZE
//) : LifecycleObserver {
//
//    private var originalMode: Int = SOFT_INPUT_ADJUST_PAN
//
//    private val lifecycleEventObserver = LifecycleEventObserver { source, event ->
//        if (event == Lifecycle.Event.ON_START) {
//                window?.let {
//                    originalMode = it.getSoftInputMode()
//
//                    it.setSoftInputMode(
//                        when (mode) {
//                            Mode.ADJUST_RESIZE -> WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
//                            Mode.ADJUST_PAN -> WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
//                        }
//                    )
//                }
//        } else if (event == Lifecycle.Event.ON_STOP) {
//                if (originalMode != SOFT_INPUT_ADJUST_PAN) {
//                    window?.setSoftInputMode(originalMode)
//                }
//                window = null
//        }
//    }
//
//    enum class Mode {
//        ADJUST_RESIZE, ADJUST_PAN
//    }
//}