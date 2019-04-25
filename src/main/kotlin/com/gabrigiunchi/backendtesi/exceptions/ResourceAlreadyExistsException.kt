package com.gabrigiunchi.backendtesi.exceptions

class ResourceAlreadyExistsException(message: String) : RuntimeException(message) {
    constructor(id: Int) : this("Resource with ID: '$id' already exists.")
}
