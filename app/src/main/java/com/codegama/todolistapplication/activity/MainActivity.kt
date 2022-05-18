package com.codegama.todolistapplication.activity

import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.codegama.todolistapplication.R
import com.codegama.todolistapplication.activity.MainActivity
import com.codegama.todolistapplication.adapter.TaskAdapter
import com.codegama.todolistapplication.bottomSheetFragment.CreateTaskBottomSheetFragment
import com.codegama.todolistapplication.bottomSheetFragment.CreateTaskBottomSheetFragment.setRefreshListener
import com.codegama.todolistapplication.bottomSheetFragment.ShowCalendarViewBottomSheet
import com.codegama.todolistapplication.broadcastReceiver.AlarmBroadcastReceiver
import com.codegama.todolistapplication.database.DatabaseClient
import com.codegama.todolistapplication.model.Task
import java.util.*

class MainActivity : BaseActivity(), setRefreshListener {
    @JvmField
    @BindView(R.id.taskRecycler)
    var taskRecycler: RecyclerView? = null

    @JvmField
    @BindView(R.id.addTask)
    var addTask: TextView? = null
    var taskAdapter: TaskAdapter? = null
    var tasks: MutableList<Task?>? = ArrayList()

    @JvmField
    @BindView(R.id.noDataImage)
    var noDataImage: ImageView? = null

    @JvmField
    @BindView(R.id.calendar)
    var calendar: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)
        setUpAdapter()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val receiver = ComponentName(this, AlarmBroadcastReceiver::class.java)
        val pm = packageManager
        pm.setComponentEnabledSetting(
            receiver,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        noDataImage?.let { Glide.with(applicationContext).load(R.drawable.first_note).into(it) }
        addTask?.setOnClickListener(View.OnClickListener { view: View? ->
            val createTaskBottomSheetFragment = CreateTaskBottomSheetFragment()
            createTaskBottomSheetFragment.setTaskId(0, false, this, this@MainActivity)
            createTaskBottomSheetFragment.show(
                supportFragmentManager,
                createTaskBottomSheetFragment.tag
            )
        })
        getSavedTasks()
        calendar?.setOnClickListener(View.OnClickListener { view: View? ->
            val showCalendarViewBottomSheet = ShowCalendarViewBottomSheet()
            showCalendarViewBottomSheet.show(
                supportFragmentManager,
                showCalendarViewBottomSheet.tag
            )
        })
    }

    fun setUpAdapter() {
        taskAdapter = TaskAdapter(this, tasks, this)
        taskRecycler?.setLayoutManager(LinearLayoutManager(applicationContext))
        taskRecycler?.setAdapter(taskAdapter)
    }

    private fun getSavedTasks() {
        class GetSavedTasks : AsyncTask<Void?, Void?, MutableList<Task?>?>() {
            override fun doInBackground(vararg voids: Void?): MutableList<Task?>? {
                tasks = DatabaseClient.Companion.getInstance(applicationContext)
                    ?.getAppDatabase()
                    ?.dataBaseAction()
                    ?.getAllTasksList()
                return tasks
            }

            override fun onPostExecute(tasks: MutableList<Task?>?) {
                super.onPostExecute(tasks)
                if (tasks != null) {
                    noDataImage?.setVisibility(if (tasks.isEmpty()) View.VISIBLE else View.GONE)
                }
                setUpAdapter()
            }
        }

        val savedTasks = GetSavedTasks()
        savedTasks.execute()
    }

    override fun refresh() {
        getSavedTasks()
    }
}