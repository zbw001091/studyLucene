package com.zbw.big.studyLucene;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PointRangeQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

public class SimpleSearcher {

	public static void main(String[] args) {
		IndexReader reader;
		try {
			reader = DirectoryReader.open(FSDirectory.open(Paths.get("d:\\indexDir")));
			IndexSearcher searcher = new IndexSearcher(reader);
		    Analyzer analyzer = new StandardAnalyzer();
		    
		    int maxDocs = reader.maxDoc();
		    int numDocs = reader.numDocs();
		    
		    // 【1.1】程序构建Query
		    Query queryByMachine = new TermQuery(new Term("contents", "01_camel"));
		    
		    // 【1.2】程序构建PhraseQuery
		    PhraseQuery.Builder builder = new PhraseQuery.Builder();
		    builder.add(new Term("title", "中文"), 4);
		    builder.add(new Term("title", "跳槽"), 5);
		    builder.setSlop(8);
		    PhraseQuery phraseQuery = builder.build();
		    
		    // 【1.3】程序构建MatchAllQuery
		    Query matchallquery = new MatchAllDocsQuery();
		    
		    // 【1.4】程序构建termQuery，term大于以c开头，小于以m开头，搜出docID list
		    TermRangeQuery termRangeQuery = new TermRangeQuery("bookname", new BytesRef("c"), new BytesRef("m"), true, true);
		    
		    PointRangeQuery newRangeQuery = (PointRangeQuery)IntPoint.newRangeQuery("publishyear", 2018, 2019);
		    
		    PointRangeQuery newExactQuery = (PointRangeQuery)IntPoint.newExactQuery("publishyear", 2018);
		    
		    PrefixQuery prefixQuery = new PrefixQuery(new Term("author", "stock"));
		    
		    // 【2】前端页面用户输入的查询条件，构建Query
		    // 需要对Human输入的查询条件，先做一次analyze（分词/lowercase）
		    QueryParser parser = new QueryParser("contents", new StandardAnalyzer()); //SmartChineseAnalyzer
		    Query queryByHuman = parser.parse("mdosdco07");
//		    Query queryByHuman = parser.parse("+facebook -MOCK");
		    
		    SortField sortField = new SortField("sortSeq", SortField.Type.LONG, false);
            Sort sort = new Sort(sortField);
            
            TFIDFSimilarity tfidf = new ClassicSimilarity();
            searcher.setSimilarity(tfidf);
            
            Explanation explain = searcher.explain(queryByHuman, 0);
            System.out.println(explain.getDescription());
            for (Explanation explanation : explain.getDetails()) {
//            	System.out.println(explanation);
            }
            
		    // 开始 先identify document+后scoring document(use standard TFIDFSimilarity Model)
            TopDocs hits = searcher.search(prefixQuery, 10);
//            TopDocs hits = searcher.search(matchallquery, 10);
//		    TopDocs hits = searcher.search(queryByHuman, 10, sort);
		    System.out.println(hits.totalHits);
		    
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
//		    	System.out.println(">>> " + bytesRef.utf8ToString());
		    }
//		    System.out.println(">>> " + reader.getSumDocFreq("contents"));
		    
		    Document d;
		    for (ScoreDoc scoreDoc : hits.scoreDocs) {
		    	d = searcher.doc(scoreDoc.doc);
		    	// only fields with fieldType.setStored(true), are returned
		    	for (IndexableField field : d.getFields()) {
		    		System.out.println("【doc_fieldName】: " + field.name() + "，【doc_fieldValue】: " + field.stringValue());
		    	}
			    System.out.println("【doc score】:  " + scoreDoc);
		    }
		    
		    reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
