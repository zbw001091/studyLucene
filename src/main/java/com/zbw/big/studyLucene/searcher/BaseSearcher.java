package com.zbw.big.studyLucene.searcher;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

/**
 * usage:
 * new BaseSearch() or its subclass().index("d:\\indexDir", new StandardAnalyzer(), true)
 * 
 * @author st78sr
 */
public abstract class BaseSearcher {
	IndexReader reader;
	
	public BaseSearcher() {
		super();
	}
	
	/**
	 * 
	 * @param indexDir
	 * @param analyzer
	 * @param defaultSortByScore, sortByScore(true) or sortByField(false)
	 * @throws Exception
	 */
	public void search(String indexDir, Analyzer analyzer, boolean defaultSortByScore) throws Exception {
		getReader(indexDir);
		Query query = getAQuery();
		Sort sort = null;
		if (!defaultSortByScore) {
			sort = getASort();
		}
		startSearch(query, sort);
	}
	
	private void getReader(String indexDir) throws Exception {
		reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
	}
	
	private void closeReader() throws IOException {
		reader.close();
	}
	
	public abstract Query getAQuery() throws ParseException, Exception;
	
	private Sort getASort() {
		SortField sortField = new SortField("bookNoDocValue", SortField.Type.STRING, true);
        Sort sort = new Sort(sortField);
        return sort;
        
//        return Sort.RELEVANCE;
	}
	
	private void startSearch(Query query, Sort sort) {
		try {
			IndexSearcher searcher = new IndexSearcher(reader);
	        TFIDFSimilarity tfidf = new ClassicSimilarity();
	        searcher.setSimilarity(tfidf);
	        TopDocs hits = null;
	        // 开始 先identify document + 后scoring document(use standard TFIDFSimilarity Model)
	        if (sort != null) {
	        	hits = searcher.search(query, 100, sort);
	        } else {
	        	hits = searcher.search(query, 100);
	        }
	        
	        
		    int maxDocs = reader.maxDoc();
		    int numDocs = reader.numDocs();
		    
	        Explanation explain = searcher.explain(query, 21);
	        System.out.println(explain.getDescription());
	        for (Explanation explanation : explain.getDetails()) {
	        	System.out.println(explanation);
	        }
	        
		    /**
		     * term vector
		     */
		    Terms termVector = reader.getTermVector(0, "contents");
		    BytesRef bytesRef = null;
		    TermsEnum termsEnum = termVector.iterator();
		    while (termsEnum != null) {
		    	bytesRef = termsEnum.next();
		    	if (bytesRef == null) {
		    		break;
		    	}
		    }
		    
		    System.out.println("\nResults for: " + query.toString() + ", sorted by " + sort);
		    System.out.println(hits.totalHits);
		    
		    Document d;
		    for (ScoreDoc scoreDoc : hits.scoreDocs) {
		    	d = searcher.doc(scoreDoc.doc);
		    	// only fields with fieldType.setStored(true), are returned
		    	for (IndexableField field : d.getFields()) {
		    		System.out.println("【doc_fieldName】: " + field.name() + "，【doc_fieldValue】: " + field.stringValue());
		    	}
			    System.out.println("【doc score】:  " + scoreDoc);
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
