package com.zbw.big.studyLucene.analyzer.soundlike;

import java.io.IOException;

import org.apache.commons.codec.language.Metaphone;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class MetaphoneReplacementFilter extends TokenFilter {
	public static final String METAPHONE = "metaphone";
	private Metaphone metaphoner = new Metaphone();
	private CharTermAttribute termAttr;
	private TypeAttribute typeAttr;
	
	public MetaphoneReplacementFilter(TokenStream input) {
		super(input);
		termAttr = addAttribute(CharTermAttribute.class);
		typeAttr = addAttribute(TypeAttribute.class);
	}
	
	public boolean incrementToken() throws IOException {
		if (!input.incrementToken())
			return false;
		String encoded;
		encoded = metaphoner.encode(termAttr.toString());
		termAttr.setTermBuffer(encoded);
		typeAttr.setType(METAPHONE);
		return true;
	}
}