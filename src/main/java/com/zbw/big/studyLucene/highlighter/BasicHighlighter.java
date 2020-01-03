package com.zbw.big.studyLucene.highlighter;

import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;

public class BasicHighlighter {

	public static void main(String[] args) throws Exception {
		// mock an index
		String text = "The quick brown fox jumps over the lazy dog";
		TokenStream tokenStream = new SimpleAnalyzer().tokenStream("field", new StringReader(text));
		
		// mock a query
		TermQuery query = new TermQuery(new Term("field", "fox"));
		
		QueryScorer scorer = new QueryScorer(query, "field");
		Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);
		Highlighter highlighter = new Highlighter(scorer);
		highlighter.setTextFragmenter(fragmenter);
		
		System.out.println(highlighter.getBestFragment(tokenStream, text));
	}

}
