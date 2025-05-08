package com.matanboas.maslul.network

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.matanboas.maslul.models.Trail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TrailService(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val TAG = "TrailService"

    private fun parseAccess(rawAccess: String?): List<String> {
        return rawAccess
            ?.split(",", "&")
            ?.map { it.trim() }
            ?.filter { it.isNotEmpty() }
            ?: emptyList()
    }

    private suspend fun getAllDocumentsFromCollectionInternal(
        collectionName: String
    ): Result<List<DocumentSnapshot>> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Fetching documents from: $collectionName")
            val querySnapshot: QuerySnapshot = firestore.collection(collectionName).get().await()
            val documents: List<DocumentSnapshot> = querySnapshot.documents
            Log.d(TAG, "Successfully fetched ${documents.size} documents from $collectionName.")
            Result.success(documents)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching documents from $collectionName", e)
            Result.failure(e)
        }
    }

    private suspend fun getImageUrlsForTrail(trailName: String): List<String> {
        val sanitizedName = trailName.replace(" ", "_")
        val folderRef = storage.reference.child("images/$sanitizedName")
        return try {
            val result = folderRef.listAll().await()
            result.items.map { it.downloadUrl.await().toString() }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching image URLs for trail '$trailName'", e)
            emptyList()
        }
    }

    suspend fun getTrailsByCollection(collectionName: String): Result<List<Trail>> {
        Log.i(TAG, "Starting to fetch trails for collection '$collectionName'")
        val documentsResult = getAllDocumentsFromCollectionInternal(collectionName)

        return documentsResult.fold(
            onSuccess = { documents ->
                if (documents.isEmpty()) {
                    Log.i(TAG, "No documents found in collection '$collectionName'.")
                    return Result.success(emptyList())
                }
                Log.d(TAG, "Found ${documents.size} documents in '$collectionName'. Processing them...")

                val trailDeferreds = coroutineScope {
                    documents.map { document ->
                        async(Dispatchers.Default) {
                            try {
                                val data = document.data
                                if (data == null) {
                                    Log.w(TAG, "Document ${document.id} in $collectionName has no data, skipping.")
                                    return@async null
                                }

                                val trailName = data["Trail Name"] as? String
                                if (trailName == null) {
                                    Log.w(TAG, "Document ${document.id} in $collectionName is missing 'Trail Name', skipping.")
                                    return@async null
                                }

                                val uid = data["uid"] as? String ?: "unknown_${document.id}"
                                val imageUrls = getImageUrlsForTrail(trailName)
                                Log.d(TAG, "Trail ${trailName}, image URLs: $imageUrls")

                                Trail(
                                    name = trailName,
                                    region = data["אזור"] as? String ?: collectionName,
                                    location = data["מיקום"] as? String ?: "לא ידוע",
                                    access = parseAccess(data["גישה"] as? String),
                                    difficulty = data["רמת קושי"] as? String ?: "לא ידוע",
                                    lengthKm = when (val length = data["Length (km)"]) {
                                        is Number -> length.toFloat()
                                        is String -> length.toFloatOrNull() ?: 0.0f
                                        else -> 0.0f
                                    },
                                    trailType = data["סוג מסלול"] as? String ?: "לא ידוע",
                                    entranceFee = data["כניסה"] as? String ?: "לא ידוע",
                                    visitingSeasons = data["עונות ביקור"] as? String ?: "לא ידוע",
                                    natureReserve = data["שמורת טבע"] as? String ?: "לא ידוע",
                                    description = data["Description"] as? String ?: "אין תיאור זמין.",
                                    mapType = data["Map Type"] as? String ?: "לא ידוע",
                                    mapReference = data["מפת סימון שבילים"] as? String ?: "לא ידוע",
                                    trailMarks = data["שבילי טיול"] as? String ?: "לא ידוע",
                                    uid = uid,
                                    latitude = (data["latitude"] as? Number)?.toDouble() ?: 0.0,
                                    longitude = (data["longitude"] as? Number)?.toDouble() ?: 0.0,
                                    imageUrls = imageUrls
                                )
                            } catch (e: Exception) {
                                Log.e(TAG, "Error processing document ${document.id} in $collectionName", e)
                                null
                            }
                        }
                    }
                }

                val trails = trailDeferreds.awaitAll().filterNotNull()
                Log.i(TAG, "Successfully processed ${trails.size} trails from collection '$collectionName'.")
                Result.success(trails)
            },
            onFailure = { exception ->
                Log.e(TAG, "Failed to fetch documents for collection '$collectionName'", exception)
                Result.failure(exception)
            }
        )
    }
}