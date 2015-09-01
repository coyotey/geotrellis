package geotrellis.spark.ingest

import geotrellis.spark._
import geotrellis.spark.tiling._
import geotrellis.raster._
import geotrellis.raster.mosaic._
import geotrellis.vector._
import org.apache.spark.rdd._
import org.apache.spark.SparkContext._
import scala.reflect.ClassTag

object Tiler {
  def cutTiles[T, K: SpatialComponent: ClassTag] (
    getExtent: T=> Extent,
    createKey: (T, SpatialKey) => K,
    rdd: RDD[(T, Tile)],
    mapTransform: MapKeyTransform,
    cellType: CellType,
    tileLayout: TileLayout
  ): RDD[(K, Tile)] =
    rdd
      .flatMap { tup =>
        val (inKey, tile) = tup
        val extent = getExtent(inKey)
        mapTransform(extent)
          .coords
          .map  { spatialComponent =>
            val outKey = createKey(inKey, spatialComponent)
            val tmsTile: Tile =  ArrayTile.empty(cellType, tileLayout.tileCols, tileLayout.tileRows)
            tmsTile.merge(mapTransform(outKey), extent, tile)
            tmsTile.merge(tile)
            (outKey, tmsTile)
          }
       }

  def apply[T, K: SpatialComponent: ClassTag](
    getExtent: T=> Extent,
    createKey: (T, SpatialKey) => K,
    rdd: RDD[(T, Tile)],
    mapTransform: MapKeyTransform,
    cellType: CellType,
    tileLayout: TileLayout
  ): RDD[(K, Tile)] =
    cutTiles(getExtent, createKey, rdd, mapTransform, cellType, tileLayout)
      .reduceByKey { case (tile1: Tile, tile2: Tile) =>
        tile1.merge(tile2)
      }

  def apply[T, K: SpatialComponent: ClassTag]
    (getExtent: T=> Extent, createKey: (T, SpatialKey) => K)
    (rdd: RDD[(T, Tile)], metaData: RasterMetaData): RDD[(K, Tile)] = {

    apply(getExtent, createKey, rdd, metaData.mapTransform, metaData.cellType, metaData.tileLayout)
  }
}
