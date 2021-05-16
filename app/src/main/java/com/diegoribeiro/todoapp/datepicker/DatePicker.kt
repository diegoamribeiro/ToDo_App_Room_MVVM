package com.diegoribeiro.todoapp.datepicker

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import java.util.*

class DatePicker(
    editText: EditText?,
    context: Context
):  DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    var requireContext = context
    var deadlineEditText = editText

    var day: Int = 0
    var month: Int = 0
    var year: Int = 0
    var hour: Int = 0
    var minute: Int = 0

    var savedDay: Int = 0
    var savedMonth: Int = 0
    var savedYear: Int = 0
    var savedHour: Int = 0
    var savedMinute: Int = 0


    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month + 1
        savedYear = year

        getDateTimeCalendar()
        TimePickerDialog(requireContext, this, hour,minute,true).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        savedHour = hourOfDay
        savedMinute = minute

        deadlineEditText?.setText(
            "${String.format("%02d", savedDay)}-" +
                    "${String.format("%02d", savedMonth)}-" +
                    "${String.format("%04d", savedYear)} " +
                    "${String.format("%02d", savedHour)}:" +
                    "${String.format("%02d", savedMinute)}"
        )
    }

    fun pickDate() {
        deadlineEditText?.setOnClickListener{
            Log.w("test click", "Deadline Clicked")
            getDateTimeCalendar()
            DatePickerDialog(requireContext, this, year, month, day).show()
        }
    }

    private fun getDateTimeCalendar(){
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR)
        minute = cal.get(Calendar.MINUTE)
    }
}