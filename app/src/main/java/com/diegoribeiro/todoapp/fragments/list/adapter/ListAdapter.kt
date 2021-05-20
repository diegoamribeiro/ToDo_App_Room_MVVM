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
import com.diegoribeiro.todoapp.fragments.list.ListFragmentDirections
import kotlinx.android.synthetic.main.row_layout.view.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.Period

@RequiresApi(Build.VERSION_CODES.O)
class ListAdapter : RecyclerView.Adapter<ListAdapter.MyViewHolder>(){

    var dataList = emptyList<ToDoData>()
   // private var currentItem = UpdateFragmentDirections
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.row_layout, parent,false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemView.title_txt.text = dataList[position].title
        holder.itemView.description_txt.text = dataList[position].description
        holder.itemView.deadline_txt.text = dateTimeToString(dataList[position].toDoDateTime!!)

        when(dataList[position].priority){
            Priority.HIGH -> holder.itemView.priority_indicator.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            Priority.MEDIUM -> holder.itemView.priority_indicator.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.yellow))
            Priority.LOW -> holder.itemView.priority_indicator.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
        }
        holder.itemView.row_background.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToUpdateFragment(dataList[position])
            holder.itemView.findNavController().navigate(action)
        }

        drawCalendarIcon(holder, dataList[position].toDoDateTime!!)
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

    private fun drawCalendarIcon(holder: ListAdapter.MyViewHolder, date: OffsetDateTime){
        if (date != null){
            val dateDeadline = date.toLocalDate()
            val period = Period.between(LocalDate.now(),dateDeadline)
            if (period.months < 0){
                holder.itemView.ic_calendar.setImageResource(R.drawable.ic_calendar_red)
            } else if (period.months == 0){
                when(period.days){
                    in 0..3 ->
                        holder.itemView.ic_calendar.setImageResource(R.drawable.ic_calendar_yellow)
                    in -31..-1 ->
                        holder.itemView.ic_calendar.setImageResource(R.drawable.ic_calendar_red)
                    else ->
                        return
                }
            }
        }else{
            holder.itemView.ic_calendar.visibility = View.GONE
        }
    }
}