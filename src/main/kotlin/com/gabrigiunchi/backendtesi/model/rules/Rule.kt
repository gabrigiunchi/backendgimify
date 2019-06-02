package com.gabrigiunchi.backendtesi.model.rules

interface Rule<T> {

    @Throws(Exception::class)
    fun validate(element: T)

    fun test(element: T): Boolean
}