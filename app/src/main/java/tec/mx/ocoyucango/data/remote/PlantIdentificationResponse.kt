// data/remote/PlantIdentificationResponse.kt
package tec.mx.ocoyucango.data.remote

data class PlantIdentificationResponse(
    val query: Query,
    val language: String,
    val preferedReferential: String,
    val results: List<Result>,
    val remainingIdentificationRequests: Int,
    val version: String
)

data class Query(
    val project: String,
    val organs: List<String>,
    val images: List<String>
)

data class Result(
    val score: Double,
    val species: Species
)

data class Species(
    val scientificNameWithoutAuthor: String,
    val scientificNameAuthorship: String,
    val scientificName: String,
    val genus: Genus,
    val family: Family,
    val commonNames: List<String>?,
    val bibliographicCitation: String?
)

data class Genus(
    val scientificNameWithoutAuthor: String,
    val scientificNameAuthorship: String,
    val scientificName: String
)

data class Family(
    val scientificNameWithoutAuthor: String,
    val scientificNameAuthorship: String,
    val scientificName: String
)
