package com.codegama.todolistapplication.activity

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.codegama.todolistapplication.R

class AlarmActivity : BaseActivity() {
    @JvmField
    @BindView(R.id.imageView)
    var imageView: ImageView? = null

    @JvmField
    @BindView(R.id.title)
    var title: TextView? = null

    @JvmField
    @BindView(R.id.description)
    var description: TextView? = null

    @JvmField
    @BindView(R.id.timeAndData)
    var timeAndData: TextView? = null

    @JvmField
    @BindView(R.id.closeButton)
    var closeButton: Button? = null
    var mediaPlayer: MediaPlayer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alarm)
        ButterKnife.bind(this)
        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.notification)
        mediaPlayer?.start()
        if (intent.extras != null) {
            title?.setText(intent.getStringExtra("TITLE"))
            description?.setText(intent.getStringExtra("DESC"))
            timeAndData?.setText(intent.getStringExtra("DATE") + ", " + intent.getStringExtra("TIME"))
        }
        Glide.with(applicationContext).load(R.drawable.alert).into(imageView!!)
        closeButton?.setOnClickListener(View.OnClickListener { view: View? -> finish() })
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    companion object {
        private val inst: AlarmActivity? = null
        fun instance(): AlarmActivity? {
            return inst
        }
    }
}