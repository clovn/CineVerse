package com.cineverse.domain.repository

import com.cineverse.domain.model.ProfileStats
import com.cineverse.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun login(username: String, password: String): UserProfile
    suspend fun register(username: String, password: String): UserProfile
    suspend fun logout()
    fun getActiveSession(): Flow<UserProfile?>
    suspend fun getProfileStats(username: String): ProfileStats
    fun isOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)
}
