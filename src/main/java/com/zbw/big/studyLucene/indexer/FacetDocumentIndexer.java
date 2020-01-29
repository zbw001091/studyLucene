package com.zbw.big.studyLucene.indexer;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.facet.FacetsConfig;
import org.apache.lucene.facet.sortedset.SortedSetDocValuesFacetField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class FacetDocumentIndexer {
	private Directory directory;
	private IndexWriter writer;
	private FacetsConfig config;
	
	private static final IndexableField[] DOC_SINGLEVALUED = new IndexableField[] {
			new SortedSetDocValuesFacetField("Author", "Mark Twain") };
	
	private static final IndexableField[] DOC_MULTIVALUED = new SortedSetDocValuesFacetField[] {
			new SortedSetDocValuesFacetField("Author", "Kurt Vonnegut") };
	
	private static final IndexableField[] DOC_NOFACET = new IndexableField[] {
			new TextField("Hello", "World", Field.Store.YES) };
	
	public void index(String indexDir, Analyzer analyzer) throws Exception{
		this.directory = FSDirectory.open(Paths.get(indexDir));
		this.writer = getWriter(analyzer);
		this.config = new FacetsConfig();
		
		// 【submit document to lucene, begin analyze and inverted indexing】
		indexDocuments(DOC_SINGLEVALUED, DOC_MULTIVALUED, DOC_NOFACET);
		this.writer.commit();
		this.writer.close();
	}
	
	private IndexWriter getWriter(Analyzer analyzer) throws Exception{
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		
		// debugging an index
//		iwc.setInfoStream(System.out);
		
		return new IndexWriter(this.directory, iwc);
	}
	
	private void indexDocuments(IndexableField[]... docs) throws IOException {
		for (IndexableField[] fields : docs) {
			for (IndexableField field : fields) {
				Document doc = new Document();
				doc.add(field);
				this.writer.addDocument(this.config.build(doc));
			}
		}
	}
}
