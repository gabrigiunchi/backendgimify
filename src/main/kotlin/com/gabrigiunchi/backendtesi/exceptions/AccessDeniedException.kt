package com.gabrigiunchi.backendtesi.exceptions

class AccessDeniedException : RuntimeException {

    constructor(message: String) : super(message)

    constructor() : super("You do not have the permission to execute this operation")

}