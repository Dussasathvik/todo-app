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
import android.os.IBinder
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
import android.content.*
import android.view.*
import android.widget.*
import com.codegama.todolistapplication.model.Task
import java.lang.Exception
import java.util.*

class CreateTaskBottomSheetFragment : BottomSheetDialogFragment() {
    var unbinder: Unbinder? = null

    @JvmField
    @BindView(R.id.addTaskTitle)
    var addTaskTitle: EditText? = null

    @JvmField
    @BindView(R.id.addTaskDescription)
    var addTaskDescription: EditText? = null

    @JvmField
    @BindView(R.id.taskDate)
    var taskDate: EditText? = null

    @JvmField
    @BindView(R.id.taskTime)
    var taskTime: EditText? = null

    @JvmField
    @BindView(R.id.taskEvent)
    var taskEvent: EditText? = null

    @JvmField
    @BindView(R.id.addTask)
    var addTask: Button? = null
    var taskId = 0
    var isEdit = false
    var task: Task? = null
    var mYear = 0
    var mMonth = 0
    var mDay = 0
    var mHour = 0
    var mMinute = 0
    var setRefreshListener: setRefreshListener? = null
    var alarmManager: AlarmManager? = null
    var timePickerDialog: TimePickerDialog? = null
    var datePickerDialog: DatePickerDialog? = null
    var activity: MainActivity? = null
    private val mBottomSheetBehaviorCallback: BottomSheetCallback? =
        object : BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        }

    fun setTaskId(
        taskId: Int,
        isEdit: Boolean,
        setRefreshListener: setRefreshListener?,
        activity: MainActivity?
    ) {
        this.taskId = taskId
        this.isEdit = isEdit
        this.activity = activity
        this.setRefreshListener = setRefreshListener
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("RestrictedApi", "ClickableViewAccessibility")
    override fun setupDialog(dialog: Dialog?, style: Int) {
        super.setupDialog(dialog, style)
        val contentView = View.inflate(context, R.layout.fragment_create_task, null)
        unbinder = ButterKnife.bind(this, contentView)
        dialog.setContentView(contentView)
        alarmManager = getActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        addTask.setOnClickListener(View.OnClickListener { view: View? -> if (validateFields()) createTask() })
        if (isEdit) {
            showTaskFromId()
        }
        taskDate.setOnTouchListener(OnTouchListener { view: View?, motionEvent: MotionEvent? ->
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                val c = Calendar.getInstance()
                mYear = c[Calendar.YEAR]
                mMonth = c[Calendar.MONTH]
                mDay = c[Calendar.DAY_OF_MONTH]
                datePickerDialog = DatePickerDialog(getActivity(),
                    OnDateSetListener { view1: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                        taskDate.setText(dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                        datePickerDialog.dismiss()
                    }, mYear, mMonth, mDay
                )
                datePickerDialog.getDatePicker().minDate = System.currentTimeMillis() - 1000
                datePickerDialog.show()
            }
            true
        })
        taskTime.setOnTouchListener(OnTouchListener { view: View?, motionEvent: MotionEvent? ->
            if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                // Get Current Time
                val c = Calendar.getInstance()
                mHour = c[Calendar.HOUR_OF_DAY]
                mMinute = c[Calendar.MINUTE]

                // Launch Time Picker Dialog
                timePickerDialog = TimePickerDialog(getActivity(),
                    { view12: TimePicker?, hourOfDay: Int, minute: Int ->
                        taskTime.setText("$hourOfDay:$minute")
                        timePickerDialog.dismiss()
                    }, mHour, mMinute, false
                )
                timePickerDialog.show()
            }
            true
        })
    }

    fun validateFields(): Boolean {
        return if (addTaskTitle.getText().toString().equals("", ignoreCase = true)) {
            Toast.makeText(activity, "Please enter a valid title", Toast.LENGTH_SHORT).show()
            false
        } else if (addTaskDescription.getText().toString().equals("", ignoreCase = true)) {
            Toast.makeText(activity, "Please enter a valid description", Toast.LENGTH_SHORT).show()
            false
        } else if (taskDate.getText().toString().equals("", ignoreCase = true)) {
            Toast.makeText(activity, "Please enter date", Toast.LENGTH_SHORT).show()
            false
        } else if (taskTime.getText().toString().equals("", ignoreCase = true)) {
            Toast.makeText(activity, "Please enter time", Toast.LENGTH_SHORT).show()
            false
        } else if (taskEvent.getText().toString().equals("", ignoreCase = true)) {
            Toast.makeText(activity, "Please enter an event", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun createTask() {
        internal class saveTaskInBackend : AsyncTask<Void?, Void?, Void?>() {
            @SuppressLint("WrongThread")
            override fun doInBackground(vararg voids: Void?): Void? {
                val createTask = Task()
                createTask.taskTitle = addTaskTitle.getText().toString()
                createTask.taskDescrption = addTaskDescription.getText().toString()
                createTask.date = taskDate.getText().toString()
                createTask.lastAlarm = taskTime.getText().toString()
                createTask.event = taskEvent.getText().toString()
                if (!isEdit) DatabaseClient.Companion.getInstance(getActivity()).getAppDatabase()
                    .dataBaseAction()
                    .insertDataIntoTaskList(createTask) else DatabaseClient.Companion.getInstance(
                    getActivity()
                ).getAppDatabase()
                    .dataBaseAction()
                    .updateAnExistingRow(
                        taskId, addTaskTitle.getText().toString(),
                        addTaskDescription.getText().toString(),
                        taskDate.getText().toString(),
                        taskTime.getText().toString(),
                        taskEvent.getText().toString()
                    )
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                super.onPostExecute(aVoid)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    createAnAlarm()
                }
                setRefreshListener.refresh()
                Toast.makeText(getActivity(), "Your event is been added", Toast.LENGTH_SHORT).show()
                dismiss()
            }
        }

        val st = saveTaskInBackend()
        st.execute()
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    fun createAnAlarm() {
        try {
            val items1: Array<String?> =
                taskDate.getText().toString().split("-".toRegex()).toTypedArray()
            val dd = items1[0]
            val month = items1[1]
            val year = items1[2]
            val itemTime: Array<String?> =
                taskTime.getText().toString().split(":".toRegex()).toTypedArray()
            val hour = itemTime[0]
            val min = itemTime[1]
            val cur_cal: Calendar = GregorianCalendar()
            cur_cal.timeInMillis = System.currentTimeMillis()
            val cal: Calendar = GregorianCalendar()
            cal[Calendar.HOUR_OF_DAY] = hour.toInt()
            cal[Calendar.MINUTE] = min.toInt()
            cal[Calendar.SECOND] = 0
            cal[Calendar.MILLISECOND] = 0
            cal[Calendar.DATE] = dd.toInt()
            val alarmIntent = Intent(activity, AlarmBroadcastReceiver::class.java)
            alarmIntent.putExtra("TITLE", addTaskTitle.getText().toString())
            alarmIntent.putExtra("DESC", addTaskDescription.getText().toString())
            alarmIntent.putExtra("DATE", taskDate.getText().toString())
            alarmIntent.putExtra("TIME", taskTime.getText().toString())
            val pendingIntent = PendingIntent.getBroadcast(
                activity,
                count,
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    cal.timeInMillis,
                    pendingIntent
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
                }
                count++
                val intent = PendingIntent.getBroadcast(activity, count, alarmIntent, 0)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        cal.timeInMillis - 600000,
                        intent
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmManager.setExact(
                            AlarmManager.RTC_WAKEUP,
                            cal.timeInMillis - 600000,
                            intent
                        )
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis - 600000, intent)
                    }
                }
                count++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showTaskFromId() {
        internal class showTaskFromId : AsyncTask<Void?, Void?, Void?>() {
            @SuppressLint("WrongThread")
            override fun doInBackground(vararg voids: Void?): Void? {
                task = DatabaseClient.Companion.getInstance(getActivity()).getAppDatabase()
                    .dataBaseAction().selectDataFromAnId(taskId)
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                super.onPostExecute(aVoid)
                setDataInUI()
            }
        }

        val st = showTaskFromId()
        st.execute()
    }

    private fun setDataInUI() {
        addTaskTitle.setText(task.getTaskTitle())
        addTaskDescription.setText(task.getTaskDescrption())
        taskDate.setText(task.getDate())
        taskTime.setText(task.getLastAlarm())
        taskEvent.setText(task.getEvent())
    }

    interface setRefreshListener {
        open fun refresh()
    }

    companion object {
        var count = 0
    }
}