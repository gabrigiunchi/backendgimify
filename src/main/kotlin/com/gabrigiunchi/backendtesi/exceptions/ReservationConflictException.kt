package com.gabrigiunchi.backendtesi.exceptions

class ReservationConflictException : RuntimeException("There is another reservation in that interval")