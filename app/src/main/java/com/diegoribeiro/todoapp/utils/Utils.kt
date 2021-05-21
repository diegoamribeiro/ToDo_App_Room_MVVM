package com.diegoribeiro.todoapp.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.diegoribeiro.todoapp.data.models.ToDoDateTime
import java.time.OffsetDateTime
import kotlin.random.Random

fun hideKeyboard(activity: Activity){
    val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val currentFocusView = activity.currentFocus
    currentFocusView.let {
        inputMethodManager.hideSoftInputFromWindow(
                currentFocusView?.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}

fun <T>LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: Observer<T>){
    observe(lifecycleOwner, object : Observer<T>{
        override fun onChanged(t: T) {
            observer.onChanged(t)
            removeObserver(this)
        }
    })
}

@RequiresApi(Build.VERSION_CODES.O)
fun OffsetDateTime.convertToDateTime(): ToDoDateTime {
    return ToDoDateTime(
            day = dayOfMonth,
            month = monthValue,
            year = year,
            hour = hour,
            minute = minute
    )
}

fun String.toRandom(): String{
    val random = Random(32)
    return this + random
}