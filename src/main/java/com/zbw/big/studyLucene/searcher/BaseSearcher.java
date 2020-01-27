package com.zbw.big.studyLucene.searcher;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.SegmentCommitInfo;
import org.apache.lucene.index.SegmentInfo;
import org.apache.lucene.index.SegmentInfos;
import org.apache.lucene.index.StandardDirectoryReader;
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
		Query query = getAQuery(reader);
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
	
	public abstract Query getAQuery(IndexReader reader) throws ParseException, Exception;
	
	private Sort getASort() {
		SortField sortField = new SortField("bookNoDocValue", SortField.Type.STRING, true);
        Sort sort = new Sort(sortField);
        return sort;
        
//        return Sort.RELEVANCE;
	}
	
	private void startSearch(Query query, Sort sort) {
		SegmentInfos sis = ((StandardDirectoryReader)reader).getSegmentInfos();
		System.out.println("Segments_N: " + sis.getVersion());
		System.out.println("Segments_N: " + sis.getCommitLuceneVersion());
		System.out.println("Segments_N: " + sis.getMinSegmentLuceneVersion());
		System.out.println("Segments_N: " + sis.getIndexCreatedVersionMajor());
		System.out.println("Segments_N: " + sis.counter);
		System.out.println("Segments_N: " + sis.toString());
		System.out.println("Segments_N: " + sis.size() + " segments");
		for (int i = 0; i < sis.size(); i++) {
			SegmentCommitInfo sci = sis.info(i);
			sci.getDelCount();
			// 该segment中的.dvm和.dvd
			System.out.println(".sci.DocValuesGeneration: " + sci.getDocValuesGen());
			// 该segment中的.fnm
			System.out.println(".sci.getFieldInfosGen: " + sci.getFieldInfosGen());
			SegmentInfo si = sci.info;
			System.out.println(".si.name: " + si.name);
			si.getCodec();
			si.getDiagnostics();
			si.getAttributes();
		}
		
		try {
//			IndexSearcher searcher = new IndexSearcher(reader);
			
			ExecutorService es = Executors.newFixedThreadPool(5);
			IndexSearcher searcher = new IndexSearcher(reader, es);
			
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
		    
		    // LeafReader(Segment), 从1个Segment里获取content字段的所有terms
		    System.out.println();
		    System.out.println(reader.leaves().size() + " leafReaders/segments");
		    System.out.println(reader.leaves().get(1).reader().terms("content"));
		    
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
		    System.out.println(hits.totalHits + "\n");
		    
		    Document d;
		    for (ScoreDoc scoreDoc : hits.scoreDocs) {
		    	d = searcher.doc(scoreDoc.doc);
		    	
		    	String[] content = d.getValues("content");
		    	for (int i = 0; i < content.length; i++) {
		    		System.out.println(content[i]);
		    	}
		    	
		    	// only fields with fieldType.setStored(true), are returned
		    	for (IndexableField field : d.getFields()) {
		    		System.out.println("【doc_fieldName】: " + field.name() + "，【doc_fieldValue】: " + field.stringValue());
		    	}
			    System.out.println("【doc score】:  " + scoreDoc);
			    System.out.println("\n");
		    }
		    
		    /** 
		    // get termVector for 1 doc (docId = 0)
	    	Fields vector = reader.getTermVectors(0);
	    	if (vector != null) {
	    		// get terms from termVector for 1 doc and 1 field (field = contents)
	    		Terms terms = vector.terms("contents");
	    		if (terms != null) {
	    			TermsEnum iterator = terms.iterator();
	    			BytesRef v;
	    			while ((v = iterator.next()) != null) {
	    				System.out.println(v.utf8ToString());
	    			}
	    		}
	    	}
	    	System.out.println("\n");
	    	
	    	// get termVector for 1 doc and 1 field, directly
	    	Terms terms = reader.getTermVector(0, "contents");
	    	if (terms != null) {
    			TermsEnum iterator = terms.iterator();
    			BytesRef v;
    			while ((v = iterator.next()) != null) {
    				System.out.println(v.utf8ToString());
    			}
    		}
    		**/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
