package com.gabrigiunchi.backendtesi.exceptions

class ResourceNotFoundException(message: String) : RuntimeException(message) {
    constructor(id: Int): this("Could not find resource with id: '$id'")
}
