package com.agarwaldevs.alarm

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*
import androidx.recyclerview.widget.RecyclerView
import com.agarwaldevs.alarm.databinding.AlarmItemBinding

class AlarmAdapter : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    private var alarms = listOf<Alarm>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val binding = AlarmItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlarmViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        holder.bind(alarms[position])
    }

    override fun getItemCount() = alarms.size

    fun submitList(newAlarms: List<Alarm>) {
        alarms = newAlarms
        notifyDataSetChanged()
    }

    class AlarmViewHolder(private val binding: AlarmItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(alarm: Alarm) {

            val timeFormat24Hour = SimpleDateFormat("HH:mm", Locale("en", "IN"))
            val timeFormat12Hour = SimpleDateFormat("hh:mm a", Locale("en", "IN"))

            try {
                val date = timeFormat24Hour.parse(alarm.time)
                if (date != null) {
                    val formattedTime = timeFormat12Hour.format(date)
                    val timeParts = formattedTime.split(" ")
                    binding.tvTime.text = timeParts[0]
                    binding.tvAM.text = timeParts[1]
                }
            } catch (e: Exception) {
                Log.e("AlarmAdapter", "Time parsing error: ${e.message}")
            }

            binding.alarmSwitch.isChecked = alarm.isEnabled
            val daysStr = alarm.days.joinToString(", ")
            if (daysStr.isEmpty()){
                binding.tvDays.text = "Ring Once"
            }else{
                binding.tvDays.text = daysStr
            }

            if (alarm.name != "Alarm"){
                binding.tvName.text = alarm.name
                binding.tvName.visibility = View.VISIBLE
            }

            binding.alarmSwitch.setOnCheckedChangeListener { _, isChecked ->
                val dbHelper = AlarmDatabaseHelper(binding.root.context)
                dbHelper.toggleAlarm(alarm.id, isChecked)
                alarm.isEnabled = isChecked

                if (isChecked) {
                    AlarmScheduler.scheduleAlarm(binding.root.context, alarm)
                    Toast.makeText(binding.root.context,alarm.name + " Scheduled!", Toast.LENGTH_SHORT).show()
                } else {
                    AlarmScheduler.cancelAlarm(binding.root.context, alarm.id)
                    Toast.makeText(binding.root.context,alarm.name + " Cancelled!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
