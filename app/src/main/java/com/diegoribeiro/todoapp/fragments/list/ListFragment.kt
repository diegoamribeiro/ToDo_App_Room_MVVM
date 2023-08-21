package com.diegoribeiro.todoapp.fragments.list

import android.app.Activity
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
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
import com.diegoribeiro.todoapp.databinding.FragmentListBinding
import com.diegoribeiro.todoapp.utils.ToDoWorkManager
import com.diegoribeiro.todoapp.utils.hideKeyboard
import com.diegoribeiro.todoapp.utils.observeOnce
import com.diegoribeiro.todoapp.utils.viewBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import dagger.hilt.android.AndroidEntryPoint
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator


@AndroidEntryPoint
@RequiresApi(Build.VERSION_CODES.O)
class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val binding: FragmentListBinding by viewBinding()

    private lateinit var recyclerView: RecyclerView
    private val listAdapter: ListAdapter by lazy { ListAdapter() }
    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    private val mToDoWorkManager = ToDoWorkManager(WorkManager.getInstance())

    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var register: ActivityResultLauncher<IntentSenderRequest>
    //private val UPDATE_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        register = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            val resultCode = result.resultCode
            val intent = result.data
            if (resultCode != Activity.RESULT_OK){
                println("Something went wrong!")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        appUpdateManager = AppUpdateManagerFactory.create(requireContext())


        binding.floatingActionButton.setOnClickListener {
            NavHostFragment.findNavController(this).navigate(R.id.action_listFragment_to_addFragment)
        }

        setupRecyclerView()

        mSharedViewModel.emptyDatabase.observe(viewLifecycleOwner) {
            showEmptyDatabaseView(it)
        }

        //Hide Keyboard
        hideKeyboard(requireActivity())

        //Set menu
        setHasOptionsMenu(true)
        checkForAppUpdates()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        updateInProgress()
    }

    private fun checkForAppUpdates() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            println("Check for updates")
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE){// && info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    register,
                    AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
                )
            }
        }
    }

    private fun updateInProgress() {
        // Quando o usuário permite a atualização e fecha o app, a atualização continua background
        // Caso o usuário abra o app novamente, essa função irá checar o status para retomar de onde parou.
        appUpdateManager.appUpdateInfo.addOnSuccessListener { updateInfo ->
            if (updateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS){
                appUpdateManager.startUpdateFlowForResult(
                    updateInfo,
                    register,
                    AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE)
                )
            }
        }
    }


    private fun setupRecyclerView(){
        recyclerView = binding.recyclerListView
        recyclerView.adapter = listAdapter
        recyclerView.layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        recyclerView.itemAnimator = SlideInUpAnimator().apply {
            addDuration = 300
        }
        swipeToDelete(recyclerView)

        mToDoViewModel.getAllData.observe(viewLifecycleOwner) { data ->
            mSharedViewModel.verifyEmptyList(data)
            listAdapter.setData(data)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_delete_all -> confirmRemoval()
            R.id.menu_priority_high -> mToDoViewModel.sortByHighPriority.observe(this, {listAdapter.setData(it)})
            R.id.menu_priority_low -> mToDoViewModel.sortByLowPriority.observe(this, {listAdapter.setData(it)})
            R.id.menu_datetime -> mToDoViewModel.sortByDateTime.observe(this, {listAdapter.setData(it)})

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
            binding.noDataImageView.visibility = View.VISIBLE
            binding.noDataTextView.visibility = View.VISIBLE
        }else{
            binding.noDataImageView.visibility = View.INVISIBLE
            binding.noDataTextView.visibility = View.INVISIBLE
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
            mToDoWorkManager.workManager.cancelAllWork()
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
