package service

import domain.Post
import java.text.SimpleDateFormat
import java.util.{Calendar, GregorianCalendar, Locale}

/**
 * Created by akirillov on 8/14/14.
 */
object PostGenerator {
  def generate(amount: Int): List[Post] = {
    val preview =
      """
        |Lorem ipsum dolor sit amet, consectetur
        |adipisicing elit. Cupiditate, voluptates, voluptas dolore ipsam cumque
      """.stripMargin
    val body =
      """Lorem ipsum dolor sit amet, consectetur
        |adipisicing elit. Cupiditate, voluptates, voluptas dolore ipsam cumque
        |quam veniam accusantium laudantium adipisci architecto itaque dicta aperiam maiores
        |provident id incidunt autem. Magni, ratione.""".stripMargin

    val fmt = new SimpleDateFormat("MMMM dd, yyyy", Locale.US)
    val date = Calendar.getInstance().getTime

    (0 to amount) map { i =>

      Post(i.toString, s"Mock post #$i", preview, body, fmt.format(date), (0 to i) map (t => s"tag_$t") toList)

    } toList
  }
}
