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

package geotrellis.raster.io.geotiff

import geotrellis.raster._
import geotrellis.raster.io.geotiff.compression._

import java.nio.ByteBuffer

class BitGeoTiffMultibandTile(
  compressedBytes: SegmentBytes,
  decompressor: Decompressor,
  segmentLayout: GeoTiffSegmentLayout,
  compression: Compression,
  bandCount: Int,
  hasPixelInterleave: Boolean,
  val cellType: BitCells with NoDataHandling
) extends GeoTiffMultibandTile(compressedBytes, decompressor, segmentLayout, compression, bandCount, hasPixelInterleave)
    with BitGeoTiffSegmentCollection {

  protected def createSegmentCombiner(targetSize: Int): SegmentCombiner =
    new SegmentCombiner(bandCount) {
      private val arr = Array.ofDim[Byte](targetSize + 7 / 8)

      def set(targetIndex: Int, v: Int): Unit = {
        BitArrayTile.update(arr, targetIndex, v)
      }

      def setDouble(targetIndex: Int, v: Double): Unit = {
        BitArrayTile.updateDouble(arr, targetIndex, v)
      }

      def getBytes(): Array[Byte] =
        arr
    }

  def withNoData(noDataValue: Option[Double]) =
    new BitGeoTiffMultibandTile(compressedBytes, decompressor, segmentLayout, compression, bandCount, hasPixelInterleave, cellType.withNoData(noDataValue))

  def interpretAs(newCellType: CellType)  = {
    newCellType match {
      case dt: BitCells with NoDataHandling =>
        new BitGeoTiffMultibandTile(compressedBytes, decompressor, segmentLayout, compression, bandCount, hasPixelInterleave, dt)
      case _ =>
        withNoData(None).convert(newCellType)
    }
  }
}

