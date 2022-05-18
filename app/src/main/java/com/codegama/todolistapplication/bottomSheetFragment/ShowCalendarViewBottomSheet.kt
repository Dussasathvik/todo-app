package com.codegama.todolistapplication.bottomSheetFragment

import io.github.inflationx.viewpump.ViewPump.Builder.addInterceptor
import io.github.inflationx.viewpump.ViewPump.Builder.build
import org.junit.runner.RunWith
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.codegama.todolistapplication.activity.MainActivity
import com.codegama.todolistapplication.bottomSheetFragment.CreateTaskBottomSheetFragment.setRefreshListener
import androidx.recyclerview.widget.RecyclerView
import com.codegama.todolistapplication.adapter.TaskAdapter.TaskViewHolder
import com.codegama.todolistapplication.R
import android.content.DialogInterface
import com.codegama.todolistapplication.bottomSheetFragment.CreateTaskBottomSheetFragment
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import com.codegama.todolistapplication.database.DatabaseClient
import butterknife.BindView
import butterknife.ButterKnife
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import com.codegama.todolistapplication.activity.BaseActivity
import com.codegama.todolistapplication.adapter.TaskAdapter
import android.content.ComponentName
import com.codegama.todolistapplication.broadcastReceiver.AlarmBroadcastReceiver
import android.content.pm.PackageManager
import com.bumptech.glide.Glide
import com.codegama.todolistapplication.bottomSheetFragment.ShowCalendarViewBottomSheet
import androidx.recyclerview.widget.LinearLayoutManager
import com.codegama.todolistapplication.activity.AlarmActivity
import androidx.room.Database
import androidx.room.RoomDatabase
import com.codegama.todolistapplication.database.OnDataBaseAction
import androidx.room.DatabaseConfiguration
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.room.InvalidationTracker
import kotlin.jvm.Volatile
import com.codegama.todolistapplication.database.AppDatabase
import kotlin.jvm.Synchronized
import androidx.room.Room
import androidx.room.Dao
import android.content.Intent
import android.os.IBinder
import android.content.BroadcastReceiver
import android.content.ComponentCallbacks2
import androidx.appcompat.app.AppCompatDelegate
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.calligraphy3.CalligraphyConfig
import com.codegama.todolistapplication.AppController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import butterknife.Unbinder
import com.google.android.material.bottomsheet.BottomSheetBehavior.BottomSheetCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.annotation.RequiresApi
import android.os.Build
import android.annotation.SuppressLint
import android.app.*
import com.applandeo.materialcalendarview.EventDay
import android.view.View.OnTouchListener
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog.OnTimeSetListener
import android.view.*
import android.widget.*
import com.applandeo.materialcalendarview.CalendarView
import com.codegama.todolistapplication.model.Task
import java.util.*

class ShowCalendarViewBottomSheet : BottomSheetDialogFragment() {
    var unbinder: Unbinder? = null
    var activity: MainActivity? = null

    @JvmField
    @BindView(R.id.back)
    var back: ImageView? = null

    @JvmField
    @BindView(R.id.calendarView)
    var calendarView: CalendarView? = null
    var tasks: MutableList<Task?>? = ArrayList()
    private val mBottomSheetBehaviorCallback: BottomSheetCallback? =
        object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi", "ClickableViewAccessibility")
    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.fragment_calendar_view, null)
        unbinder = ButterKnife.bind(this, contentView)
        dialog.setContentView(contentView)
        calendarView.setHeaderColor(R.color.colorAccent)
        getSavedTasks()
        back.setOnClickListener(View.OnClickListener { view: View? -> dialog.dismiss() })
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun getSavedTasks() {
        internal class GetSavedTasks : AsyncTask<Void?, Void?, MutableList<Task?>?>() {
            override fun doInBackground(vararg voids: Void?): MutableList<Task?>? {
                tasks = DatabaseClient.Companion.getInstance(getActivity())
                    .getAppDatabase()
                    .dataBaseAction()
                    .getAllTasksList()
                return tasks
            }

            override fun onPostExecute(tasks: MutableList<Task?>?) {
                super.onPostExecute(tasks)
                calendarView.setEvents(getHighlitedDays())
            }
        }

        val savedTasks = GetSavedTasks()
        savedTasks.execute()
    }

    fun getHighlitedDays(): MutableList<EventDay?>? {
        val events: MutableList<EventDay?> = ArrayList()
        for (i in tasks.indices) {
            val calendar = Calendar.getInstance()
            val items1: Array<String?> = tasks.get(i).getDate().split("-".toRegex()).toTypedArray()
            val dd = items1[0]
            val month = items1[1]
            val year = items1[2]
            calendar[Calendar.DAY_OF_MONTH] = dd.toInt()
            calendar[Calendar.MONTH] = month.toInt() - 1
            calendar[Calendar.YEAR] = year.toInt()
            events.add(EventDay(calendar, R.drawable.dot))
        }
        return events
    }
}