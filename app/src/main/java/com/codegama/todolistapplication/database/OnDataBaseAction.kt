package com.codegama.todolistapplication.database

import io.github.inflationx.viewpump.ViewPump.Builder.addInterceptor
import io.github.inflationx.viewpump.ViewPump.Builder.build
import org.junit.runner.RunWith
import com.codegama.todolistapplication.activity.MainActivity
import com.codegama.todolistapplication.bottomSheetFragment.CreateTaskBottomSheetFragment.setRefreshListener
import androidx.recyclerview.widget.RecyclerView
import com.codegama.todolistapplication.adapter.TaskAdapter.TaskViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import com.codegama.todolistapplication.R
import android.content.DialogInterface
import com.codegama.todolistapplication.bottomSheetFragment.CreateTaskBottomSheetFragment
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import com.codegama.todolistapplication.database.DatabaseClient
import butterknife.BindView
import android.widget.TextView
import butterknife.ButterKnife
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import com.codegama.todolistapplication.activity.BaseActivity
import com.codegama.todolistapplication.adapter.TaskAdapter
import android.view.WindowManager
import android.content.ComponentName
import com.codegama.todolistapplication.broadcastReceiver.AlarmBroadcastReceiver
import android.content.pm.PackageManager
import com.bumptech.glide.Glide
import com.codegama.todolistapplication.bottomSheetFragment.ShowCalendarViewBottomSheet
import androidx.recyclerview.widget.LinearLayoutManager
import com.codegama.todolistapplication.activity.AlarmActivity
import com.codegama.todolistapplication.database.OnDataBaseAction
import androidx.sqlite.db.SupportSQLiteOpenHelper
import kotlin.jvm.Volatile
import com.codegama.todolistapplication.database.AppDatabase
import kotlin.jvm.Synchronized
import android.content.Intent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
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
import com.applandeo.materialcalendarview.EventDay
import android.widget.EditText
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.app.DatePickerDialog
import android.view.View.OnTouchListener
import android.view.MotionEvent
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog.OnTimeSetListener
import android.widget.TimePicker
import android.widget.Toast
import androidx.room.*
import com.codegama.todolistapplication.model.Task

@Dao
interface OnDataBaseAction {
    @Query("SELECT * FROM Task")
    open fun getAllTasksList(): MutableList<Task?>?
    @Query("DELETE FROM Task")
    open fun truncateTheList()
    @Insert
    open fun insertDataIntoTaskList(task: Task?)
    @Query("DELETE FROM Task WHERE taskId = :taskId")
    open fun deleteTaskFromId(taskId: Int)
    @Query("SELECT * FROM Task WHERE taskId = :taskId")
    open fun selectDataFromAnId(taskId: Int): Task?

    @Query(
        "UPDATE Task SET taskTitle = :taskTitle, taskDescription = :taskDescription, date = :taskDate, " +
                "lastAlarm = :taskTime, event = :taskEvent WHERE taskId = :taskId"
    )
    open fun updateAnExistingRow(
        taskId: Int,
        taskTitle: String?,
        taskDescription: String?,
        taskDate: String?,
        taskTime: String?,
        taskEvent: String?
    )
}