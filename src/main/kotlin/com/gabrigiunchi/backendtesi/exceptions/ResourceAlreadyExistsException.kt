package com.gabrigiunchi.backendtesi.exceptions

class ResourceAlreadyExistsException : RuntimeException {
    constructor(id: String) : super("Resource with ID: '$id' already exists.")

    constructor(id: Int) : super("Resource with ID: '$id' already exists.")
}
