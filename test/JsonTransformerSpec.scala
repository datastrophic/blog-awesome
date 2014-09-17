import org.specs2.mutable.Specification
import play.api.libs.json.{JsValue, Json}
import util.JsonPostTransformer

class JsonTransformerSpec extends Specification {

  "Json Transformer" should {

    "correctly extract title from post" in {

      val sample =
        """
          |{"title":"test title","data":[],"tags":[]}
        """.stripMargin

      val json: JsValue = Json.parse(sample)
      val post = JsonPostTransformer.createPostFromJson(json)

      post.isDefined mustEqual true
      post.get.title mustEqual "test title"
    }

    "correctly create text block" in {

      val sample =
        """
          |{"title":"test title","data":[{"type":"text","data":{"text":"test text"}}],"tags":[]}
        """.stripMargin

      val json: JsValue = Json.parse(sample)
      val post = JsonPostTransformer.createPostFromJson(json)

      post.isDefined mustEqual true

      val dataBlocks = post.get.body
      dataBlocks.size mustEqual 1
      dataBlocks.head.`type` mustEqual "text" //TODO: switch to enum
      dataBlocks.head.data mustEqual "test text"
    }

    "correctly create image block" in {
      val sample =
        """
          |{"title":"test title","data":[{"type":"image","data":{"file":{"url":"http://localhost:9000/images/kbr9i9970sq44m04gscu8p7v50.jpg"}}}],"tags":[]}
        """.stripMargin

      val json: JsValue = Json.parse(sample)
      val post = JsonPostTransformer.createPostFromJson(json)

      post.isDefined mustEqual true

      val dataBlocks = post.get.body
      dataBlocks.size mustEqual 1
      dataBlocks.head.`type` mustEqual "image" //TODO: switch to enum
      dataBlocks.head.data mustEqual "http://localhost:9000/images/kbr9i9970sq44m04gscu8p7v50.jpg"
    }

    "correctly create gist block" in {
      val sample =
        """
          |{"title":"test title","data":[{"type":"gist","data":{"id":"00c4d337dbc5c9ea4cbc"}}],"tags":[]}
        """.stripMargin

      val json: JsValue = Json.parse(sample)
      val post = JsonPostTransformer.createPostFromJson(json)

      post.isDefined mustEqual true

      val dataBlocks = post.get.body
      dataBlocks.size mustEqual 1
      dataBlocks.head.`type` mustEqual "gist" //TODO: switch to enum
      dataBlocks.head.data mustEqual "00c4d337dbc5c9ea4cbc"
    }

    "correctly extract video block" in {
      val sample =
        """
          |{"title":"test","data":[{"type":"video","data":{"source":"youtube","remote_id":"ZnJ7uOK4nYg"}}],"tags":[]}
        """.stripMargin

      //TODO: finish video addition logic and tests

      1 mustEqual 1
    }

    "correctly extract tags" in {
      val sample =
        """
          |{"title":"test title","data":[],"tags":["tag1","tag2","tag3"]}
        """.stripMargin

      val json: JsValue = Json.parse(sample)
      val tags = JsonPostTransformer.extractTags(json)

      tags.isDefined mustEqual true

      tags.get.size mustEqual 3
      tags.get.contains("tag1") mustEqual true
      tags.get.contains("tag2") mustEqual true
      tags.get.contains("tag3") mustEqual true
    }
  }

}
