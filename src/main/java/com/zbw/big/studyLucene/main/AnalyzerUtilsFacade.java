package com.zbw.big.studyLucene.main;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.cjk.CJKAnalyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import com.zbw.big.studyLucene.analyzer.AnalyzerUtils;
import com.zbw.big.studyLucene.analyzer.stopandstemming.PositionalStopStemAnalyzer;
import com.zbw.big.studyLucene.analyzer.synonym.SynonymAnalyzer;
import com.zbw.big.studyLucene.analyzer.synonym.SynonymEngineImpl;

public class AnalyzerUtilsFacade {
	private static final String[] examples = { 
			"The quick brown fox jumps over the lazy dog", 
			"XY&Z Corporation - xyz@example.com",
			"道德經是個高富帥"};
	
	private static final String[] stopWords = {"the","a","an"};
	
	private static final Analyzer[] analyzers = new Analyzer[] { 
			new WhitespaceAnalyzer(), 
			new SimpleAnalyzer(), 
			new StopAnalyzer(StopFilter.makeStopSet(Arrays.asList(AnalyzerUtilsFacade.stopWords))), 
			new StandardAnalyzer(),
			new CJKAnalyzer(),
			new SmartChineseAnalyzer(),
			new SynonymAnalyzer(new SynonymEngineImpl()),
			new PositionalStopStemAnalyzer(AnalyzerUtilsFacade.stopWords)
			/** new IKAnalyzer() **/ };
	
	public static void main(String[] args) throws IOException {
		String[] strings = examples;
		if (args.length > 0) {
			strings = args;
		}
		for (String text : strings) {
			analyze(text);
		}
	}
	
	private static void analyze(String text) throws IOException {
		System.out.println("Analyzing \"" + text + "\"");
		
		// different analyzer has different tokenStream config
		for (Analyzer analyzer : analyzers) {
			String name = analyzer.getClass().getSimpleName();
			System.out.println(" " + name + ":");
			AnalyzerUtils.displayTokens(analyzer, text);
			System.out.println("\n");
		}
	}
}
