package com.weather.app.data.remote

import com.weather.app.data.remote.dto.GeocodingResponse
import com.weather.app.domain.model.LocationModel

/**
 * Клієнт геокодування Open-Meteo з підтримкою багатомовності, транслітерації (PL/UK)
 * та локальною базою популярних міст для роботи без інтернету.
 * 
 * Першоджерело: https://geocoding-api.open-meteo.com/v1/search?name={query}&count=10&language=uk&format=json
 * Ліцензія: CC BY 4.0.
 */
class GeocodingApiClient(
    private val networkSearch: (suspend (query: String, language: String) -> Result<GeocodingResponse>)? = { query, lang ->
        OpenMeteoHttpClient.searchCities(query, lang)
    }
) {

    private val mockCities = listOf(
        LocationModel(
            id = 3088006L,
            name = "Пшецлав (Przecław)",
            country = "Польща",
            admin1 = "Західнопоморське воєводство (передмістя Щецина)",
            latitude = 53.3745,
            longitude = 14.4725,
            timezone = "Europe/Warsaw"
        ),
        LocationModel(
            id = 3083829L,
            name = "Щецин (Szczecin)",
            country = "Польща",
            admin1 = "Західнопоморське воєводство",
            latitude = 53.4285,
            longitude = 14.5528,
            timezone = "Europe/Warsaw"
        ),
        LocationModel(
            id = 756135L,
            name = "Варшава (Warszawa)",
            country = "Польща",
            admin1 = "Мазовецьке воєводство",
            latitude = 52.2297,
            longitude = 21.0122,
            timezone = "Europe/Warsaw"
        ),
        LocationModel(
            id = 703448L,
            name = "Київ",
            country = "Україна",
            admin1 = "Київська область",
            latitude = 50.4501,
            longitude = 30.5234,
            timezone = "Europe/Kyiv"
        ),
        LocationModel(
            id = 702550L,
            name = "Львів",
            country = "Україна",
            admin1 = "Львівська область",
            latitude = 49.8397,
            longitude = 24.0297,
            timezone = "Europe/Kyiv"
        ),
        LocationModel(
            id = 698740L,
            name = "Одеса",
            country = "Україна",
            admin1 = "Одеська область",
            latitude = 46.4825,
            longitude = 30.7233,
            timezone = "Europe/Kyiv"
        ),
        LocationModel(
            id = 706483L,
            name = "Харків",
            country = "Україна",
            admin1 = "Харківська область",
            latitude = 49.9935,
            longitude = 36.2304,
            timezone = "Europe/Kyiv"
        ),
        LocationModel(
            id = 709930L,
            name = "Дніпро",
            country = "Україна",
            admin1 = "Дніпропетровська область",
            latitude = 48.4647,
            longitude = 35.0462,
            timezone = "Europe/Kyiv"
        )
    )

    // Словник транслітерації кирилиці в польські/латинські топоніми
    private val translitAliases = mapOf(
        "пшецлав" to "Przecław",
        "щецин" to "Szczecin",
        "щецін" to "Szczecin",
        "варшава" to "Warszawa",
        "краків" to "Krakow",
        "вроцлав" to "Wroclaw",
        "познань" to "Poznan",
        "гданськ" to "Gdansk",
        "колбасково" to "Kolbaskowo"
    )

    suspend fun search(query: String, language: String = "uk"): Result<List<LocationModel>> {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            return Result.success(emptyList())
        }

        // Перевірка відповідності в локальній базі
        val localMatches = mockCities.filter { city ->
            city.name.contains(cleanQuery, ignoreCase = true) ||
                    (city.admin1?.contains(cleanQuery, ignoreCase = true) == true) ||
                    city.country.contains(cleanQuery, ignoreCase = true)
        }

        // Якщо мережевий клієнт доступний — робимо запит
        if (networkSearch != null) {
            // Шукаємо за оригінальним запитом
            val netResult = networkSearch.invoke(cleanQuery, language)
            var networkCities = netResult.getOrNull()?.results?.map { item ->
                LocationModel(
                    id = item.id,
                    name = item.name,
                    country = item.country ?: "",
                    admin1 = item.admin1,
                    latitude = item.latitude,
                    longitude = item.longitude,
                    timezone = item.timezone ?: "auto"
                )
            } ?: emptyList()

            // Якщо запит був кирилицею і нічого не знайдено, або є прямий псевдонім (наприклад: "пшецлав" -> "Przecław")
            val alias = translitAliases[cleanQuery.lowercase()]
            if (networkCities.isEmpty() && alias != null) {
                val aliasResult = networkSearch.invoke(alias, "en")
                networkCities = aliasResult.getOrNull()?.results?.map { item ->
                    LocationModel(
                        id = item.id,
                        name = item.name,
                        country = item.country ?: "",
                        admin1 = item.admin1,
                        latitude = item.latitude,
                        longitude = item.longitude,
                        timezone = item.timezone ?: "auto"
                    )
                } ?: emptyList()
            }

            // Об'єднуємо результати з дедуплікацією за координатами
            val combined = (networkCities + localMatches).distinctBy { "${it.latitude.toInt()},${it.longitude.toInt()}" }
            if (combined.isNotEmpty()) {
                return Result.success(combined)
            }
        }

        return Result.success(localMatches)
    }
}
