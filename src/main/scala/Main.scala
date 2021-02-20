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

  val out = new PrintWriter(new File("movies.json"))
  val rawData = getClass.getResource("/movies_metadata.csv")
  val reader = rawData.asCsvReader[Movie](rfc.withHeader.withCellSeparator(',').withQuote('"'))
    .foreach {
      case Left(x) => ()
      case Right(x) =>
        out.write(Json.obj("index" -> Json.obj("_id" -> Json.fromLong(x.id))).printWith(Printer.noSpaces))
        out.write("\n")
        out.write(x.asJson.printWith(Printer.noSpaces))
        out.write("\n")
    }
  out.close()
  println("done")

}
