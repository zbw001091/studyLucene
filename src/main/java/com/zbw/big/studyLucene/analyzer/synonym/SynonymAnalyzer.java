package com.zbw.big.studyLucene.analyzer.synonym;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

public class SynonymAnalyzer extends Analyzer {
	
	private SynonymEngine engine;
	
	public SynonymAnalyzer(SynonymEngine engine) {
		this.engine = engine;
	}
	
	/**public TokenStream tokenStream(String fieldName, Reader reader) {
		System.err.println("SynonymAnalyzer.tokenStream()");
		TokenStream result = new SynonymFilter(
								new StopFilter(
									new LowerCaseFilter(
										new StandardTokenizer()
									), CharArraySet.EMPTY_SET),
								engine);
		return result;
	}**/
	
	// each field has it's own analyzer(tokenizer/filters)
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
//		System.err.println("SynonymAnalyzer.createComponents()");
		Tokenizer source = new StandardTokenizer();
		TokenStream filters = new SynonymFilter(
								new PorterStemFilter(
									new StopFilter(
										new LowerCaseFilter(source), 
										CharArraySet.EMPTY_SET
									)
								),
								engine);
		return new TokenStreamComponents(source, filters);
	}
}
