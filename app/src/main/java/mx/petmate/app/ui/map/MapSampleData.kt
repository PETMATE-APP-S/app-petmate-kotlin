package mx.petmate.app.ui.map

import mx.petmate.app.R

/**
 * Negocios de ejemplo de la maqueta, solo para previews y para la pantalla
 * mientras no exista su ViewModel. No se usan como datos reales.
 */
internal object MapSampleData {

    val businesses: List<MapBusiness> = listOf(
        MapBusiness(
            id = "b1",
            name = "Veterinaria San Marcos",
            type = BusinessType.Vet,
            mapX = 470f,
            mapY = 440f,
            distance = "600 m",
            isOpen = true,
            scheduleTime = "20:00",
            phone = "771 123 4567",
            hasWhatsApp = true,
            photo = R.drawable.sample_business_vet,
        ),
        MapBusiness(
            id = "b2",
            name = "Patitas Felices",
            type = BusinessType.Spa,
            mapX = 330f,
            mapY = 610f,
            distance = "1.1 km",
            isOpen = true,
            scheduleTime = "19:00",
            phone = "771 222 0190",
            hasWhatsApp = true,
            photo = R.drawable.sample_business_spa,
        ),
        MapBusiness(
            id = "b3",
            name = "Croquetas El Sabueso",
            type = BusinessType.Shop,
            mapX = 540f,
            mapY = 620f,
            distance = "900 m",
            isOpen = true,
            scheduleTime = "21:00",
            phone = "771 300 4411",
            hasWhatsApp = true,
            photo = R.drawable.sample_business_shop,
        ),
        MapBusiness(
            id = "b4",
            name = "Hospital Veterinario Tula",
            type = BusinessType.Vet,
            mapX = 650f,
            mapY = 300f,
            distance = "2.3 km",
            isOpen = true,
            scheduleTime = null,
            phone = "771 555 0001",
            hasWhatsApp = true,
        ),
        MapBusiness(
            id = "b5",
            name = "Guau Spa",
            type = BusinessType.Spa,
            mapX = 220f,
            mapY = 420f,
            distance = "1.8 km",
            isOpen = false,
            scheduleTime = "10:00",
            phone = "771 410 7788",
            hasWhatsApp = false,
        ),
        MapBusiness(
            id = "b6",
            name = "Mundo Mascota",
            type = BusinessType.Shop,
            mapX = 300f,
            mapY = 290f,
            distance = "1.5 km",
            isOpen = true,
            scheduleTime = "20:30",
            phone = "771 610 2233",
            hasWhatsApp = true,
        ),
        MapBusiness(
            id = "b7",
            name = "Clínica Huellitas",
            type = BusinessType.Vet,
            mapX = 610f,
            mapY = 800f,
            distance = "3.0 km",
            isOpen = false,
            scheduleTime = "9:00",
            phone = "771 700 8899",
            hasWhatsApp = false,
        ),
    )

    val state = MapUiState(
        businesses = businesses,
        remindersCount = 3,
    )
}
