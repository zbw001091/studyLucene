package com.zbw.big.studyLucene.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.facet.DrillDownQuery;
import org.apache.lucene.facet.DrillSideways;
import org.apache.lucene.facet.FacetField;
import org.apache.lucene.facet.FacetResult;
import org.apache.lucene.facet.Facets;
import org.apache.lucene.facet.FacetsCollector;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.LongValueFacetCounts;
import org.apache.lucene.facet.range.LongRange;
import org.apache.lucene.facet.range.LongRangeFacetCounts;
import org.apache.lucene.facet.sortedset.DefaultSortedSetDocValuesReaderState;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetCounts;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesReaderState;
import org.apache.lucene.facet.taxonomy.FastTaxonomyFacetCounts;
import org.apache.lucene.facet.taxonomy.TaxonomyFacetSumValueSource;
import org.apache.lucene.facet.taxonomy.TaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyReader;
import org.apache.lucene.facet.taxonomy.directory.DirectoryTaxonomyWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.DoubleValuesSource;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
//import org.apache.lucene.util.TestUtil;
//import org.apache.lucene.util.LuceneTestCase;

public class FacetDocumentIndexer2 {
	private final Directory indexDir = new RAMDirectory();
	private final Directory taxoDir = new RAMDirectory();
	private final FacetsConfig config = new FacetsConfig();

	/** 当前时间的毫秒数 */
	private final long nowSec = System.currentTimeMillis();
	/** 近1小时的毫秒数 */
	final LongRange PAST_HOUR = new LongRange("Past hour", this.nowSec - 3600L, true, this.nowSec, true);
	/** 近6小时的毫秒数 */
	final LongRange PAST_SIX_HOURS = new LongRange("Past six hours", this.nowSec - 21600L, true, this.nowSec, true);
	/** 近24小时的毫秒数 */
	final LongRange PAST_DAY = new LongRange("Past day", this.nowSec - 86400L, true, this.nowSec, true);

	public FacetDocumentIndexer2() {

	}

	/**
	 * index FacetField
	 * 
	 * @throws IOException
	 */
	private void index() throws IOException {
		IndexWriter indexWriter = new IndexWriter(this.indexDir,
				new IndexWriterConfig(new WhitespaceAnalyzer()).setOpenMode(IndexWriterConfig.OpenMode.CREATE));

//		this.config.setHierarchical("Author", true);
		this.config.setHierarchical("Publish Date", true); // 3 level, year/month/date, new String[] { "2010", "10", "15" }

		DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(this.taxoDir);

		Document doc = new Document();
		doc.add(new FacetField("Author", new String[] { "Bob" }));
		doc.add(new FacetField("Publish Date", new String[] { "2010", "10", "15" }));
		indexWriter.addDocument(this.config.build(taxoWriter, doc));

		doc = new Document();
		doc.add(new FacetField("Author", new String[] { "Lisa" }));
		doc.add(new FacetField("Publish Date", new String[] { "2010", "10", "20" }));
		indexWriter.addDocument(this.config.build(taxoWriter, doc));

		doc = new Document();
		doc.add(new FacetField("Author", new String[] { "Lisa" }));
		doc.add(new FacetField("Publish Date", new String[] { "2012", "1", "1" }));
		indexWriter.addDocument(this.config.build(taxoWriter, doc));

		doc = new Document();
		doc.add(new FacetField("Author", new String[] { "Susan" }));
		doc.add(new FacetField("Publish Date", new String[] { "2012", "1", "7" }));
		indexWriter.addDocument(this.config.build(taxoWriter, doc));

		doc = new Document();
		doc.add(new FacetField("Author", new String[] { "Frank" }));
		doc.add(new FacetField("Publish Date", new String[] { "1999", "5", "5" }));
		indexWriter.addDocument(this.config.build(taxoWriter, doc));

		indexWriter.close();
		taxoWriter.close();
	}

