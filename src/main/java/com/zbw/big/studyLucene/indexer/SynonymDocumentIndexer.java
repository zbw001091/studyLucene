package com.zbw.big.studyLucene.indexer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

/**
 * usage:
 * new SynonymDocumentIndexer().index("d:\\indexDir", new SynonymAnalyzer(new SynonymEngineImpl()))
 * 
 * @author st78sr
 */
public class SynonymDocumentIndexer extends BaseIndexer {

	@Override
	public Document createADocument() {
		Document doc = new Document();
		doc.add(new TextField("content", "The quick brown fox jumps over the lazy dog", Field.Store.YES));
		return doc;
	}

}
