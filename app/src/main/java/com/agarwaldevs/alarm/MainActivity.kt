package com.agarwaldevs.alarm

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity(), CreateAlarm.OnAlarmSavedListener  {

    private lateinit var alarmDatabaseHelper: AlarmDatabaseHelper
    private lateinit var alarmAdapter: AlarmAdapter
    private val notificationPermissionCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        Notifier.createNotificationChannels(this)
        checkAndRequestNotificationPermission()
        alarmDatabaseHelper = AlarmDatabaseHelper(this)
        alarmAdapter = AlarmAdapter()

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = alarmAdapter
        loadAlarms()

        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener {
            val createAlarmFragment = CreateAlarm()

            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(
                R.anim.fly_in_bottom,
                R.anim.fly_out_bottom
            )
            createAlarmFragment.show(transaction, "CreateAlarm")

        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    notificationPermissionCode
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == notificationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onAlarmSaved() {
        loadAlarms()
    }

    @SuppressLint("Range")
    fun loadAlarms() {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val placeholderText: TextView = findViewById(R.id.placeholderText)
        val alarms = alarmDatabaseHelper.fetchAlarms()

        if (alarms.isEmpty()) {
            placeholderText.visibility = android.view.View.VISIBLE
            recyclerView.visibility = android.view.View.GONE
        } else {
            placeholderText.visibility = android.view.View.GONE
            recyclerView.visibility = android.view.View.VISIBLE

            alarmAdapter.submitList(alarms)
        }
    }

    override fun onResume() {
        super.onResume()
        loadAlarms()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                Toast.makeText(this, "Will Update this soon 😅!", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
