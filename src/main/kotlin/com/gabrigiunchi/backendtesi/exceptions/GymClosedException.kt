package com.gabrigiunchi.backendtesi.exceptions

import java.lang.RuntimeException

class GymClosedException : RuntimeException("The gym is closed in the given date")