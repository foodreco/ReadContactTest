package com.leesangmin89.readcontacttest

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PorterDuff
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.WindowManager.LayoutParams.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.leesangmin89.readcontacttest.data.entity.CallLogData
import java.text.SimpleDateFormat


@SuppressLint("SimpleDateFormat")
fun convertLongToDateString(systemTime: Long): String {
    val simpleDateFormat = SimpleDateFormat("yyyy년 MM월 dd일")
    return simpleDateFormat.format(systemTime)
}

fun convertLongToTimeString(systemTime: Long): String {
    val minutes = systemTime / 60
    val seconds = systemTime % 60
    // 통화시간 1분 미만은 초 단위로만 출력
    return when (systemTime) {
        in 0..59 -> "${seconds}초"
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