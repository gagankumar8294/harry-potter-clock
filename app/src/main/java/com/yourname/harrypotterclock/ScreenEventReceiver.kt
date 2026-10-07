package com.yourname.harrypotterclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ScreenEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_SCREEN_ON) {
            val lockIntent = Intent(context, HarryPotterLockActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            try {
                context.startActivity(lockIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
