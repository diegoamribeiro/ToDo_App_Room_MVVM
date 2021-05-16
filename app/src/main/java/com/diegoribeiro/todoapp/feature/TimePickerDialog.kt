package com.diegoribeiro.todoapp.feature

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment: DialogFragment() {

    private lateinit var timeListener: TimePickerDialog.OnTimeSetListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return TimePickerDialog(requireActivity(), timeListener, hour, minute, DateFormat.is24HourFormat(activity))
    }

    fun setTimeListener(listener: TimePickerDialog.OnTimeSetListener){
        timeListener = listener
    }

    companion object{
        fun newInstance(listener: TimePickerDialog.OnTimeSetListener): TimePickerFragment{
            val instance = TimePickerFragment()
            instance.setTimeListener(listener)
            return instance
        }
    }

}