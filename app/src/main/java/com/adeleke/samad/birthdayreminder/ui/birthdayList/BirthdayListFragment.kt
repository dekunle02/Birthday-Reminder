package com.adeleke.samad.birthdayreminder.ui.birthdayList

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.adeleke.samad.birthdayreminder.*
import com.adeleke.samad.birthdayreminder.databinding.BirthdayListFragmentBinding
import com.adeleke.samad.birthdayreminder.ui.birthdayEdit.BirthdayEditActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BirthdayListFragment : Fragment() {

    private lateinit var viewModel: BirthdayListViewModel

    private var _binding: BirthdayListFragmentBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        Binding and ViewModel
        _binding = BirthdayListFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(BirthdayListViewModel::class.java)


//        RecyclerView
        val adapter = BirthdayListAdapter(requireActivity())
        binding?.recyclerViewBirthdays?.layoutManager = LinearLayoutManager(context)
        binding?.recyclerViewBirthdays?.adapter = adapter


//      Toolbar Set Up
        val toolbarMenuItemListener =
            Toolbar.OnMenuItemClickListener { item ->
                when (item?.itemId) {
                    R.id.action_sort_by_name -> {
                        viewModel.changeState(BirthdayListViewModel.ListState.SORT_NAME)
                    }
                    R.id.action_sort_by_date -> {
                        viewModel.changeState(BirthdayListViewModel.ListState.SORT_DATE)
                    }

                    R.id.action_show_favorites -> {
                        // uncheck the archived bar when archive is clicked
                        binding?.toolbarBirthdayList?.menu?.findItem(R.id.action_show_archived)?.isChecked =
                            false
                        if (item.isChecked) {
                            viewModel.changeState(BirthdayListViewModel.ListState.DEFAULT)
                            item.isChecked = false
                        } else {
                            viewModel.changeState(BirthdayListViewModel.ListState.FAVORITES)
                            item.isChecked = true
                        }
                    }
                    R.id.action_show_archived -> {
                        // uncheck the favorite bar when archive is clicked
                        binding?.toolbarBirthdayList?.menu?.findItem(R.id.action_show_favorites)?.isChecked =
                            false
                        if (item.isChecked) {
                            viewModel.changeState(BirthdayListViewModel.ListState.DEFAULT)
                            item.isChecked = false
                        } else {
                            viewModel.changeState(BirthdayListViewModel.ListState.ARCHIVED)
                            item.isChecked = true
                        }
                    }
                }
                true
            }

        binding?.toolbarBirthdayList?.setOnMenuItemClickListener(toolbarMenuItemListener)

        val menu = binding?.toolbarBirthdayList?.menu
        val searchView: SearchView =
            menu?.findItem(R.id.action_search_list)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter.filter(newText)
                return true
            }
        })


//        LiveData
        viewModel.snack.observe(viewLifecycleOwner, Observer { text ->
            text?.let { binding?.root?.makeSimpleSnack(it) }
        })
        viewModel.uiState.observe(viewLifecycleOwner, Observer { state ->
            state?.let { updateUIState(state) }
        })
        viewModel.recyclerData.observe(viewLifecycleOwner, Observer { data ->
            if (data.isNullOrEmpty()) {
                updateUIState(UiState.EMPTY)
            } else {
                updateUIState(UiState.ACTIVE)
                adapter.data = data
            }
        })

        //      OnCLickListeners
        binding?.fabAddNew?.setOnClickListener {
            MaterialAlertDialogBuilder(it.context)
                .setTitle(resources.getString(R.string.import_contact))
                .setMessage(getString(R.string.import_contact_body))
                .setNegativeButton(getString(R.string.decline)) { dialog, which ->
                    val i = Intent(activity, BirthdayEditActivity::class.java)
                    i.putExtra(BIRTHDAY_IS_NEW_KEY, true)
                    i.putExtra(IMPORT_CONTACT_INTENT_KEY, false)
                    activity?.startActivity(i)
                }
                .setPositiveButton(getString(R.string.import_text)) { dialog, which ->
                    val i = Intent(activity, BirthdayEditActivity::class.java)
                    i.putExtra(BIRTHDAY_IS_NEW_KEY, true)
                    i.putExtra(IMPORT_CONTACT_INTENT_KEY, true)
                    activity?.startActivity(i)
                }
                .show()
        }
        

        return binding?.root
    }

    private fun updateUIState(state: UiState) {
        when (state) {
            UiState.EMPTY -> {
                binding?.progressBar?.visibility = View.GONE
                binding?.emptyLayoutView?.visibility = View.VISIBLE
                binding?.recyclerViewBirthdays?.visibility = View.GONE
            }
            UiState.ACTIVE -> {
                binding?.progressBar?.visibility = View.GONE
                binding?.emptyLayoutView?.visibility = View.GONE
                binding?.recyclerViewBirthdays?.visibility = View.VISIBLE

            }
            UiState.LOADING -> {
                binding?.progressBar?.visibility = View.VISIBLE
                binding?.emptyLayoutView?.visibility = View.GONE
                binding?.recyclerViewBirthdays?.visibility = View.GONE

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshState()
    }

    companion object {
        const val TAG = "BirthdayListFragment"
    }

}