package com.weather.app

import android.app.Application
import com.weather.app.data.local.WeatherLocalCache

/**
 * Головний клас застосунку [WeatherApplication].
 * Ініціалізує глобальний контекст для [WeatherLocalCache] при старті процесу,
 * забезпечуючи надійне дискове збереження локацій та погодних даних.
 */
class WeatherApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        WeatherLocalCache.init(this)
    }
}
