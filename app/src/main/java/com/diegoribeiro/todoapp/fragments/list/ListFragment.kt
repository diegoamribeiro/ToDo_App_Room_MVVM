package com.diegoribeiro.todoapp.fragments.list

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.SearchView
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
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.fragment_list.view.*

class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val mAdapter: ListAdapter by lazy { ListAdapter() }
    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_list, container, false)

        view.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }

        val recyclerView = view.recyclerListView
        recyclerView.adapter = mAdapter
        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.itemAnimator = SlideInUpAnimator().apply {
            addDuration = 300
        }
        swipeToDelete(recyclerView)

        mToDoViewModel.getAllData.observe(viewLifecycleOwner,  { data->
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
                mToDoViewModel.deleteItem(itemToDelete)
                mAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                Toast.makeText(requireContext(), "Successfully removed '${itemToDelete.title}'", Toast.LENGTH_SHORT).show()
                restoreDeletedItem(viewHolder.itemView, itemToDelete, viewHolder.adapterPosition)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedItem(view: View, deletedItem: ToDoData, position: Int){
        val snackBar = Snackbar.make(
                view, "Deleted '${deletedItem.title}'", Snackbar.LENGTH_SHORT
        )
        snackBar.setAction("Undo"){
            mToDoViewModel.insert(deletedItem)
            mAdapter.notifyItemChanged(position)
        }
        snackBar.show()
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
            R.id.menu_priority_high -> mToDoViewModel.sortByHighPriority.observe(this, {mAdapter.setData(it)})
            R.id.menu_priority_low -> mToDoViewModel.sortByLowPriority.observe(this, {mAdapter.setData(it)})
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null){
            searchThroughDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (newText != null){
            searchThroughDatabase(newText)
        }
        return true
    }

    private fun searchThroughDatabase(query: String){
        val searchQuery = "%$query%"

        mToDoViewModel.searchDatabase(searchQuery).observe(this, { list ->
            list?.let {
                mAdapter.setData(it)
            }
        })

    }

    private fun confirmRemoval(){
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setPositiveButton("Yes"){_,_ ->
            mToDoViewModel.deleteAll()
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
        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }
}