	/**
	 * index normal Field, not FacetField
	 * 
	 * @throws IOException
	 */
	private void indexRange() throws IOException {
		IndexWriter indexWriter = new IndexWriter(this.indexDir,
				new IndexWriterConfig(new WhitespaceAnalyzer()).setOpenMode(IndexWriterConfig.OpenMode.CREATE));

		/**
		 * 每次按[1000*i]这个斜率递减创建一个索引
		 */
		for (int i = 0; i < 100; i++) {
			Document doc = new Document();
			long then = this.nowSec - i * 1000;

			doc.add(new NumericDocValuesField("timestamp", then));
//			doc.add(new LongField("timestamp", then, Field.Store.YES));
			indexWriter.addDocument(doc);
		}
		indexWriter.close();
	}

	/**
	 * index longField, not FacetField
	 * 
	 * @throws IOException
	 */
	private void indexLong() throws IOException {
		IndexWriter indexWriter = new IndexWriter(this.indexDir,
				new IndexWriterConfig(new WhitespaceAnalyzer()).setOpenMode(IndexWriterConfig.OpenMode.CREATE));

		for (long l = 0; l < 100; l++) {
			Document doc = new Document();
			doc.add(new NumericDocValuesField("longField", l % 5));
			indexWriter.addDocument(doc);
		}

		// Also add Long.MAX_VALUE
		Document doc = new Document();
		doc.add(new NumericDocValuesField("longField", Long.MAX_VALUE));
		indexWriter.addDocument(doc);

		indexWriter.close();
	}

	/**
	 * index SortedSetDocValuesFacetField
	 * 
	 * @throws IOException
	 */
	private void indexSortedSetDocValuesFacetField() throws IOException {
		IndexWriter indexWriter = new IndexWriter(this.indexDir,
				new IndexWriterConfig(new WhitespaceAnalyzer()).setOpenMode(IndexWriterConfig.OpenMode.CREATE));

		this.config.setMultiValued("a", true);

		Document doc = new Document();
		doc.add(new SortedSetDocValuesFacetField("a", "foo"));
		doc.add(new SortedSetDocValuesFacetField("a", "bar"));
		doc.add(new SortedSetDocValuesFacetField("a", "zoo"));
		doc.add(new SortedSetDocValuesFacetField("b", "baz"));
		indexWriter.addDocument(config.build(doc));

		doc = new Document();
		doc.add(new SortedSetDocValuesFacetField("a", "foo"));
		indexWriter.addDocument(config.build(doc));

		indexWriter.close();
	}
	
	/**
	 * index SortedSetDocValuesFacetField
	 * 
	 * @throws IOException
	 */
	private void indexTaxonomyFacetSumValueSource() throws IOException {
		IndexWriter indexWriter = new IndexWriter(this.indexDir,
				new IndexWriterConfig(new WhitespaceAnalyzer()).setOpenMode(IndexWriterConfig.OpenMode.CREATE));

		DirectoryTaxonomyWriter taxoWriter = new DirectoryTaxonomyWriter(this.taxoDir);
		
		Document doc = new Document();
	    doc.add(new NumericDocValuesField("num", 10));
	    doc.add(new FacetField("Author", "Bob"));
	    indexWriter.addDocument(config.build(taxoWriter, doc));

	    doc = new Document();
	    doc.add(new NumericDocValuesField("num", 20));
	    doc.add(new FacetField("Author", "Lisa"));
	    indexWriter.addDocument(config.build(taxoWriter, doc));

	    doc = new Document();
	    doc.add(new NumericDocValuesField("num", 30));
	    doc.add(new FacetField("Author", "Lisa"));
	    indexWriter.addDocument(config.build(taxoWriter, doc));

	    doc = new Document();
	    doc.add(new NumericDocValuesField("num", 40));
	    doc.add(new FacetField("Author", "Susan"));
	    indexWriter.addDocument(config.build(taxoWriter, doc));

	    doc = new Document();
	    doc.add(new NumericDocValuesField("num", 45));
	    doc.add(new FacetField("Author", "Frank"));
	    indexWriter.addDocument(config.build(taxoWriter, doc));
	    
	    indexWriter.close();
		taxoWriter.close();
	}

