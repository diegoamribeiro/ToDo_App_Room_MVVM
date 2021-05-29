package com.diegoribeiro.todoapp.fragments.add

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.diegoribeiro.todoapp.R
import com.diegoribeiro.todoapp.data.ToDoConstants
import com.diegoribeiro.todoapp.data.models.ToDoData
import com.diegoribeiro.todoapp.data.models.ToDoDateTime
import com.diegoribeiro.todoapp.data.viewmodel.SharedViewModel
import com.diegoribeiro.todoapp.data.viewmodel.ToDoViewModel
import com.diegoribeiro.todoapp.feature.DatePickerFragment
import com.diegoribeiro.todoapp.feature.TimePickerFragment
import com.diegoribeiro.todoapp.utils.NotificationWorkManager
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
import kotlinx.android.synthetic.main.fragment_update.view.*
import kotlinx.android.synthetic.main.row_layout.*
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
class AddFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener{

    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()
    private var deadLine: ToDoDateTime = ToDoDateTime()
    private lateinit var newData: ToDoData
    private val workManager = WorkManager.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        val view =  inflater.inflate(R.layout.fragment_add, container, false)

        setHasOptionsMenu(true)
        view.priorities_spinner.onItemSelectedListener = mSharedViewModel.listener

        view.text_new_date.setOnClickListener {
            showDatePickerDialog()
        }
        setupObserver(view)

        view.text_new_time.setOnClickListener {
            showTimePickerDialog()
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_add){
            insertDataToDatabase()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun insertDataToDatabase() {
        val mTitle = title_et.text.toString()
        val mPriority = priorities_spinner.selectedItemPosition
        val mDescription = description_et.text.toString()
        val date = text_new_date.text.toString()
        val time = text_new_time.text.toString()

        val validation = mSharedViewModel.verifyDataFromUser(mTitle, mDescription, date, time)

        if (validation){
            newData = ToDoData(0, mTitle, mSharedViewModel
                    .parseIntToPriority(mPriority), mDescription, mSharedViewModel.setDeadLine(deadLine))
            mToDoViewModel.insert(newData)

            findNavController().navigate(R.id.action_addFragment_to_listFragment)
            Toast.makeText(requireContext(), R.string.saved_successfully, Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(requireContext(), R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show()
        }
    }

    private fun verifyIfSwitcherIsChecked(view: View, switcher: SwitchCompat): Boolean{
        return switcher.isChecked

    }

    private fun setupObserver(view: View){
        mToDoViewModel.taskId.observe(requireActivity(), {
            if(deadLine.isDateReady() && deadLine.isTimeReady() ){
                if(view.sw_inDay.isChecked) {
                    createWorkManager(newData.copy(id = it), view, 0)
                }
                if(view.sw_oneDay.isChecked) {
                    createWorkManager(newData.copy(id = it), view, 1)
                }
                if(view.sw_twoDays.isChecked) {
                    createWorkManager(newData.copy(id = it), view, 2)
                }
            }else{
                Toast.makeText(requireContext(), "Date not set", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createWorkManager(toDoData: ToDoData, view: View, daysToReminder: Long){
        val timeTilFuture = ChronoUnit.MILLIS.between(OffsetDateTime.now(), toDoData.dateTime?.minusDays(daysToReminder))
        val data = Data.Builder()
        val stringPriority = view.priorities_spinner.selectedItem.toString()
        val stringDeadLine =
            " ${view.context.getString(R.string.deadline)}: " +
                    "${view.text_new_date.text} " +
                    "${view.text_new_time.text}"

        data.putString(ToDoConstants.EXTRA_TASK_NAME, toDoData.title)
        data.putString(ToDoConstants.EXTRA_TASK_PRIORITY, stringPriority)
        data.putString(ToDoConstants.EXTRA_TASK_DEADLINE, stringDeadLine)
        data.putInt(ToDoConstants.EXTRA_TASK_ID, toDoData.id)

        val workRequest = OneTimeWorkRequest.Builder(NotificationWorkManager::class.java)
            .setInitialDelay(timeTilFuture, TimeUnit.MILLISECONDS)
            .setInputData(data.build())
            .addTag(toDoData.title)
            .build()

        workManager.enqueue(workRequest)
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
        text_new_date.text = deadLine.getDate()
    }

    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        deadLine = deadLine.copy(hour = hourOfDay, minute = minute)
        text_new_time.text = deadLine.getTime()
    }

    companion object {

        /**
         * Start [TaskAddActivity]
         * @param context previous activity
         */
        fun start(context: Context): Intent {
            return Intent(context, AddFragment::class.java)
        }
    }
}
