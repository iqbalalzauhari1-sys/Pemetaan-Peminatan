package com.iqbal.pemetaanpeminatan // Sesuaikan dengan package Anda

open class Event<out T>(private val content: T) {

    @Suppress("MemberVisibilityCanBePrivate")
    var hasBeenHandled = false
        private set // Mengizinkan pembacaan dari luar, tapi penulisan hanya dari dalam

    /**
     * Mengembalikan nilai (content) dan mencegahnya digunakan lagi.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Mengembalikan nilai (content) meskipun sudah pernah digunakan.
     */
    fun peekContent(): T = content
}