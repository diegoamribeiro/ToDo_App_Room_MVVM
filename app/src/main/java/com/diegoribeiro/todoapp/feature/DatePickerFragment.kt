package com.diegoribeiro.todoapp.feature

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.*

class DatePickerFragment : DialogFragment(){
    private lateinit var dateListener: DatePickerDialog.OnDateSetListener

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
        val month: Int = calendar.get(Calendar.MONTH)
        val year: Int = calendar.get(Calendar.YEAR)
        return DatePickerDialog(requireActivity(), dateListener, year, month, day)
    }

    @JvmName("setDateListener1")
    fun setDateListener(listener: DatePickerDialog.OnDateSetListener){
        dateListener = listener
    }

    companion object{
        fun newInstance(listener: DatePickerDialog.OnDateSetListener): DatePickerFragment{
            val instance = DatePickerFragment()
            instance.setDateListener(listener)
            return instance
        }
    }
}