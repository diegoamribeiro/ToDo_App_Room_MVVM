package com.diegoribeiro.todoapp.fragments.list.adapter

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.diegoribeiro.todoapp.R
import com.diegoribeiro.todoapp.data.models.Priority
import com.diegoribeiro.todoapp.data.models.ToDoData
import com.diegoribeiro.todoapp.fragments.list.ListFragmentDirections
import kotlinx.android.synthetic.main.row_layout.view.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemView.title_txt.text = dataList[position].title
        holder.itemView.description_txt.text = dataList[position].description
        if(dataList[position].deadline == ""){
            holder.itemView.deadline_txt.visibility = View.GONE
        }else{
            holder.itemView.deadline_txt.text = dataList[position].deadline
        }

        when(dataList[position].priority){
            Priority.HIGH -> holder.itemView.priority_indicator.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            Priority.MEDIUM -> holder.itemView.priority_indicator.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.yellow))
            Priority.LOW -> holder.itemView.priority_indicator.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
        }
        holder.itemView.row_background.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToUpdateFragment(dataList[position])
            holder.itemView.findNavController().navigate(action)
        }

        drawCalendarIcon(holder, dataList[position].deadline)
    }

    fun setData(toDoList: List<ToDoData>){
        val toDoDiffUtil = ToDoDiffUtil(dataList, toDoList)
        val toDoDiffResult = DiffUtil.calculateDiff(toDoDiffUtil)
        this.dataList = toDoList
        toDoDiffResult.dispatchUpdatesTo(this)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun parseStringToLocalDate(string: String): LocalDate {
        val year = string.substring(range = 6..9).toInt()
        val month = string.substring(range = 3..4).toInt()
        val day = string.substring(range = 0..1).toInt()
        val hour = string.substring(range = 11..12).toInt()
        val minute = string.substring(range = 14..15).toInt()

        return LocalDateTime.of(year, month, day, hour, minute).toLocalDate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun drawCalendarIcon(holder: MyViewHolder, string: String){
        if (string != ""){
            val dateDeadline = parseStringToLocalDate(string)
            val period = Period.between(LocalDate.now(),dateDeadline)
            if (period.months < 0){
                holder.itemView.ic_calendar.setImageResource(R.drawable.ic_calendar_red)
            } else if (period.months <= 0){
                when(period.days){
                    in 0..3 ->
                        holder.itemView.ic_calendar.setImageResource(R.drawable.ic_calendar_yellow)
                    in 4..Int.MAX_VALUE ->
                        return
                    else ->
                        holder.itemView.ic_calendar.setImageResource(R.drawable.ic_calendar_red)
                }
            }
        }else{
            holder.itemView.ic_calendar.visibility = View.GONE
        }
    }
}