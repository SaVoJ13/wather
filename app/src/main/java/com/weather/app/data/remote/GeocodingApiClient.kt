package com.weather.app.data.remote

import com.weather.app.data.mapper.GeocodingMapper
import com.weather.app.data.remote.dto.GeocodingResponse
import com.weather.app.domain.model.LocationModel

/**
 * Клієнт геокодування Open-Meteo з автономним мок-фолбеком на випадок відсутності мережі.
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
        ),
        LocationModel(
            id = 756135L,
            name = "Варшава",
            country = "Польща",
            admin1 = "Мазовецьке воєводство",
            latitude = 52.2297,
            longitude = 21.0122,
            timezone = "Europe/Warsaw"
        ),
        LocationModel(
            id = 2643743L,
            name = "Лондон",
            country = "Велика Британія",
            admin1 = "Англія",
            latitude = 51.5085,
            longitude = -0.1257,
            timezone = "Europe/London"
        ),
        LocationModel(
            id = 2950159L,
            name = "Берлін",
            country = "Німеччина",
            admin1 = "Берлін",
            latitude = 52.5244,
            longitude = 13.4105,
            timezone = "Europe/Berlin"
        )
    )

    suspend fun search(query: String, language: String = "uk"): Result<List<LocationModel>> {
        val cleanQuery = query.trim()
        if (cleanQuery.isEmpty()) {
            return Result.success(emptyList())
        }

        // Якщо передано реальний мережевий обробник — викликаємо його
        if (networkSearch != null) {
            val netResult = networkSearch.invoke(cleanQuery, language)
            if (netResult.isSuccess) {
                val list = GeocodingMapper.toDomainList(netResult.getOrThrow())
                return Result.success(list)
            }
        }

        // Мок-фолбек пошук по локальній базі міст
        val filtered = mockCities.filter { city ->
            city.name.contains(cleanQuery, ignoreCase = true) ||
                    city.country.contains(cleanQuery, ignoreCase = true) ||
                    (city.admin1?.contains(cleanQuery, ignoreCase = true) == true)
        }

        return Result.success(filtered)
    }
}
