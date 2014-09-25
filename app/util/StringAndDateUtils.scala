package util

import java.text.DateFormatSymbols
import java.util.{Calendar, GregorianCalendar}

object StringAndDateUtils {

  def generateUID(title: String): String = {
    title.replaceAll("[^a-zA-Z0-9 ]", "").toLowerCase.split("\\s+").mkString("-")
  }

  def getCurrentDateAsString: Option[String] = {
    val calendar = new GregorianCalendar()

    getMonthByNum(calendar.get(Calendar.MONTH)) map { monthName =>

      s"$monthName ${calendar.get(Calendar.DAY_OF_MONTH)}, ${calendar.get(Calendar.YEAR)}"

    }
  }

  private def getMonthByNum(num: Int): Option[String] = {
    val months = new DateFormatSymbols().getMonths

    if (num >= 0 && num <= 11 ) {
      Some(months(num))
    } else None

  }

}