	private List<FacetResult> facetsWithSearch() throws IOException {
		DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TaxonomyReader taxoReader = new DirectoryTaxonomyReader(this.taxoDir);

		// search result, set into facetsCollector
		FacetsCollector fc = new FacetsCollector();
		FacetsCollector.search(searcher, new MatchAllDocsQuery(), 10, fc);

		List<FacetResult> results = new ArrayList<FacetResult>();

		// read from facetsCollector into facets
		FastTaxonomyFacetCounts facets = new FastTaxonomyFacetCounts(taxoReader, this.config, fc);
		results.add(facets.getTopChildren(10, "Author", new String[0]));

		// first level group by
//		results.add(facets.getTopChildren(10, "Publish Date", new String[0]));

		// second level group by - drill down
//		results.add(facets.getTopChildren(10, "Publish Date", new String[] { "2012" }));

		// third level group by - drill down once more
		results.add(facets.getTopChildren(10, "Publish Date", new String[] { "2012", "1" }));

		FacetResult result = facets.getTopChildren(10, "Author", new String[0]);
		
		indexReader.close();
		taxoReader.close();

		return results;
	}

	private List<FacetResult> facetsOnly() throws IOException {
//		DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
//		IndexSearcher searcher = new IndexSearcher(indexReader);
//		TaxonomyReader taxoReader = new DirectoryTaxonomyReader(this.taxoDir);
//
//		FacetsCollector fc = new FacetsCollector();
//		searcher.search(new MatchAllDocsQuery(), null, fc);
//
//		List<FacetResult> results = new ArrayList<FacetResult>();
//
//		Facets facets = new FastTaxonomyFacetCounts(taxoReader, this.config, fc);
//
//		results.add(facets.getTopChildren(10, "Author"));
//		results.add(facets.getTopChildren(10, "Publish Date"));
//
//		indexReader.close();
//		taxoReader.close();
//
//		return results;
		return null;
	}

	/**
	 * Based on facetsWithSearch(), mock end user drill down in the "Publish Date"
	 * on the webpage
	 * 
	 * @return
	 * @throws IOException
	 */
	private FacetResult drillDown() throws IOException {
		DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TaxonomyReader taxoReader = new DirectoryTaxonomyReader(this.taxoDir);

		DrillDownQuery q = new DrillDownQuery(this.config);
		q.add("Publish Date", new String[] { "2010" });

		// search result, set into facetsCollector
		FacetsCollector fc = new FacetsCollector();
		FacetsCollector.search(searcher, q, 10, fc);

		// read from facetsCollector into facets
		Facets facets = new FastTaxonomyFacetCounts(taxoReader, this.config, fc);
		FacetResult result = facets.getTopChildren(10, "Author", new String[0]);

		indexReader.close();
		taxoReader.close();

		return result;
	}

	private List<FacetResult> drillSideways() throws IOException {
		DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TaxonomyReader taxoReader = new DirectoryTaxonomyReader(this.taxoDir);

		DrillDownQuery q = new DrillDownQuery(this.config);
		q.add("Publish Date", new String[] { "2010" });

		DrillSideways ds = new DrillSideways(searcher, this.config, taxoReader);
		DrillSideways.DrillSidewaysResult result = ds.search(q, 10);

		List<FacetResult> facets = result.facets.getAllDims(10);

		indexReader.close();
		taxoReader.close();

		return facets;
	}

