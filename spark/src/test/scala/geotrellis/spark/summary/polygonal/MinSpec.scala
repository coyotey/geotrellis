/*
 * Copyright 2016 Azavea
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package geotrellis.spark.summary.polygonal

import geotrellis.spark._
import geotrellis.spark.io.hadoop._
import geotrellis.spark.testfiles._
import geotrellis.raster.summary.polygonal._
import geotrellis.spark.testkit._

import geotrellis.vector._

import org.scalatest.FunSpec

class MinSpec extends FunSpec with TestEnvironment with TestFiles {

  describe("Min Zonal Summary Operation") {
    val inc = IncreasingTestFile

    val tileLayout = inc.metadata.tileLayout
    val count = (inc.count * tileLayout.tileCols * tileLayout.tileRows).toInt
    val totalExtent = inc.metadata.extent

    it("should get correct min over whole raster extent") {
      inc.polygonalMin(totalExtent.toPolygon) should be(0)
    }

    it("should get correct min over a quarter of the extent") {
      val xd = totalExtent.xmax - totalExtent.xmin
      val yd = totalExtent.ymax - totalExtent.ymin

      val quarterExtent = Extent(
        totalExtent.xmin,
        totalExtent.ymin,
        totalExtent.xmin + xd / 2,
        totalExtent.ymin + yd / 2
      )

      val result = inc.polygonalMin(quarterExtent.toPolygon)
      val expected = inc.stitch.tile.polygonalMin(totalExtent, quarterExtent.toPolygon)

      result should be (expected)
    }
  }

  describe("Min Zonal Summary Operation (collections api)") {
    val inc = IncreasingTestFile.toCollection

    val tileLayout = inc.metadata.tileLayout
    val count = inc.length * tileLayout.tileCols * tileLayout.tileRows
    val totalExtent = inc.metadata.extent

    it("should get correct min over whole raster extent") {
      inc.polygonalMin(totalExtent.toPolygon) should be(0)
    }

    it("should get correct min over a quarter of the extent") {
      val xd = totalExtent.xmax - totalExtent.xmin
      val yd = totalExtent.ymax - totalExtent.ymin

      val quarterExtent = Extent(
        totalExtent.xmin,
        totalExtent.ymin,
        totalExtent.xmin + xd / 2,
        totalExtent.ymin + yd / 2
      )

      val result = inc.polygonalMin(quarterExtent.toPolygon)
      val expected = inc.stitch.tile.polygonalMin(totalExtent, quarterExtent.toPolygon)

      result should be (expected)
    }
  }
}
