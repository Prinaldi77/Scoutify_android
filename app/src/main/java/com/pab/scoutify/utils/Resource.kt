package com.pab.scoutify.utils

/**
 * A generic class that holds a value with its loading status.
 * Used to communicate network/repository state to the UI layer cleanly.
 */
sealed class Resource<out T> {
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error(val message: String, val exception: Throwable? = null) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}
