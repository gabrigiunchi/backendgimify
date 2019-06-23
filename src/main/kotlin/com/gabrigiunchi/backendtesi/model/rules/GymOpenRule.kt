package com.gabrigiunchi.backendtesi.model.rules

import com.gabrigiunchi.backendtesi.dao.TimetableDAO
import com.gabrigiunchi.backendtesi.exceptions.GymClosedException
import com.gabrigiunchi.backendtesi.model.entities.Gym
import com.gabrigiunchi.backendtesi.model.time.Interval
import com.gabrigiunchi.backendtesi.model.time.ZonedInterval
import org.springframework.stereotype.Service
import java.time.OffsetDateTime

@Service
class GymOpenRule(private val timetableDAO: TimetableDAO) : Rule<Pair<Gym, Interval<OffsetDateTime>>> {
    override fun validate(element: Pair<Gym, Interval<OffsetDateTime>>) {
        if (!this.test(element)) {
            throw GymClosedException()
        }
    }

    override fun test(element: Pair<Gym, Interval<OffsetDateTime>>): Boolean {
        val interval = element.second
        val timetable = this.timetableDAO.findByGym(element.first)
        return timetable.isPresent && timetable.get()
                .isOpenAt(ZonedInterval(interval.start, interval.end)
                        .toLocalInterval(element.first.city.zoneId))
    }


}