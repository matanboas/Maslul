package com.matanboas.maslul.models

data class Trail(
    val name: String,
    val region: String,
    val location: String,
    val access: List<String>,
    val difficulty: String,
    val lengthKm: Float,
    val trailType: String,
    val entranceFee: String,
    val visitingSeasons: String,
    val natureReserve: String,
    val description: String,
    val mapType: String,
    val mapReference: String,
    val trailMarks: String,
    val uid: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrls: List<String>
)