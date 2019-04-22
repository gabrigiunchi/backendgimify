package com.gabrigiunchi.backendtesi.exceptions

import java.lang.RuntimeException

class ReservationConflictException : RuntimeException("There is another reservation in that interval")