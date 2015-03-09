package domain

case class Snippet(imageUrl: String, title: String, description: String, url: String) {

  def toHtml = {
    s"""<meta property="og:image" content="$imageUrl" />
      |<meta property="og:title" content="$title" />
      |<meta property="og:description" content="$description" />
      |<meta property="og:url" content="$url" />
      |<meta property="og:type" content="article" />""".stripMargin
  }
}
