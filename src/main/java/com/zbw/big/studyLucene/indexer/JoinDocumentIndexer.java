package com.zbw.big.studyLucene.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.join.BitSetProducer;
import org.apache.lucene.search.join.CheckJoinIndex;
import org.apache.lucene.search.join.JoinUtil;
import org.apache.lucene.search.join.QueryBitSetProducer;
import org.apache.lucene.search.join.ScoreMode;
import org.apache.lucene.search.join.ToParentBlockJoinQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

public class JoinDocumentIndexer {
	private final Directory indexDir = new RAMDirectory();
	private final String idField = "id";
	private final String toField = "productId";

	/**
	 * index Parent-Child documents
	 * 
	 * @throws IOException
	 */
	private void indexQueryTimeJoin() throws IOException {
		IndexWriter indexWriter = new IndexWriter(this.indexDir,
				new IndexWriterConfig(new WhitespaceAnalyzer()).setOpenMode(IndexWriterConfig.OpenMode.CREATE));

		// 0 product
		Document doc = new Document();
		doc.add(new TextField("description", "product1", Field.Store.NO));
		doc.add(new TextField("name", "name1", Field.Store.NO));
		doc.add(new TextField(idField, "1", Field.Store.NO));
		doc.add(new SortedDocValuesField(idField, new BytesRef("1")));
		indexWriter.addDocument(doc);

		// 1 offer
		doc = new Document();
		doc.add(new TextField("price", "10.0", Field.Store.NO));
		doc.add(new TextField(idField, "2", Field.Store.NO));
		doc.add(new SortedDocValuesField(idField, new BytesRef("2")));
		doc.add(new TextField(toField, "1", Field.Store.NO));
		doc.add(new SortedDocValuesField(toField, new BytesRef("1")));
		indexWriter.addDocument(doc);

		// 2 offer
		doc = new Document();
		doc.add(new TextField("price", "20.0", Field.Store.NO));
		doc.add(new TextField(idField, "3", Field.Store.NO));
		doc.add(new SortedDocValuesField(idField, new BytesRef("3")));
		doc.add(new TextField(toField, "1", Field.Store.NO));
		doc.add(new SortedDocValuesField(toField, new BytesRef("1")));
		indexWriter.addDocument(doc);

		// 3 product
		doc = new Document();
		doc.add(new TextField("description", "product2", Field.Store.NO));
		doc.add(new TextField("name", "name2", Field.Store.NO));
		doc.add(new TextField(idField, "4", Field.Store.NO));
		doc.add(new SortedDocValuesField(idField, new BytesRef("4")));
		indexWriter.addDocument(doc);

		// 4 offer
		doc = new Document();
		doc.add(new TextField("price", "10.0", Field.Store.NO));
		doc.add(new TextField(idField, "5", Field.Store.NO));
		doc.add(new SortedDocValuesField(idField, new BytesRef("5")));
		doc.add(new TextField(toField, "4", Field.Store.NO));
		doc.add(new SortedDocValuesField(toField, new BytesRef("4")));
		indexWriter.addDocument(doc);

		// 5 offer
		doc = new Document();
		doc.add(new TextField("price", "20.0", Field.Store.NO));
		doc.add(new TextField(idField, "6", Field.Store.NO));
		doc.add(new SortedDocValuesField(idField, new BytesRef("6")));
		doc.add(new TextField(toField, "4", Field.Store.NO));
		doc.add(new SortedDocValuesField(toField, new BytesRef("4")));
		indexWriter.addDocument(doc);

		indexWriter.close();
	}

	/**
	 * index Parent-Child documents
	 * 
	 * @throws IOException
	 */
	private void indexIndexTimeJoin() throws IOException {
		IndexWriter indexWriter = new IndexWriter(this.indexDir,
				new IndexWriterConfig(new WhitespaceAnalyzer()).setOpenMode(IndexWriterConfig.OpenMode.CREATE));

		final List<Document> docs = new ArrayList<>();

		// 一组，child doc在前，parent doc在后，一起addDocuments()
		docs.add(makeJob("java", 2007));
		docs.add(makeJob("python", 2010));
		docs.add(makeResume("Lisa", "United Kingdom"));
		indexWriter.addDocuments(docs);

		// 一组，child doc在前，parent doc在后，一起addDocuments()
		docs.clear();
		docs.add(makeJob("ruby", 2005));
		docs.add(makeJob("java", 2006));
		docs.add(makeResume("Frank", "United States"));
		indexWriter.addDocuments(docs);

		indexWriter.close();
	}

