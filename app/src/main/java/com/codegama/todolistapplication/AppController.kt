package com.codegama.todolistapplication

import io.github.inflationx.viewpump.ViewPump.Builder.addInterceptor
import io.github.inflationx.viewpump.ViewPump.Builder.build
import org.junit.runner.RunWith
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.codegama.todolistapplication.activity.MainActivity
import com.codegama.todolistapplication.bottomSheetFragment.CreateTaskBottomSheetFragment.setRefreshListener
import androidx.recyclerview.widget.RecyclerView
import com.codegama.todolistapplication.adapter.TaskAdapter.TaskViewHolder
import android.view.LayoutInflater
import android.view.ViewGroup
import com.codegama.todolistapplication.R
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
import android.widget.EditText
import android.view.View.OnTouchListener
import android.view.MotionEvent
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog.OnTimeSetListener
import android.content.*
import android.widget.TimePicker
import android.widget.Toast

class AppController : Application(), ComponentCallbacks2 {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        ViewPump.init(
            ViewPump.builder()
                .addInterceptor(
                    CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                            .setDefaultFontPath("fonts/nunito_medium.ttf")
                            .setFontAttrId(R.attr.fontPath)
                            .build()
                    )
                )
                .build()
        )
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    companion object {
        private val mInstance: AppController? = null
        @Synchronized
        fun getInstance(): AppController? {
            return mInstance
        }
    }
}