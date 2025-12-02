package com.univalle.inventory.ui.widget

import com.univalle.inventory.data.repository.WidgetRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent


/**
 * EntryPoint necesario para permitir que el Widget (AppWidgetProvider),
 * que NO es compatible directamente con Hilt, pueda obtener dependencias
 * como el Repository.
 */

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun widgetRepository(): WidgetRepository
}

