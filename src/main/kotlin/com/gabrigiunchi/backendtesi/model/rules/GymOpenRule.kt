package com.gabrigiunchi.backendtesi.model.rules

import com.gabrigiunchi.backendtesi.dao.TimetableDAO
import com.gabrigiunchi.backendtesi.exceptions.GymClosedException
import com.gabrigiunchi.backendtesi.model.entities.Gym
import com.gabrigiunchi.backendtesi.model.time.ZonedInterval
import org.springframework.stereotype.Service

@Service
class GymOpenRule(private val timetableDAO: TimetableDAO) : Rule<Pair<Gym, ZonedInterval>> {
    override fun validate(element: Pair<Gym, ZonedInterval>) {
        if (!this.test(element)) {
            throw GymClosedException()
        }
    }

    override fun test(element: Pair<Gym, ZonedInterval>): Boolean {
        val timetable = this.timetableDAO.findByGym(element.first)
        return timetable.isPresent && timetable.get()
                .contains(ZonedInterval(element.second.start, element.second.end)
                        .toInterval(element.first.city.zoneId))
    }


}