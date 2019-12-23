package com.zbw.big.studyLucene.analyzer.soundlike;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.util.PagedBytes.Reader;

public class MetaphoneReplacementAnalyzer extends Analyzer {
	public TokenStream tokenStream(String fieldName, Reader reader) {
		return new MetaphoneReplacementFilter(new LetterTokenizer());
	}
}
