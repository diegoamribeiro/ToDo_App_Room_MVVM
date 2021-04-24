package com.diegoribeiro.todoapp.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.diegoribeiro.todoapp.R
import com.diegoribeiro.todoapp.data.models.ToDoData
import com.diegoribeiro.todoapp.fragments.list.adapter.ListAdapter
import com.diegoribeiro.todoapp.data.viewmodel.SharedViewModel
import com.diegoribeiro.todoapp.data.viewmodel.ToDoViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_list.view.*

class ListFragment : Fragment() {

    private val mAdapter: ListAdapter by lazy { ListAdapter() }
    private val toDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_list, container, false)

        view.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }

        val recyclerView = view.recyclerListView
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        swipeToDelete(recyclerView)

        toDoViewModel.getAllData.observe(viewLifecycleOwner,  { data->
            mSharedViewModel.verifyEmptyList(data)
            mAdapter.setData(data)
        })

        mSharedViewModel.emptyDatabase.observe(viewLifecycleOwner,  {
            showEmptyDatabaseView(it)
        })

        //Set menu
        setHasOptionsMenu(true)
        return view
    }

    private fun swipeToDelete(recyclerView: RecyclerView){
        val swipeToDeleteCallback = object : SwipeToDelete(){

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val itemToDelete = mAdapter.dataList[viewHolder.adapterPosition]
                toDoViewModel.deleteItem(itemToDelete)
                mAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                Toast.makeText(requireContext(), "Successfully removed '${itemToDelete.title}'", Toast.LENGTH_SHORT).show()
                restoreDeletedItem(viewHolder.itemView, itemToDelete, viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedItem(view: View, deletedItem: ToDoData, position: Int){
        val snackbar = Snackbar.make(
                view, "Deleted '${deletedItem.title}'", Snackbar.LENGTH_SHORT
        )
        snackbar.setAction("Undo"){
            toDoViewModel.insert(deletedItem)
            mAdapter.notifyItemChanged(position)
        }
        snackbar.show()

    }


    private fun showEmptyDatabaseView(emptyDatabase: Boolean) {
        if (emptyDatabase){
            view?.no_data_imageView?.visibility = View.VISIBLE
            view?.no_data_textView?.visibility = View.VISIBLE
        }else{
            view?.no_data_imageView?.visibility = View.INVISIBLE
            view?.no_data_textView?.visibility = View.INVISIBLE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_delete_all -> confirmRemoval()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun confirmRemoval(){
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setPositiveButton("Yes"){_,_ ->
            toDoViewModel.deleteAll()
            Toast.makeText(requireContext(), "All items Removed!", Toast.LENGTH_SHORT).show()
        }
        dialog.setNegativeButton("No"){_, _, ->}
        dialog.setTitle("Confirm removal")
        dialog.setMessage("Are you sure delete All?")
        dialog.create()
        dialog.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.list_fragment_menu, menu)
    }

}