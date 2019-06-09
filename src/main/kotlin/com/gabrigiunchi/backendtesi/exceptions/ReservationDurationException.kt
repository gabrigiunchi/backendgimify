package com.gabrigiunchi.backendtesi.exceptions

class ReservationDurationException(max: Int) : RuntimeException("reservation duration exceeds maximum (max=$max minutes)")