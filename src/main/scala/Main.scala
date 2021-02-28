import io.circe._
import io.circe.generic._
import kantan.csv._
import kantan.csv.ops._
import kantan.csv.generic._
import io.circe.syntax._

import java.io._

@JsonCodec
case class Movie(adult: Boolean, belongs_to_collection: Json, budget: Long, genres: Json, homepage: String, id: Long, imdb_id: String, original_language: String,
                 original_title: String, overview: String, popularity: Double, poster_path: String, production_companies: Json, production_countries: Json,
                 release_date: String, revenue: Long, runtime: Double, spoken_languages: Json, status: String, tagline: String, title: String, video: Boolean,
                 vote_average: Double, vote_count: Long)

object Main extends App {

  def generate(s: String): List[String] = {
    s.split(" ").map(_.filter(_.isLetterOrDigit)).foldRight(("", List.empty[String])) { case (x, (prev, res)) =>
      val together = s"$x $prev".trim
      (together, together :: res)
    }._2
  }

  implicit val jsonCellDecoder: CellDecoder[Json] = CellDecoder[String]
    .emap { x =>
      if (x.isEmpty) Right(Json.Null)
      else {
        // quotes inside are wrong
        val mod = x.replaceAll("'", "\"")
        parser.parse(mod)
          .orElse(parser.parse(mod.stripPrefix("\"").stripSuffix("\""))) // sometimes there is suffix and prefix "
          .left.map(e => DecodeError.TypeError(e.message))
      }
    }

  val outRaw = new PrintWriter(new File("movies.json"))
  val outCompletion = new PrintWriter(new File("movies_completion.json"))
  val outContext = new PrintWriter(new File("movies_context.json"))
  val rawData = getClass.getResource("/movies_metadata.csv")
  val reader = rawData.asCsvReader[Movie](rfc.withHeader.withCellSeparator(',').withQuote('"'))
    .foreach {
      case Left(x) => ()
      case Right(x) =>
        outRaw.write(Json.obj("index" -> Json.obj("_id" -> Json.fromLong(x.id))).printWith(Printer.noSpaces))
        outRaw.write("\n")
        outRaw.write(x.asJson.printWith(Printer.noSpaces))
        outRaw.write("\n")

        outCompletion.write(Json.obj("index" -> Json.obj("_id" -> Json.fromLong(x.id))).printWith(Printer.noSpaces))
        outCompletion.write("\n")
        val completions = generate(x.title).map(Json.fromString)
        val completionjson = x.asJson.mapObject(x => x.add("title_completion", Json.arr(completions: _*)))
        outCompletion.write(completionjson.printWith(Printer.noSpaces))
        outCompletion.write("\n")

        outContext.write(Json.obj("index" -> Json.obj("_id" -> Json.fromLong(x.id))).printWith(Printer.noSpaces))
        outContext.write("\n")
        val contexts = Json.obj(
          "movie_category" -> x.genres.mapArray(_.flatMap(_ \\ "name"))
        )
        val contextJson = x.asJson.mapObject(x => x.add("title_completion",
          Json.obj("input" -> Json.arr(completions: _*), "contexts" -> contexts)))
        outContext.write(contextJson.printWith(Printer.noSpaces))
        outContext.write("\n")
    }

  outRaw.close()
  outCompletion.close()
  println("done")
}
