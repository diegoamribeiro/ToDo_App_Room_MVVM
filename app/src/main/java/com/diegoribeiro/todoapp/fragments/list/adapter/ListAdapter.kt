package com.diegoribeiro.todoapp.fragments.list.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.diegoribeiro.todoapp.R
import com.diegoribeiro.todoapp.data.models.Priority
import com.diegoribeiro.todoapp.data.models.ToDoData
import com.diegoribeiro.todoapp.data.models.ToDoDateTime
import com.diegoribeiro.todoapp.databinding.RowLayoutBinding
import com.diegoribeiro.todoapp.fragments.list.ListFragmentDirections
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.Period

@RequiresApi(Build.VERSION_CODES.O)
class ListAdapter : RecyclerView.Adapter<ListAdapter.ItemTaskViewHolder>(){

    var dataList = emptyList<ToDoData>()
   // private var currentItem = UpdateFragmentDirections

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemTaskViewHolder {
        val binding = RowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemTaskViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ItemTaskViewHolder, position: Int) {
        holder.binding.titleTxt.text = dataList[position].title
        holder.binding.deadlineTxt.text = dateTimeToString(dataList[position].dateTime!!)

        when(dataList[position].priority){
            Priority.HIGH -> holder.binding.priorityIndicator.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            Priority.MEDIUM -> holder.binding.priorityIndicator.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.yellow))
            Priority.LOW -> holder.binding.priorityIndicator.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
        }
        holder.binding.rowBackground.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToUpdateFragment(dataList[position])
            holder.itemView.findNavController().navigate(action)
        }

        drawCalendarIcon(holder, dataList[position].dateTime!!)
    }

    fun setData(toDoList: List<ToDoData>){
        val toDoDiffUtil = ToDoDiffUtil(dataList, toDoList)
        val toDoDiffResult = DiffUtil.calculateDiff(toDoDiffUtil)
        this.dataList = toDoList
        toDoDiffResult.dispatchUpdatesTo(this)
    }

    private fun dateTimeToString (dateTime: OffsetDateTime): String{
        val toDoDateTime = ToDoDateTime(
            dateTime.dayOfMonth,
            dateTime.monthValue,
            dateTime.year,
            dateTime.hour,
            dateTime.minute
        )
        return toDoDateTime.getDateTime()
    }

    private fun drawCalendarIcon(holder: ListAdapter.ItemTaskViewHolder, date: OffsetDateTime){
        if (date != null){
            val dateDeadline = date.toLocalDate()
            val period = Period.between(LocalDate.now(),dateDeadline)

            when {
                period.months < 0 -> holder.binding.icCalendar.setImageResource(R.drawable.ic_calendar_red)
                period.months == 0 -> {
                    when (period.days) {
                        0 -> {
                            if (LocalDateTime.now().hour > date.hour) {
                                holder.binding.icCalendar.setImageResource(R.drawable.ic_calendar_red)
                            } else {
                                if (LocalDateTime.now().minute > date.minute) {
                                    holder.binding.icCalendar.setImageResource(R.drawable.ic_calendar_red)
                                } else holder.binding.icCalendar.setImageResource(R.drawable.ic_calendar_yellow)
                            }
                        }
                        in 1..3 ->
                            holder.binding.icCalendar.setImageResource(R.drawable.ic_calendar_yellow)
                        in -31..-1 ->
                            holder.binding.icCalendar.setImageResource(R.drawable.ic_calendar_red)
                        else ->
                            return
                    }
                }
            }
        }else{
            holder.binding.icCalendar.visibility = View.GONE
        }
    }

    class ItemTaskViewHolder(val binding: RowLayoutBinding) : RecyclerView.ViewHolder(binding.root){

    }
}