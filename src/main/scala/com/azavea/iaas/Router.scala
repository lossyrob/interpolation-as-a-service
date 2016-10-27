package com.azavea.iaas

import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.unmarshalling.Unmarshaller._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import de.heikoseeberger.akkahttpcirce.CirceSupport
import io.circe.generic.auto._
import geotrellis.proj4._
import geotrellis.raster._
import geotrellis.raster.interpolation._
import geotrellis.raster.io.geotiff._
import geotrellis.vector._
import geotrellis.vector.interpolation._
import geotrellis.vector.io._
import geotrellis.vector.io.json._
import spray.json._

import java.lang.management.ManagementFactory
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

case class Data(v: Double)

case class Status(uptime: String)

trait Router extends Directives with CirceSupport with AkkaSystem.LoggerExecutor {
  def dataJsonReader(valueProperty: String): JsonReader[Data] =
    new JsonReader[Data] {
      def read(value: JsValue): Data =
        value.asJsObject.getFields(valueProperty) match {
          case Seq(JsNumber(value)) =>
            Data(value.toDouble)

          case _ =>
            throw new DeserializationException(s"Could not find propert $valueProperty.")
        }
    }

  def routes =
    pathPrefix("ping") {
      get {
        complete { "pong" }
      }
    } ~
    pathPrefix("system") {
      pathPrefix("status") {
        log.info("/status/uptime executed")
        complete(Status(Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toString()))
      }
    } ~
    pathPrefix("interpolate") {
      post {
        entity(as[JsValue]) { json =>
          parameters('extent?, 'cols.as[Int], 'rows.as[Int],  'valueProperty) { (extentStrOpt, cols, rows, valueProperty) =>
            implicit val jsonReader = dataJsonReader(valueProperty)

            val featureCollection = json.convertTo[JsonFeatureCollection]

            val points: Seq[PointFeature[Double]] =
              featureCollection
                .getAllPointFeatures[Data]().map(_.mapData(_.v))

            log.info(s"NUM POINTS: ${points.length}")

            val extent =
              extentStrOpt.map{ extentStr =>
                Extent.fromString(extentStr)
              }.getOrElse {
                MultiPoint(points.map(_.geom)).envelope
              }

            val rasterExtent = RasterExtent(extent, cols, rows)

            log.info(s"$rasterExtent")

            val interpolated: Raster[Tile] =
              points.inverseDistanceWeighted(rasterExtent, InverseDistanceWeighted.Options(weightingPower = 4))

            val geoTiff = GeoTiff(interpolated, extent, LatLng)

            // DEBUG
            geoTiff.write("/Users/rob/tmp/iaas-3.tif")

            complete { geoTiff.toBytes }
          }
        }
      }
    }
}
