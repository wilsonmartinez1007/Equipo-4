package com.univalle.inventory.ui.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import com.univalle.inventory.R
import com.univalle.inventory.ui.login.LoginActivity
import com.univalle.inventory.view.MainActivity
import com.univalle.inventory.data.repository.WidgetRepository  // ← CAMBIO AQUÍ
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import com.google.firebase.auth.FirebaseAuth

/**
 * Widget de Android para mostrar el inventario total
 * Implementa arquitectura MVVM con Repository Pattern
 * Utiliza Dagger Hilt para inyección de dependencias
 * Integra Firebase Auth para validación de usuario
 */
class Widget : AppWidgetProvider() {

    companion object {

        private const val ACTION_EYE = "ACTION_EYE"
        private const val ACTION_MANAGER = "ACTION_MANAGER"
        private var isVisible = false

        /**
         * Verifica si hay usuario autenticado en Firebase
         * @return true si hay usuario logueado, false en caso contrario
         */
        private fun isUserLogged(): Boolean {
            return FirebaseAuth.getInstance().currentUser != null
        }

        /**
         * Obtiene el repository desde Hilt usando EntryPoint
         * @param context Contexto de la aplicación
         * @return Instancia de WidgetRepository
         */
        private fun getRepository(context: Context): WidgetRepository {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java
            )
            return entryPoint.widgetRepository()
        }

        /**
         * Crea un PendingIntent para manejar eventos del widget
         * @param context Contexto de la aplicación
         * @param action Acción a ejecutar
         * @param widgetId ID del widget
         * @return PendingIntent configurado
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
         * Muestra el balance total del inventario
         * @param context Contexto de la aplicación
         * @param manager AppWidgetManager para actualizar el widget
         * @param widgetId ID del widget a actualizar
         */
        private fun updateWidgetUI(
            context: Context,
            manager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget)

            // Configurar colores de los iconos
            views.setInt(R.id.ivEye, "setColorFilter", Color.WHITE)
            views.setInt(R.id.ivSettings, "setColorFilter", Color.parseColor("#FA8523"))

            // Configurar listeners de los botones
            views.setOnClickPendingIntent(
                R.id.ivEye,
                createPendingIntent(context, ACTION_EYE, widgetId)
            )
            views.setOnClickPendingIntent(
                R.id.ivSettings,
                createPendingIntent(context, ACTION_MANAGER, widgetId)
            )

            val repo = getRepository(context)

            // Obtener y formatear el balance en segundo plano
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val total = repo.getTotalValue()

                    // Formatear como moneda colombiana
                    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                    formatter.minimumFractionDigits = 2
                    formatter.maximumFractionDigits = 2

                    // Mostrar u ocultar el balance según el estado
                    val balance = if (isVisible) formatter.format(total) else "$ ****"
                    val icon = if (isVisible) R.drawable.eye_open else R.drawable.eye_close

                    views.setTextViewText(R.id.tvBalance, balance)
                    views.setImageViewResource(R.id.ivEye, icon)

                    manager.updateAppWidget(widgetId, views)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // En caso de error, mostrar mensaje genérico
                    views.setTextViewText(R.id.tvBalance, "$ 0.00")
                    manager.updateAppWidget(widgetId, views)
                }
            }
        }
    }

    /**
     * Callback cuando el widget necesita actualizarse
     * Se llama cuando se agrega el widget o cuando se actualiza periódicamente
     */
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidgetUI(context, manager, it) }
    }

    /**
     * Callback para manejar eventos del widget
     * Maneja las acciones de mostrar/ocultar balance y abrir el inventario
     */
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val manager = AppWidgetManager.getInstance(context)
        val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 0)

        when (intent.action) {

            // Acción: Mostrar / ocultar saldo
            ACTION_EYE -> {
                // Validar que el usuario esté autenticado
                if (!isUserLogged()) {
                    val login = Intent(context, LoginActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(login)
                    return
                }

                // Toggle de visibilidad
                isVisible = !isVisible
                updateWidgetUI(context, manager, id)
            }

            // Acción: Abrir gestión de inventario
            ACTION_MANAGER -> {
                if (!isUserLogged()) {
                    // Si no está autenticado, abrir login
                    val loginIntent = Intent(context, LoginActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(loginIntent)
                } else {
                    // Si está autenticado, abrir la MainActivity
                    val homeIntent = Intent(context, MainActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(homeIntent)
                }
            }
        }
    }
}