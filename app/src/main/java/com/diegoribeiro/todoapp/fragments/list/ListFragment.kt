package com.diegoribeiro.todoapp.fragments.list

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.work.WorkManager
import com.diegoribeiro.todoapp.R
import com.diegoribeiro.todoapp.data.models.ToDoData
import com.diegoribeiro.todoapp.fragments.list.adapter.ListAdapter
import com.diegoribeiro.todoapp.data.viewmodel.SharedViewModel
import com.diegoribeiro.todoapp.data.viewmodel.ToDoViewModel
import com.diegoribeiro.todoapp.utils.ToDoWorkManager
import com.diegoribeiro.todoapp.utils.hideKeyboard
import com.diegoribeiro.todoapp.utils.observeOnce
import com.google.android.material.snackbar.Snackbar
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.*
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_list.view.*

@RequiresApi(Build.VERSION_CODES.O)
class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private lateinit var recyclerView: RecyclerView
    private val listAdapter: ListAdapter by lazy { ListAdapter() }
    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    private val mToDoWorkManager = ToDoWorkManager(WorkManager.getInstance())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_list, container, false)

        view.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_listFragment_to_addFragment)
        }

        setupRecyclerView(view)

        mSharedViewModel.emptyDatabase.observe(viewLifecycleOwner,  {
            showEmptyDatabaseView(it)
        })

        //Hide Keyboard
        hideKeyboard(requireActivity())

        //Set menu
        setHasOptionsMenu(true)
        return view
    }


    private fun setupRecyclerView(view: View){
        recyclerView = view.recyclerListView
        recyclerView.adapter = listAdapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        recyclerView.itemAnimator = SlideInUpAnimator().apply {
            addDuration = 300
        }
        swipeToDelete(recyclerView)

        mToDoViewModel.getAllData.observe(viewLifecycleOwner,  { data->
            mSharedViewModel.verifyEmptyList(data)
            listAdapter.setData(data)
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_delete_all -> confirmRemoval()
            R.id.menu_priority_high -> mToDoViewModel.sortByHighPriority.observe(this, {listAdapter.setData(it)})
            R.id.menu_priority_low -> mToDoViewModel.sortByLowPriority.observe(this, {listAdapter.setData(it)})

        }
        return super.onOptionsItemSelected(item)
    }

    private fun swipeToDelete(recyclerView: RecyclerView){
        val swipeToDeleteCallback = object : SwipeToDelete(){

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val itemToDelete = listAdapter.dataList[viewHolder.adapterPosition]
                mToDoViewModel.deleteItem(itemToDelete)
                mToDoWorkManager.workManager.cancelAllWorkByTag(itemToDelete.id.toString() + itemToDelete.title)

                listAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                restoreDeletedItem(viewHolder.itemView, itemToDelete)
            }
        }
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeletedItem(view: View, deletedItem: ToDoData){
        val snackBar = Snackbar.make(
                view, "Deleted '${deletedItem.title}'", Snackbar.LENGTH_SHORT
        )
        snackBar.setAction(R.string.undo){
            mToDoViewModel.insert(deletedItem)

            mToDoWorkManager.createWorkManager(deletedItem.copy(id = deletedItem.id), view)
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

        mToDoViewModel.searchDatabase(searchQuery).observeOnce(this, { list ->
            list?.let {
                //Log.d("**ListFragment", "Search through database")
                listAdapter.setData(it)
            }
        })
    }

    private fun confirmRemoval(){
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setPositiveButton(R.string.yes){_,_ ->
            mToDoViewModel.deleteAll()
            Toast.makeText(requireContext(), R.string.all_items_removed, Toast.LENGTH_SHORT).show()
        }
        dialog.setNegativeButton(R.string.no){_, _, ->}
        dialog.setTitle(R.string.confirm_removal)
        dialog.setMessage(R.string.are_you_sure_all)
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
