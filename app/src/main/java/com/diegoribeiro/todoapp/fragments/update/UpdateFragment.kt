package com.diegoribeiro.todoapp.fragments.update

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.diegoribeiro.todoapp.R
import com.diegoribeiro.todoapp.data.models.ToDoData
import com.diegoribeiro.todoapp.data.viewmodel.SharedViewModel
import com.diegoribeiro.todoapp.data.viewmodel.ToDoViewModel
import com.diegoribeiro.todoapp.datepicker.DatePicker
import kotlinx.android.synthetic.main.fragment_update.*
import kotlinx.android.synthetic.main.fragment_update.view.*

class UpdateFragment : Fragment() {

    var deadline: EditText? = null

    private val args: UpdateFragmentArgs by navArgs<UpdateFragmentArgs>()
    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_update, container, false)
        view.current_title_et.setText(args.currentItem.title)
        view.current_description_et.setText(args.currentItem.description)
        view.current_deadline_et.setText(args.currentItem.deadline)
        view.current_priorities_spinner.setSelection(mSharedViewModel.parsePriorityToInt(args.currentItem.priority))
        view.current_priorities_spinner.onItemSelectedListener = mSharedViewModel.listener

        deadline = view.findViewById(R.id.current_deadline_et)
        DatePicker(deadline, requireContext()).pickDate()
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_save -> {
                updateData()}
            R.id.menu_delete -> {
                confirmRemoval()
            }
        }
        return super.onOptionsItemSelected(item)
    }


    private fun confirmRemoval(){
        val dialog = AlertDialog.Builder(requireContext())
        val message: String = resources.getString(R.string.are_you_sure)
        val removed: String = resources.getString(R.string.removed)
        dialog.setPositiveButton(R.string.yes){_,_ ->
            mToDoViewModel.deleteItem(args.currentItem)
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
            Toast.makeText(requireContext(), "${args.currentItem.title} $removed!", Toast.LENGTH_SHORT).show()
        }
        dialog.setNegativeButton(R.string.no){_, _, ->}
        dialog.setTitle(R.string.confirm_removal)
        dialog.setMessage("$message '${args.currentItem.title}'?")
        dialog.create()
        dialog.show()
    }

    private fun updateData() {
        val mTitle = current_title_et.text.toString()
        val mDescription = current_description_et.text.toString()
        val mPriority = current_priorities_spinner.selectedItemPosition
        val mDeadline = current_deadline_et?.text.toString()

        val validation = mSharedViewModel.verifyDataFromUser(mTitle, mDescription)
        if (validation){
            mToDoViewModel.updateData(
                    ToDoData(
                            args.currentItem.id, mTitle,
                            mSharedViewModel.parseIntToPriority(mPriority),
                            mDescription, mDeadline))
            findNavController().navigate(R.id.action_updateFragment_to_listFragment)
            Toast.makeText(requireContext(), " ${args.currentItem.title}", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(requireContext(), R.string.please_fill_all_fields, Toast.LENGTH_SHORT).show()
        }
    }
}