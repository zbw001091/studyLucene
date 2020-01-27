package com.zbw.big.studyLucene.indexer;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHits.Relation;
import org.apache.lucene.spatial.SpatialStrategy;
import org.apache.lucene.spatial.prefix.RecursivePrefixTreeStrategy;
import org.apache.lucene.spatial.prefix.tree.GeohashPrefixTree;
import org.apache.lucene.spatial.prefix.tree.SpatialPrefixTree;
import org.apache.lucene.spatial.query.SpatialArgs;
import org.apache.lucene.spatial.query.SpatialArgsParser;
import org.apache.lucene.spatial.query.SpatialOperation;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.distance.DistanceUtils;
import org.locationtech.spatial4j.shape.Point;
import org.locationtech.spatial4j.shape.Shape;

public class SpatialDocumentIndexer {

	private SpatialContext ctx;
	private SpatialStrategy strategy;
	private Directory directory;

	public void init() {
		// Typical geospatial context
		// These can also be constructed from SpatialContextFactory
		this.ctx = SpatialContext.GEO;
		int maxLevels = 11; // results in sub-meter precision for geohash
		// This can also be constructed from SpatialPrefixTreeFactory
		SpatialPrefixTree grid = new GeohashPrefixTree(ctx, maxLevels);
		this.strategy = new RecursivePrefixTreeStrategy(grid, "myGeoField");

		this.directory = new RAMDirectory();
	}

	public void indexPoints() throws Exception {
		IndexWriterConfig iwConfig = new IndexWriterConfig(null);
		IndexWriter indexWriter = new IndexWriter(directory, iwConfig);

		// Spatial4j is x-y order for arguments
		indexWriter.addDocument(newSampleDocument(2, ctx.makePoint(-80.93, 33.77)));

		// Spatial4j has a WKT parser which is also "x y" order
		indexWriter.addDocument(newSampleDocument(4, ctx.readShapeFromWkt("POINT(60.9289094 -50.7693246)")));

		indexWriter.addDocument(newSampleDocument(20, ctx.makePoint(0.1, 0.1), ctx.makePoint(0, 0)));

		indexWriter.close();
	}

	private Document newSampleDocument(int id, Shape... shapes) {
		Document doc = new Document();
		doc.add(new StoredField("id", id));
		doc.add(new NumericDocValuesField("id", id));
		// Potentially more than one shape in this field is supported by some strategies
		for (Shape shape : shapes) {
			// indexed, but not stored fields
			for (Field f : strategy.createIndexableFields(shape)) {
				doc.add(f);
			}
			// store it too; the format is up to you
			// (assume point in this example)
			Point pt = (Point) shape;
			doc.add(new StoredField(strategy.getFieldName(), pt.getX() + " " + pt.getY()));
		}

		return doc;
	}

	private void search() throws Exception {
		IndexReader indexReader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		Sort idSort = new Sort(new SortField("id", SortField.Type.INT));

		// --Filter by circle (<= distance from a point)
		{
			// Search with circle
			// note: SpatialArgs can be parsed from a string
			SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects,
					ctx.makeCircle(-80.0, 33.0, DistanceUtils.dist2Degrees(200, DistanceUtils.EARTH_MEAN_RADIUS_KM)));
			Query query = strategy.makeQuery(args);
			TopDocs docs = indexSearcher.search(query, 10, idSort);
			assertDocMatchedIds(indexSearcher, docs, 2);
			// Now, lets get the distance for the 1st doc via computing from stored point
			// value:
			// (this computation is usually not redundant)
			Document doc1 = indexSearcher.doc(docs.scoreDocs[0].doc);
			String doc1Str = doc1.getField(strategy.getFieldName()).stringValue();
			// assume doc1Str is "x y" as written in newSampleDocument()
			int spaceIdx = doc1Str.indexOf(' ');
			double x = Double.parseDouble(doc1Str.substring(0, spaceIdx));
			double y = Double.parseDouble(doc1Str.substring(spaceIdx + 1));
			double doc1DistDEG = ctx.calcDistance(args.getShape().getCenter(), x, y);
			System.out.println(121.6d + " / "
					+ DistanceUtils.degrees2Dist(doc1DistDEG, DistanceUtils.EARTH_MEAN_RADIUS_KM) + " / " + 0.1);
			// or more simply:
			System.out.println(121.6d + " / " + doc1DistDEG * DistanceUtils.DEG_TO_KM + " / " + 0.1);
		}
		// --Match all, order by distance ascending
		{
			Point pt = ctx.makePoint(60, -50);
			DoubleValuesSource valueSource = strategy.makeDistanceValueSource(pt, DistanceUtils.DEG_TO_KM); // the distance (in km)
			Sort distSort = new Sort(valueSource.getSortField(false)).rewrite(indexSearcher); // false=asc dist
			TopDocs docs = indexSearcher.search(new MatchAllDocsQuery(), 10, distSort);
			assertDocMatchedIds(indexSearcher, docs, 4, 20, 2);
			// To get the distance, we could compute from stored values like earlier.
			// However in this example we sorted on it, and the distance will get
			// computed redundantly. If the distance is only needed for the top-X
			// search results then that's not a big deal. Alternatively, try wrapping
			// the ValueSource with CachingDoubleValueSource then retrieve the value
			// from the ValueSource now. See LUCENE-4541 for an example.
		}
		// demo arg parsing
		{
			SpatialArgs args = new SpatialArgs(SpatialOperation.Intersects, ctx.makeCircle(-80.0, 33.0, 1));
			SpatialArgs args2 = new SpatialArgsParser().parse("Intersects(BUFFER(POINT(-80 33),1))", ctx);
			System.out.println(args.toString() + " / " + args2.toString());
		}

		indexReader.close();
	}

	private void assertDocMatchedIds(IndexSearcher indexSearcher, TopDocs docs, int... ids) throws IOException {
		assert docs.totalHits.relation == Relation.EQUAL_TO;
		int[] gotIds = new int[Math.toIntExact(docs.totalHits.value)];
		for (int i = 0; i < gotIds.length; i++) {
			gotIds[i] = indexSearcher.doc(docs.scoreDocs[i].doc).getField("id").numericValue().intValue();
		}
	}
}