	private FacetResult facetsWithRange() throws IOException {
		DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
		IndexSearcher searcher = new IndexSearcher(indexReader);

		// search result, set into facetsCollector
		FacetsCollector fc = new FacetsCollector();
		TopDocs topDocs = FacetsCollector.search(searcher, new MatchAllDocsQuery(), 20, fc);

//		ScoreDoc[] scoreDocs = topDocs.scoreDocs;
//		for (ScoreDoc scoreDoc : scoreDocs) {
//			int docId = scoreDoc.doc;
//			Document doc = searcher.doc(docId);
//			System.out.println(scoreDoc.doc + "\t" + doc.get("timestamp"));
//		}

		// read from facetsCollector into facets, group by longRange
		Facets facets = new LongRangeFacetCounts("timestamp", fc,
				new LongRange[] { this.PAST_HOUR, this.PAST_SIX_HOURS, this.PAST_DAY });

		return facets.getTopChildren(10, "timestamp", new String[0]);
	}

	private FacetResult facetsWithLong() throws IOException {
		DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
		IndexSearcher searcher = new IndexSearcher(indexReader);

		// search result, set into facetsCollector
		FacetsCollector fc = new FacetsCollector();
		searcher.search(new MatchAllDocsQuery(), fc);

		// read from facetsCollector into facets, group by LongValueFacetCounts
		LongValueFacetCounts facets = new LongValueFacetCounts("longField", fc, false);

		return facets.getAllChildrenSortByValue();
	}

	private FacetResult facetsWithSortedSetDocValues() throws IOException {
		DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
		IndexSearcher searcher = new IndexSearcher(indexReader);

		// Per-top-reader state:
		SortedSetDocValuesReaderState state = new DefaultSortedSetDocValuesReaderState(searcher.getIndexReader());

		// search result, set into facetsCollector
		FacetsCollector fc = new FacetsCollector();
		searcher.search(new MatchAllDocsQuery(), fc);

		// read from facetsCollector into facets, group by SortedSetDocValuesFacetCounts
		Facets facets = new SortedSetDocValuesFacetCounts(state, fc);

		return facets.getTopChildren(10, "a");
	}

	
	private FacetResult facetsWithTaxonomyFacetSumValueSource() throws IOException {
		DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		TaxonomyReader taxoReader = new DirectoryTaxonomyReader(this.taxoDir);

		// search result, set into facetsCollector
		FacetsCollector fc = new FacetsCollector();
		FacetsCollector.search(searcher, new MatchAllDocsQuery(), 10, fc);
		
		TaxonomyFacetSumValueSource facets = new TaxonomyFacetSumValueSource(taxoReader, this.config, fc, DoubleValuesSource.fromIntField("num"));
		
		return facets.getTopChildren(10, "Author");
	}
	
	public List<FacetResult> runFacetOnly() throws IOException {
		index();
		return facetsOnly();
	}

	public List<FacetResult> runFacetsWithSearch() throws IOException {
		index();
		return facetsWithSearch();
	}

	public FacetResult runDrillDown() throws IOException {
		index();
		return drillDown();
	}

	public List<FacetResult> runDrillSideways() throws IOException {
		index();
		return drillSideways();
	}

	public FacetResult runFacetsWithRange() throws IOException {
		indexRange();
		return facetsWithRange();
	}

	public FacetResult runFacetsWithLong() throws IOException {
		indexLong();
		return facetsWithLong();
	}

	public void runPathToString() {
		for (int i = 0; i < 1; i++) {
			String[] parts = new String[5];
			for (int j = 0; j < 5; j++) {
				parts[j] = Integer.toString(j);
			}
			String s = FacetsConfig.pathToString(parts);
			System.out.println(parts);
			System.out.println(s);
		}
	}

	public FacetResult runSortedSetDocValuesFacetField() throws IOException {
		indexSortedSetDocValuesFacetField();
		return facetsWithSortedSetDocValues();
	}
	
	public FacetResult runfacetsWithTaxonomyFacetSumValueSource() throws IOException {
		indexTaxonomyFacetSumValueSource();
		return facetsWithTaxonomyFacetSumValueSource();
	}
	
}
