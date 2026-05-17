package com.hasiru.usiru.mapper

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.hasiru.usiru.mapper.ai.GeminiTreeIdentifier
import com.hasiru.usiru.mapper.data.local.AppDatabase
import com.hasiru.usiru.mapper.data.remote.FirestoreService
import com.hasiru.usiru.mapper.data.repository.TreeRepository

class HasiruApp : Application() {
    lateinit var database: AppDatabase
        private set
    lateinit var repository: TreeRepository
        private set
    lateinit var geminiTreeIdentifier: GeminiTreeIdentifier
        private set

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        val firestore = FirebaseFirestore.getInstance()
        firestore.firestoreSettings = firestoreSettings { isPersistenceEnabled = true }
        database = AppDatabase.getInstance(this)
        val firestoreService = FirestoreService(firestore)
        repository = TreeRepository(database.treeDao(), firestoreService)
        geminiTreeIdentifier = GeminiTreeIdentifier(BuildConfig.GEMINI_API_KEY)
    }
}
