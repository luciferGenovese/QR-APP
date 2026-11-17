package com.example.data

import kotlinx.coroutines.flow.Flow

class CodeRepository(private val codeDao: CodeDao) {
    val allCodes: Flow<List<CodeEntity>> = codeDao.getAllCodes()

    suspend fun insert(code: CodeEntity): Long = codeDao.insertCode(code)
    
    suspend fun update(code: CodeEntity) = codeDao.updateCode(code)

    suspend fun deleteById(id: Int) = codeDao.deleteCodeById(id)

    suspend fun deleteAll() = codeDao.deleteAllCodes()
}
