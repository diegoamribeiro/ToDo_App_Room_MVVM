package com.diegoribeiro.todoapp.data.models

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.diegoribeiro.todoapp.R
import com.diegoribeiro.todoapp.fragments.list.ListFragmentDirections
import com.diegoribeiro.todoapp.fragments.update.UpdateFragmentDirections
import kotlinx.android.synthetic.main.row_layout.view.*

class ListAdapter : RecyclerView.Adapter<ListAdapter.MyViewHolder>(){

    private var dataList = emptyList<ToDoData>()
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

        when(dataList[position].priority){
            Priority.HIGH -> holder.itemView.priority_indicator.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
            Priority.MEDIUM -> holder.itemView.priority_indicator.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.yellow))
            Priority.LOW -> holder.itemView.priority_indicator.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.green))
        }
        holder.itemView.row_background.setOnClickListener {
            val action = ListFragmentDirections.actionListFragmentToUpdateFragment(dataList[position])
            holder.itemView.findNavController().navigate(action)
        }
    }

    fun setData(toDoList: List<ToDoData>){
        this.dataList = toDoList
        notifyDataSetChanged()
    }


}