	// One resume...
	private Document makeResume(String name, String country) {
		Document resume = new Document();
		resume.add(new StringField("docType", "resume", Field.Store.NO));
		resume.add(new StringField("name", name, Field.Store.YES));
		resume.add(new StringField("country", country, Field.Store.NO));
		return resume;
	}

	// ... has multiple jobs
	private Document makeJob(String skill, int year) {
		Document job = new Document();
		job.add(new StringField("skill", skill, Field.Store.YES));
		job.add(new IntPoint("year", year));
		job.add(new StoredField("year", year));
		return job;
	}

	// ... has multiple qualifications
	private Document makeQualification(String qualification, int year) {
		Document job = new Document();
		job.add(new StringField("qualification", qualification, Field.Store.YES));
		job.add(new IntPoint("year", year));
		return job;
	}

	private void searchIndexTimeJoin() throws IOException {
		DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
		IndexSearcher searcher = new IndexSearcher(indexReader);
		
		// Create a filter that defines "parent" documents in the index - in this case resumes
	    BitSetProducer parentsFilter = new QueryBitSetProducer(new TermQuery(new Term("docType", "resume")));
	    CheckJoinIndex.check(indexReader, parentsFilter);

	    // Define child document criteria (finds an example of relevant work experience)
	    BooleanQuery.Builder childQuery = new BooleanQuery.Builder();
	    childQuery.add(new BooleanClause(new TermQuery(new Term("skill", "java")), Occur.MUST));
	    childQuery.add(new BooleanClause(IntPoint.newRangeQuery("year", 2006, 2011), Occur.MUST));

	    // Define parent document criteria (find a resident in the UK)
	    Query parentQuery = new TermQuery(new Term("country", "United Kingdom"));

	    // Wrap the child document query to 'join' any matches up to corresponding parent:
	    ToParentBlockJoinQuery childJoinQuery = new ToParentBlockJoinQuery(childQuery.build(), parentsFilter, ScoreMode.Avg);

	    // Combine the parent and nested child queries into a single query for a candidate
	    BooleanQuery.Builder fullQuery = new BooleanQuery.Builder();
	    fullQuery.add(new BooleanClause(parentQuery, Occur.MUST));
	    fullQuery.add(new BooleanClause(childJoinQuery, Occur.MUST));

//	    CheckHits.checkHitCollector(random(), fullQuery.build(), "country", searcher, new int[] {2});

	    TopDocs result = searcher.search(fullQuery.build(), 1);
	    System.out.println(result.totalHits.value);
	    
	    Document parentDoc = searcher.doc(result.scoreDocs[0].doc);
	    System.out.println("should be Lisa: " + parentDoc.get("name"));
	}

	private void searchQueryTimeJoin() throws IOException {
		DirectoryReader indexReader = DirectoryReader.open(this.indexDir);
		IndexSearcher searcher = new IndexSearcher(indexReader);

		// Search for product, 用TermQuery查，查出的结果集里的idField = 别人其他doc的toField的，返回别人其他doc
		// list
		// return doc 4/5
		Query joinQuery = JoinUtil.createJoinQuery(idField, false, toField, new TermQuery(new Term("name", "name2")),
				searcher, ScoreMode.None);
		TopDocs result = searcher.search(joinQuery, 10);
		System.out.println(result.totalHits.value);

		// Search for offer，用TermQuery查，查出的结果集里的toField = 别人其他doc的idField的，返回别人其他doc
		// list
		// return doc 3
		joinQuery = JoinUtil.createJoinQuery(toField, false, idField, new TermQuery(new Term("id", "5")), searcher,
				ScoreMode.None);
		result = searcher.search(joinQuery, 10);
		System.out.println(result.totalHits.value);
	}

	public void runQueryTimeJoin() throws IOException {
		indexQueryTimeJoin();
		searchQueryTimeJoin();
	}

	public void runIndexTimeJoin() throws IOException {
		indexIndexTimeJoin();
		searchIndexTimeJoin();
	}
}
