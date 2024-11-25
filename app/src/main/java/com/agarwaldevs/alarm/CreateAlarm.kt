package com.agarwaldevs.alarm


import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import com.agarwaldevs.alarm.databinding.CreateAlarmBinding

class CreateAlarm : DialogFragment() {

    private lateinit var binding: CreateAlarmBinding
    private val selectedDays = mutableListOf<String>()
    private val ringtoneRequestCode = 1001
    private val customRingtoneRequestCode = 1002

    override fun onStart() {
        super.onStart()

        val dialog = dialog ?: return
        val window = dialog.window ?: return
        val params = window.attributes
        params.width = ViewGroup.LayoutParams.MATCH_PARENT
        window.attributes = params
        window.setBackgroundDrawableResource(R.drawable.round_border)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = android.app.AlertDialog.Builder(requireActivity())
        val inflater = requireActivity().layoutInflater
        binding = CreateAlarmBinding.inflate(inflater)
        builder.setView(binding.root)

        val defaultRingtoneUri: Uri? = RingtoneManager.getActualDefaultRingtoneUri(requireContext(), RingtoneManager.TYPE_ALL)
        val ringtone = RingtoneManager.getRingtone(requireContext(), defaultRingtoneUri)
        val ringtoneName = ringtone?.getTitle(requireContext()) ?: "Unknown Ringtone"
        selectedRingtoneUri = defaultRingtoneUri
        binding.tvRingtone.text = ringtoneName


        binding.btnRingOnce.setOnClickListener {
            binding.btnRingOnce.setBackgroundColor(Color.parseColor("#00A0E9"))
            binding.btnCustom.setBackgroundColor(Color.parseColor("#303030"))
            hideCustomDaysSection()
        }

        binding.btnCustom.setOnClickListener {
            binding.btnCustom.setBackgroundColor(Color.parseColor("#00A0E9"))
            binding.btnRingOnce.setBackgroundColor(Color.parseColor("#303030"))
            showCustomDaysSection()
        }

        val dayButtons = mapOf(
            binding.btMon to "Mon", binding.btTue to "Tue", binding.btWed to "Wed",
            binding.btThu to "Thu", binding.btFri to "Fri", binding.btSat to "Sat", binding.btSun to "Sun"
        )

        for ((button, day) in dayButtons) {
            button.setOnClickListener {
                toggleDaySelection(button, day)
            }
        }


        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnDone.setOnClickListener {
            saveAlarm()
            dismiss()
        }

        binding.tvRingtone.setOnClickListener {
            openSystemRingtonePicker()
        }

        binding.switchVibrate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Will update in Future to set Vibrate
            } else {
                // Disable vibrate
            }
        }


        val dialog = builder.create()
        dialog.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        return dialog
    }

    private fun openSystemRingtonePicker() {
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_RINGTONE)
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Ringtone")
        startActivityForResult(intent, ringtoneRequestCode)
    }

    private var selectedRingtoneUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == android.app.Activity.RESULT_OK) {
            val ringtoneUri: Uri? = when (requestCode) {
                ringtoneRequestCode -> data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                customRingtoneRequestCode -> data?.data
                else -> null
            }

            ringtoneUri?.let {
                selectedRingtoneUri = it
                val ringtone = RingtoneManager.getRingtone(requireContext(), it)
                val ringtoneName = ringtone?.getTitle(requireContext()) ?: "Unknown Ringtone"
                binding.tvRingtone.text = ringtoneName
            } ?: run {
                Toast.makeText(requireContext(), "No ringtone selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    interface OnAlarmSavedListener {
        fun onAlarmSaved()
    }

    private var listener: OnAlarmSavedListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnAlarmSavedListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnAlarmSavedListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun saveAlarm() {
        var alarmName = binding.etAlarmName.text.toString()
        val time = "${binding.timePicker.hour}:${binding.timePicker.minute}"
        val ringtoneUri = selectedRingtoneUri?.toString()
        val vibrate = binding.switchVibrate.isChecked

        if (alarmName.isNullOrEmpty()){
            alarmName = "Alarm"
        }

        val alarm = Alarm(
            time = time,
            name = alarmName,
            days = selectedDays,
            ringtone = ringtoneUri.toString(),
            vibrate = vibrate,
            isEnabled = true
        )

        val dbHelper = AlarmDatabaseHelper(requireContext())
        dbHelper.addAlarm(alarm)
        AlarmScheduler.scheduleAlarm(requireContext(),alarm)
        listener?.onAlarmSaved()
        Toast.makeText(requireContext(), alarm.name + " Scheduled!", Toast.LENGTH_SHORT).show()
    }


    private fun showCustomDaysSection() {
        binding.customDays.visibility = android.view.View.VISIBLE
    }

    private fun hideCustomDaysSection() {
        binding.customDays.visibility = android.view.View.GONE
    }

    private fun toggleDaySelection(button: Button, day: String) {
        val selectedColor = ContextCompat.getColor(requireContext(), R.color.selectedColor)
        val unselectedColor = ContextCompat.getColor(requireContext(), R.color.unselectedColor)
        if (selectedDays.contains(day)) {
            selectedDays.remove(day)
            ViewCompat.setBackgroundTintList(button, ColorStateList.valueOf(unselectedColor))
        } else {
            selectedDays.add(day)
            ViewCompat.setBackgroundTintList(button, ColorStateList.valueOf(selectedColor))
        }
    }
}
