package com.hasiru.usiru.mapper.data.repository

import com.hasiru.usiru.mapper.data.local.TreeDao
import com.hasiru.usiru.mapper.data.local.TreeEntity
import com.hasiru.usiru.mapper.data.remote.FirestoreService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TreeRepository(
    private val treeDao: TreeDao,
    private val firestoreService: FirestoreService
) {
    suspend fun insertAndSync(tree: TreeEntity) = withContext(Dispatchers.IO) {
        val localId = treeDao.insert(tree).toInt()
        runCatching {
            firestoreService.uploadTree(tree.copy(id = localId, syncedToFirebase = true))
            treeDao.markSynced(localId)
        }
        Unit
    }

    fun observeAllTrees(): Flow<List<TreeEntity>> = treeDao.getAllTrees()

    suspend fun fetchCommunityTrees() = withContext(Dispatchers.IO) {
        runCatching { firestoreService.fetchTrees() }.getOrDefault(emptyList()).forEach { remoteTree ->
            val existing = treeDao.findByLocation(remoteTree.latitude, remoteTree.longitude)
            treeDao.insert(remoteTree.copy(id = existing?.id ?: 0, syncedToFirebase = true))
        }
    }

    fun getTotalOxygenScore(): Flow<Float> = treeDao.getTotalOxygenScore()

    suspend fun syncPendingTrees() = withContext(Dispatchers.IO) {
        treeDao.getUnsynced().forEach { tree ->
            runCatching {
                firestoreService.uploadTree(tree.copy(syncedToFirebase = true))
                treeDao.markSynced(tree.id)
            }
        }
    }
}
