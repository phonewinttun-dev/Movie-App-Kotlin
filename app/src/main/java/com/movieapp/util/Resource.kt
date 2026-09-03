package com.movieapp.util

/**
 * Sealed wrapper representing asynchronous data loading states for network operations.
 *
 * @param T The type of data contained within a successful resource.
 * @property data The optional payload data.
 * @property message The optional error message.
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    /**
     * Represents the in-flight loading state.
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)

    /**
     * Represents a successful network response containing non-null data.
     */
    class Success<T>(data: T) : Resource<T>(data)

    /**
     * Represents a failed network response containing an error description.
     */
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
}
