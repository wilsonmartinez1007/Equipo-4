package com.univalle.inventory.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.widget.RemoteViews
import com.univalle.inventory.R
import com.univalle.inventory.ui.login.LoginActivity
import com.univalle.inventory.view.MainActivity
import com.univalle.inventory.data.repository.WidgetRepository
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth

/**
 * Widget de Android para mostrar el inventario total
 */
class Widget : AppWidgetProvider() {

    companion object {
        private const val ACTION_EYE = "ACTION_EYE"
        private const val ACTION_MANAGER = "ACTION_MANAGER"
        const val ACTION_LOGOUT = "com.univalle.inventory.LOGOUT"
        private var isVisible = false

        /**
         * Verifica el estado real del usuario en Firebase
         */
        private fun verifyAuth(
            onSuccess: () -> Unit,
            onFailure: () -> Unit
        ) {
            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser

            if (user == null) {
                onFailure()
                return
            }

            user.reload()
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener {
                    auth.signOut()
                    onFailure()
                }
        }

        /**
         * Obtiene el repository desde Hilt usando EntryPoint
         */
        private fun getRepository(context: Context): WidgetRepository {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            return entryPoint.widgetRepository()
        }

        /**
         * Crea un PendingIntent para acciones del widget
         */
        private fun createPendingIntent(
            context: Context,
            action: String,
            widgetId: Int
        ): PendingIntent {
            val intent = Intent(context, Widget::class.java).apply {
                this.action = action
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }

            return PendingIntent.getBroadcast(
                context,
                widgetId + action.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        /**
         * Actualiza la interfaz del widget
         */
        private fun updateWidgetUI(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget)

            views.setInt(R.id.ivEye, "setColorFilter", Color.WHITE)
            views.setInt(R.id.ivSettings, "setColorFilter", Color.parseColor("#FA8523"))

            views.setOnClickPendingIntent(
                R.id.ivEye,
                createPendingIntent(context, ACTION_EYE, widgetId)
            )
            views.setOnClickPendingIntent(
                R.id.ivSettings,
                createPendingIntent(context, ACTION_MANAGER, widgetId)
            )

            val repo = getRepository(context)

            // Siempre verificar usuario antes de mostrar saldo
            val auth = FirebaseAuth.getInstance()
            if (auth.currentUser == null) {
                isVisible = false
                views.setTextViewText(R.id.tvBalance, "$ ****")
                views.setImageViewResource(R.id.ivEye, R.drawable.eye_close)
                manager.updateAppWidget(widgetId, views)
                return
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val total = repo.getTotalValue()

                    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                    formatter.minimumFractionDigits = 2
                    formatter.maximumFractionDigits = 2

                    val balance = if (isVisible) formatter.format(total) else "$ ****"
                    val icon = if (isVisible) R.drawable.eye_open else R.drawable.eye_close

                    views.setTextViewText(R.id.tvBalance, balance)
                    views.setImageViewResource(R.id.ivEye, icon)

                    manager.updateAppWidget(widgetId, views)

                } catch (e: Exception) {
                    e.printStackTrace()
                    views.setTextViewText(R.id.tvBalance, "$ 0.00")
                    manager.updateAppWidget(widgetId, views)
                }
            }
        }

        /**
         * Actualiza todos los widgets activos
         */
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, Widget::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            context.sendBroadcast(intent)
        }
    }

    /**
     * Se ejecuta cuando el widget se actualiza
     */
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidgetUI(context, manager, it) }
    }

    /**
     * Maneja las acciones del usuario
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val manager = AppWidgetManager.getInstance(context)
        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)

        // Detectar logout desde la app
        if (intent.action == ACTION_LOGOUT) {
            isVisible = false
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, Widget::class.java)
            )
            ids.forEach { updateWidgetUI(context, manager, it) }
            return
        }

        // Verificar estado de autenticación antes de cualquier acción
        if (FirebaseAuth.getInstance().currentUser == null) {
            isVisible = false
        }

        when (intent.action) {

            ACTION_EYE -> {
                verifyAuth(
                    onSuccess = {
                        isVisible = !isVisible
                        updateWidgetUI(context, manager, id)
                    },
                    onFailure = {
                        val login = Intent(context, LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            putExtra("from_widget", true)
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                        }
                        context.startActivity(login)
                    }
                )
            }

            ACTION_MANAGER -> {
                verifyAuth(
                    onSuccess = {
                        val homeIntent = Intent(context, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(homeIntent)
                    },
                    onFailure = {
                        val loginIntent = Intent(context, LoginActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            putExtra("from_widget", true)
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                        }
                        context.startActivity(loginIntent)
                    }
                )
            }
        }
    }
}