package pj.properties

import org.scalacheck.Prop.{forAll, propBoolean}
import org.scalacheck.Properties
import pj.domain.SimpleTypes.Duration
import pj.domain.availability.AvailabilityService
import pj.properties.{DomainProperties, SimpleTypesProperties}


object AvailabilityServiceProperties extends Properties("AvailabilityServiceProperties"):

  property("removeInterval") =
    forAll(SimpleTypesProperties.durationGen): g =>
      forAll(DomainProperties.availabilityGen): a =>
        val intervalStart = a.start
        val intervalEnd = intervalStart.plus(g)
        val result = AvailabilityService.removeInterval(a, intervalStart, intervalEnd)

        if (intervalStart.isEqual(a.start) && intervalEnd.isEqual(a.end))
          result.isEmpty
        else if (intervalStart.isEqual(a.start) && intervalEnd.isBefore(a.end))
          result.sizeIs == 1 && result.headOption.exists(a => a.start.equals(intervalEnd) && a.end.equals(a.end))
        else if (intervalStart.isAfter(a.start) && intervalEnd.isEqual(a.end))
          result.sizeIs == 1 && result.headOption.exists(a => a.start.equals(a.start) && a.end.equals(intervalStart))
        else if ((intervalStart.isAfter(a.start) && intervalStart.isBefore(a.end)) || (intervalEnd.isAfter(a.start) && intervalEnd.isBefore(a.end)))
          result.sizeIs == 2 && result.exists(a => a.start.equals(a.start) && a.end.equals(intervalStart)) && result.exists(a => a.start.equals(intervalEnd) && a.end.equals(a.end))
        else
          result.sizeIs == 1 && result.headOption.exists(a => a.start.equals(a.start) && a.end.equals(a.end))
