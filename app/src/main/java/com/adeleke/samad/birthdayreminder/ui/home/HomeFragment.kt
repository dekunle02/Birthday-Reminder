package com.adeleke.samad.birthdayreminder.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.adeleke.samad.birthdayreminder.*
import com.adeleke.samad.birthdayreminder.databinding.HomeFragmentBinding
import com.adeleke.samad.birthdayreminder.ui.birthdayEdit.BirthdayEditActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.prolificinteractive.materialcalendarview.CalendarDay

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private var _binding: HomeFragmentBinding? = null
    private val binding get() = _binding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//                Binding and ViewModel
        _binding = HomeFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)


//          Toolbar
        binding?.tbHome?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_settings -> {
                    val navHostFragment =
                        requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
                    val navController = navHostFragment.navController
                    val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment()
                    navController.navigate(action)
                    true
                }
                else -> {
                    false
                }
            }
        }


//        RecyclerView
        val recentAdapter = RecentListAdapter(requireActivity())
        binding?.rvRecent?.adapter = recentAdapter

        val monthAdapter = MonthListAdapter(requireActivity())
        binding?.rvMonth?.layoutManager = LinearLayoutManager(context)
        binding?.rvMonth?.adapter = monthAdapter


//        Calendar set up
        val calendarView = binding?.calendarView
        calendarView?.selectedDate = CalendarDay.today()
        calendarView?.setOnMonthChangedListener { _, date ->
            date?.let {it->
                viewModel.fetchBirthdaysForCalendar(it)
            }
        }


//        LiveData
        viewModel.snack.observe(viewLifecycleOwner, Observer { text ->
            text?.let { binding?.root?.makeSimpleSnack(it) }
        })
        viewModel.uiState.observe(viewLifecycleOwner, Observer { state ->
            state?.let { updateUIState(state) }
        })
        viewModel.statMap.observe(viewLifecycleOwner, Observer { map ->
            map?.let { updateStatsWithMap(map) }
        })
        viewModel.calendarDates.observe(viewLifecycleOwner, Observer { dates ->
            dates?.let { updateCalendar(dates) }
        })
        viewModel.recentBirthdays.observe(viewLifecycleOwner, Observer { data ->
            if (!data.isNullOrEmpty()) {
                recentAdapter.data = data
            }
        })
        viewModel.monthBirthdays.observe(viewLifecycleOwner, Observer { monthBirthdays ->
            if (!monthBirthdays.isNullOrEmpty()) {
                monthAdapter.data = monthBirthdays
                context?.getColor(monthBirthdays[0]?.monthColor()!!)?.let {
                    binding?.calendarView?.selectionColor = it
                }
            }
        })


//          Click listeners
        binding?.btnHomeAdd?.setOnClickListener {
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


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun updateUIState(state: UiState) {
        when (state) {
            UiState.EMPTY -> {
                binding?.progressBarHome?.visibility = View.GONE
                binding?.svHome?.visibility = View.GONE
                binding?.emptyHome?.visibility = View.VISIBLE
            }
            UiState.ACTIVE -> {
                binding?.progressBarHome?.visibility = View.GONE
                binding?.svHome?.visibility = View.VISIBLE
                binding?.emptyHome?.visibility = View.GONE
            }
            UiState.LOADING -> {
                binding?.progressBarHome?.visibility = View.VISIBLE
                binding?.svHome?.visibility = View.INVISIBLE
                binding?.emptyHome?.visibility = View.GONE
            }
        }
    }

    private fun updateStatsWithMap(map: Map<String, Int>) {
        map["today"]?.let { binding?.tvHomeTodayCount?.text = it.toString() }
        map["month"]?.let { binding?.tvHomeMonthCount?.text = it.toString() }
        map["year"]?.let { binding?.tvHomeYearCount?.text = it.toString() }
    }

    private fun updateCalendar(dateList: List<CalendarDay>) {
        dateList.forEach {
            binding?.calendarView?.setDateSelected(it, true)
        }
    }


    companion object {
        const val TAG = "HomeFragment"

    }
}