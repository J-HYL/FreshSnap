import com.squareup.moshi.Json

data class NutrimentsDto(

    @Json(name = "energy-kcal_100g")
    val energyKcal100g: Double?,

    @Json(name = "energy-kj_100g")
    val energyKj100g: Double?,

    @Json(name = "fat_100g")
    val fat100g: Double?,

    @Json(name = "saturated-fat_100g")
    val saturatedFat100g: Double?,

    @Json(name = "carbohydrates_100g")
    val carbohydrates100g: Double?,

    @Json(name = "sugars_100g")
    val sugars100g: Double?,

    @Json(name = "proteins_100g")
    val proteins100g: Double?,

    @Json(name = "salt_100g")
    val salt100g: Double?,

    @Json(name = "fiber_100g")
    val fiber100g: Double?,

    @Json(name = "sodium_100g")
    val sodium100g: Double?
)