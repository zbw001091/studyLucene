package com.zbw.big.studyLucene;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.FSDirectory;

public class SimpleSearcher {

	public static void main(String[] args) {
		IndexReader reader;
		try {
			reader = DirectoryReader.open(FSDirectory.open(Paths.get("d:\\indexDir")));
			IndexSearcher searcher = new IndexSearcher(reader);
		    Analyzer analyzer = new StandardAnalyzer();
		    
		    // 【1.1】程序构建Query
		    Query queryByMachine = new TermQuery(new Term("title", "总裁"));
		    
		    // 【1.2】程序构建PhraseQuery
		    PhraseQuery.Builder builder = new PhraseQuery.Builder();
		    builder.add(new Term("title", "中文"), 4);
		    builder.add(new Term("title", "跳槽"), 5);
		    builder.setSlop(8);
		    PhraseQuery phraseQuery = builder.build();
		    
		    // 【1.3】程序构建MatchAllQuery
		    Query matchallquery = new MatchAllDocsQuery();
		    
		    // 【2】前端页面用户输入的查询条件，构建Query
		    // 需要对Human输入的查询条件，先做一次analyze（分词/lowercase）
		    QueryParser parser = new QueryParser("title", new SmartChineseAnalyzer());
		    Query queryByHuman = parser.parse("报价");
//		    Query queryByHuman = parser.parse("+facebook -MOCK");
		    
		    SortField sortField = new SortField("sortSeq", SortField.Type.LONG, false);
            Sort sort = new Sort(sortField);
            
            TFIDFSimilarity tfidf = new ClassicSimilarity();
            searcher.setSimilarity(tfidf);
            
            Explanation explain = searcher.explain(queryByHuman, 6);
            System.out.println(explain.getDescription());
            for (Explanation explanation : explain.getDetails()) {
            	System.out.println(explanation);
            }
            
		    // 开始 先identify document+后scoring document(use standard TFIDFSimilarity Model)
            TopDocs hits = searcher.search(matchallquery, 10);
//		    TopDocs hits = searcher.search(queryByHuman, 10, sort);
		    System.out.println(hits.totalHits);
		    
		    Document d;
		    for (ScoreDoc scoreDoc : hits.scoreDocs) {
		    	d = searcher.doc(scoreDoc.doc);
//		    	d = searcher.doc(hits.scoreDocs[0].doc);
			    System.out.println("top score document's title: " + d.get("title"));
			    System.out.println("top score document's score: " + scoreDoc);
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
