package com.diegoribeiro.todoapp.fragments.add

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.service.autofill.FillEventHistory
import android.view.*
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.diegoribeiro.todoapp.MainActivity
import com.diegoribeiro.todoapp.R
import com.diegoribeiro.todoapp.data.models.ToDoData
import com.diegoribeiro.todoapp.data.models.ToDoDateTime
import com.diegoribeiro.todoapp.data.viewmodel.SharedViewModel
import com.diegoribeiro.todoapp.data.viewmodel.ToDoViewModel
import com.diegoribeiro.todoapp.feature.DatePickerFragment
import com.diegoribeiro.todoapp.feature.TimePickerFragment
import com.diegoribeiro.todoapp.fragments.list.ListFragment
import com.diegoribeiro.todoapp.fragments.list.ListFragmentDirections
import com.diegoribeiro.todoapp.utils.NotificationJobService
import com.diegoribeiro.todoapp.utils.NotificationWorkManager
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*
import kotlinx.android.synthetic.main.row_layout.*
import kotlinx.coroutines.currentCoroutineContext
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

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
        setupObserver()

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

    private fun setupObserver(){
        mToDoViewModel.taskId.observe(requireActivity(), {
            if(deadLine.isDateReady() && deadLine.isTimeReady() ){
                createWorkManager(newData.copy(id = it))
            }else{
                Toast.makeText(requireContext(), "Date not set", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createWorkManager(toDoData: ToDoData){
        val timeTilFuture = ChronoUnit.MILLIS.between(OffsetDateTime.now(), toDoData.dateTime)
        val data = Data.Builder()
        data.putString(EXTRA_TASK_NAME, toDoData.title)
        data.putInt(EXTRA_TASK_ID, toDoData.id)

        val workRequest = OneTimeWorkRequest.Builder(NotificationWorkManager::class.java)
            .setInitialDelay(timeTilFuture, TimeUnit.MILLISECONDS)
            .setInputData(data.build())
            .addTag(newData.id.toString())
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

        const val EXTRA_TASK_NAME = "EXTRA_TASK_NAME"
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val SCHEDULE_EXTRA_TASK_NAME = "SCHEDULE_EXTRA_TASK_NAME"

        /**
         * Start [TaskAddActivity]
         * @param context previous activity
         */
        fun start(context: Context): Intent {
            return Intent(context, AddFragment::class.java)
        }
    }

}
