package com.zbw.big.studyLucene.analyzer.stopandstemming;

import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class PositionalStopStemAnalyzer extends Analyzer {
	private CharArraySet stopWords;
	
	public PositionalStopStemAnalyzer(String[] stopWords) {
		this.stopWords = StopFilter.makeStopSet(Arrays.asList(stopWords));
	}
	
//	public TokenStream tokenStream(String fieldName, Reader reader) {
//		StopFilter stopFilter = new StopFilter(new LetterTokenizer(), this.stopWords);
////		stopFilter.setEnablePositionIncrements(true);
//		return new PorterStemFilter(stopFilter);
//	}
	
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer source = new StandardTokenizer();
		TokenStream filters = new PorterStemFilter(
								new StopFilter(
									new LowerCaseFilter(source), 
									this.stopWords
								));
		return new TokenStreamComponents(source, filters);
	}
}
