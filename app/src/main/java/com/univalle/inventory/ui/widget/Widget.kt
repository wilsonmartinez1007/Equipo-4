package com.univalle.inventory.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.univalle.inventory.R
import com.univalle.inventory.data.InventoryDB
import com.univalle.inventory.ui.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class Widget : AppWidgetProvider() {

    companion object {
        private const val ACTION_EYE = "ACTION_EYE"
        private const val ACTION_MANAGER = "ACTION_MANAGER"
        private var isVisible = true

        private fun createPendingIntent(
            context: Context,
            action: String,
            appWidgetId: Int
        ): PendingIntent {
            val intent = Intent(context, Widget::class.java).apply {
                this.action = action
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }

            val requestCode = when (action) {
                ACTION_EYE -> appWidgetId + 1000
                ACTION_MANAGER -> appWidgetId + 2000
                else -> appWidgetId
            }

            return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget)

            views.setInt(R.id.ivEye, "setColorFilter", Color.WHITE)
            views.setInt(R.id.ivSettings, "setColorFilter", Color.parseColor("#FF8000"))

            views.setOnClickPendingIntent(
                R.id.ivEye,
                createPendingIntent(context, ACTION_EYE, appWidgetId)
            )
            views.setOnClickPendingIntent(
                R.id.ivSettings,
                createPendingIntent(context, ACTION_MANAGER, appWidgetId)
            )

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dao = InventoryDB.getDatabase(context).inventoryDao()
                    val totalValue = dao.getAllInventories().sumOf { it.price * it.quantity }

                    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                    formatter.minimumFractionDigits = 2
                    formatter.maximumFractionDigits = 2

                    val formattedValue = formatter.format(totalValue)

                    views.setTextViewText(
                        R.id.tvBalance,
                        if (isVisible) formattedValue else "$ ****"
                    )

                    val eyeIcon = if (isVisible) R.drawable.eye_open else R.drawable.eye_close
                    views.setImageViewResource(R.id.ivEye, eyeIcon)

                    views.setInt(R.id.ivEye, "setColorFilter", Color.WHITE)

                    appWidgetManager.updateAppWidget(appWidgetId, views)

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)

        when (intent.action) {
            ACTION_EYE -> {
                isVisible = !isVisible
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }

            ACTION_MANAGER -> {
                val targetIntent = Intent(context, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                context.startActivity(targetIntent)
            }
        }
    }
}