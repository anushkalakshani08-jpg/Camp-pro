package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.CampRentDatabase
import com.example.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RentalReturnReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val database = CampRentDatabase.getDatabase(context)
        val rentalDao = database.rentalDao()

        val startOfDay = DateUtils.getTodayStartMs()
        val endOfDay = DateUtils.getTodayEndMs()

        CoroutineScope(Dispatchers.IO).launch {
            val dueRentals = rentalDao.getRentalsDueOnDate(startOfDay, endOfDay)
            dueRentals.forEach { bill ->
                NotificationHelper.showReturnDueNotification(
                    context = context,
                    billNumber = bill.billNumber,
                    customerName = bill.customerName,
                    customerPhone = bill.customerPhone
                )
            }
        }
    }
}
