package com.diegoribeiro.todoapp.fragments.update

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.work.WorkManager
import com.diegoribeiro.todoapp.R
import com.diegoribeiro.todoapp.data.models.ToDoData
import com.diegoribeiro.todoapp.data.models.ToDoDateTime
import com.diegoribeiro.todoapp.data.viewmodel.SharedViewModel
import com.diegoribeiro.todoapp.data.viewmodel.ToDoViewModel
import com.diegoribeiro.todoapp.feature.DatePickerFragment
import com.diegoribeiro.todoapp.feature.TimePickerFragment
import com.diegoribeiro.todoapp.utils.ToDoWorkManager
import kotlinx.android.synthetic.main.fragment_add.view.*
import kotlinx.android.synthetic.main.fragment_update.*
import kotlinx.android.synthetic.main.fragment_update.view.*
import java.time.OffsetDateTime

@RequiresApi(Build.VERSION_CODES.O)
class UpdateFragment : Fragment() , DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private val args: UpdateFragmentArgs by navArgs<UpdateFragmentArgs>()
    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()
    private var deadLine: ToDoDateTime = ToDoDateTime()
    private var mToDoWorkManager = ToDoWorkManager(WorkManager.getInstance())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        deadLine = mSharedViewModel.parseOffsetDateTimeToToDataDateTime(args.currentItem.dateTime)

        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_update, container, false)
        view.current_title_et.setText(args.currentItem.title)
        view.current_description_et.setText(args.currentItem.description)
        view.current_priorities_spinner.setSelection(mSharedViewModel.parsePriorityToInt(args.currentItem.priority))
        view.current_priorities_spinner.onItemSelectedListener = mSharedViewModel.listener

        view.current_text_date.text = dateToString(args.currentItem.dateTime!!)
        view.current_text_time.text = timeToString(args.currentItem.dateTime!!)

        view.current_text_date.setOnClickListener { showDatePickerDialog() }
        view.current_text_time.setOnClickListener { showTimePickerDialog() }

        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                updateData()}
            R.id.menu_delete -> {
                confirmRemoval()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmRemoval(){
        val dialog = AlertDialog.Builder(requireContext())
        val message: String = resources.getString(R.string.are_you_sure)
        val removed: String = resources.getString(R.string.removed)
        dialog.setPositiveButton(R.string.yes){_,_ ->
            mToDoWorkManager.workManager.cancelAllWorkByTag(args.currentItem.id.toString() + args.currentItem.title)
                        mToDoViewModel.deleteItem(args.currentItem)
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
            Toast.makeText(requireContext(), "${args.currentItem.title} $removed!", Toast.LENGTH_SHORT).show()
        }
        dialog.setNegativeButton(R.string.no){_, _, ->}
        dialog.setTitle(R.string.confirm_removal)
        dialog.setMessage("$message '${args.currentItem.title}'?")
        dialog.create()
        dialog.show()
    }

    private fun updateData() {
        val mTitle = current_title_et.text.toString()
        val mDescription = current_description_et.text.toString()
        val mPriority = current_priorities_spinner.selectedItemPosition
        val mDate = current_text_date.text.toString()
        val mTime = current_text_time.text.toString()

        val validation = mSharedViewModel.verifyDataFromUser(mTitle, mDescription, mDate, mTime)
        if (validation){
            val updatedItem = ToDoData(
                args.currentItem.id, mTitle,
                mSharedViewModel.parseIntToPriority(mPriority),
                mDescription, mSharedViewModel.setDeadLine(deadLine)
            )
            mToDoViewModel.updateData(updatedItem)
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)

            mToDoWorkManager.workManager.cancelAllWorkByTag(args.currentItem.id.toString() + args.currentItem.title)
            setupObserver(requireView(), updatedItem)

            Toast.makeText(requireContext(), " ${args.currentItem.title}", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(requireContext(), R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupObserver(view: View, item: ToDoData){
        mToDoViewModel.taskId.observe(requireActivity(), {
            if(deadLine.isDateReady() && deadLine.isTimeReady() ){
                if(view.sw_twoHours.isChecked) {
                    mToDoWorkManager.createWorkManager(item.copy(id = it), view, 0, 2)
                }
                if(view.sw_oneDay.isChecked) {
                    mToDoWorkManager.createWorkManager(item.copy(id = it), view, 1, 0)
                }
                if(view.current_sw_twoDays.isChecked) {
                    mToDoWorkManager.createWorkManager(item.copy(id = it), view, 2, 0)
                }
            }else{
                Toast.makeText(activity?.applicationContext, "Date not set", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDatePickerDialog() {
        val newFragment: DialogFragment = DatePickerFragment.newInstance(this)
        newFragment.show(requireActivity().supportFragmentManager, "datePicker")
    }

    private fun showTimePickerDialog(){
        val newFragment: DialogFragment = TimePickerFragment.newInstance(this)
        newFragment.show(requireActivity().supportFragmentManager, "timePicker")
    }

    @SuppressLint("SetTextI18n")
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        deadLine = deadLine.copy(day = dayOfMonth, month = month + 1, year = year)
        current_text_date.text = deadLine.getDate()
    }

    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        deadLine = deadLine.copy(hour = hourOfDay, minute = minute)
        current_text_time.text = deadLine.getTime()
    }

    private fun setupObserver(view: View){
        mToDoViewModel.taskId.observe(requireActivity(), {
            if(deadLine.isDateReady() && deadLine.isTimeReady() ){
                mToDoWorkManager.createWorkManager(args.currentItem.copy(id = it), view,0,2)
            }else{
                Toast.makeText(activity?.applicationContext, "Date not set", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun dateToString (dateTime: OffsetDateTime): String{
        val toDoDateTime = ToDoDateTime(
            dateTime.dayOfMonth,
            dateTime.monthValue,
            dateTime.year,
            dateTime.hour,
            dateTime.minute
        )
        return toDoDateTime.getDate()
    }

    private fun timeToString (dateTime: OffsetDateTime): String{
        val toDoDateTime = ToDoDateTime(
            dateTime.dayOfMonth,
            dateTime.monthValue,
            dateTime.year,
            dateTime.hour,
            dateTime.minute
        )
        return toDoDateTime.getTime()
    }

}