package com.codegama.todolistapplication.adapter

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
import android.graphics.Color
import android.view.*
import android.widget.*
import com.codegama.todolistapplication.model.Task
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(
    private val context: MainActivity?,
    private val taskList: MutableList<Task?>?,
    var setRefreshListener: setRefreshListener?
) : RecyclerView.Adapter<TaskViewHolder?>() {
    private val inflater: LayoutInflater?
    var dateFormat: SimpleDateFormat? = SimpleDateFormat("EE dd MMM yyyy", Locale.US)
    var inputDateFormat: SimpleDateFormat? = SimpleDateFormat("dd-M-yyyy", Locale.US)
    var date: Date? = null
    var outputDateString: String? = null
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): TaskViewHolder {
        val view = inflater.inflate(R.layout.item_task, viewGroup, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = taskList.get(position)
        holder.title.setText(task.getTaskTitle())
        holder.description.setText(task.getTaskDescrption())
        holder.time.setText(task.getLastAlarm())
        holder.status.setText(if (task.isComplete()) "COMPLETED" else "UPCOMING")
        holder.options.setOnClickListener(View.OnClickListener { view: View? ->
            showPopUpMenu(
                view,
                position
            )
        })
        try {
            date = inputDateFormat.parse(task.getDate())
            outputDateString = dateFormat.format(date)
            val items1: Array<String?> = outputDateString.split(" ".toRegex()).toTypedArray()
            val day = items1[0]
            val dd = items1[1]
            val month = items1[2]
            holder.day.setText(day)
            holder.date.setText(dd)
            holder.month.setText(month)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showPopUpMenu(view: View?, position: Int) {
        val task = taskList.get(position)
        val popupMenu = PopupMenu(context, view)
        popupMenu.menuInflater.inflate(R.menu.menu, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item: MenuItem? ->
            when (item.getItemId()) {
                R.id.menuDelete -> {
                    val alertDialogBuilder = AlertDialog.Builder(
                        context, R.style.AppTheme_Dialog
                    )
                    alertDialogBuilder.setTitle(R.string.delete_confirmation)
                        .setMessage(R.string.sureToDelete)
                        .setPositiveButton(R.string.yes) { dialog: DialogInterface?, which: Int ->
                            deleteTaskFromId(
                                task.getTaskId(),
                                position
                            )
                        }
                        .setNegativeButton(R.string.no) { dialog: DialogInterface?, which: Int -> dialog.cancel() }
                        .show()
                }
                R.id.menuUpdate -> {
                    val createTaskBottomSheetFragment = CreateTaskBottomSheetFragment()
                    createTaskBottomSheetFragment.setTaskId(
                        task.getTaskId(),
                        true,
                        context,
                        context
                    )
                    createTaskBottomSheetFragment.show(
                        context.getSupportFragmentManager(),
                        createTaskBottomSheetFragment.tag
                    )
                }
                R.id.menuComplete -> {
                    val completeAlertDialog = AlertDialog.Builder(
                        context, R.style.AppTheme_Dialog
                    )
                    completeAlertDialog.setTitle(R.string.confirmation)
                        .setMessage(R.string.sureToMarkAsComplete)
                        .setPositiveButton(R.string.yes) { dialog: DialogInterface?, which: Int ->
                            showCompleteDialog(
                                task.getTaskId(),
                                position
                            )
                        }
                        .setNegativeButton(R.string.no) { dialog: DialogInterface?, which: Int -> dialog.cancel() }
                        .show()
                }
            }
            false
        }
        popupMenu.show()
    }

    fun showCompleteDialog(taskId: Int, position: Int) {
        val dialog = Dialog(context, R.style.AppTheme)
        dialog.setContentView(R.layout.dialog_completed_theme)
        val close = dialog.findViewById<Button?>(R.id.closeButton)
        close.setOnClickListener { view: View? ->
            deleteTaskFromId(taskId, position)
            dialog.dismiss()
        }
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()
    }

    private fun deleteTaskFromId(taskId: Int, position: Int) {
        internal class GetSavedTasks : AsyncTask<Void?, Void?, MutableList<Task?>?>() {
            override fun doInBackground(vararg voids: Void?): MutableList<Task?>? {
                DatabaseClient.Companion.getInstance(context)
                    .getAppDatabase()
                    .dataBaseAction()
                    .deleteTaskFromId(taskId)
                return taskList
            }

            override fun onPostExecute(tasks: MutableList<Task?>?) {
                super.onPostExecute(tasks)
                removeAtPosition(position)
                setRefreshListener.refresh()
            }
        }

        val savedTasks = GetSavedTasks()
        savedTasks.execute()
    }

    private fun removeAtPosition(position: Int) {
        taskList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, taskList.size)
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    inner class TaskViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {
        @JvmField
        @BindView(R.id.day)
        var day: TextView? = null

        @JvmField
        @BindView(R.id.date)
        var date: TextView? = null

        @JvmField
        @BindView(R.id.month)
        var month: TextView? = null

        @JvmField
        @BindView(R.id.title)
        var title: TextView? = null

        @JvmField
        @BindView(R.id.description)
        var description: TextView? = null

        @JvmField
        @BindView(R.id.status)
        var status: TextView? = null

        @JvmField
        @BindView(R.id.options)
        var options: ImageView? = null

        @JvmField
        @BindView(R.id.time)
        var time: TextView? = null

        init {
            ButterKnife.bind(this, view)
        }
    }

    init {
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
}