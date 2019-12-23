package com.zbw.big.studyLucene.analyzer.synonym;

import java.io.IOException;
import java.util.Stack;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;

public class SynonymFilter extends TokenFilter {
	public static final String TOKEN_TYPE_SYNONYM = "SYNONYM";
	private Stack<String> synonymStack;
	private SynonymEngine engine;
	private AttributeSource.State current;
	private final CharTermAttribute termAtt;
	private final PositionIncrementAttribute posIncrAtt;
//	public char[] termBuf;
	
	public SynonymFilter(TokenStream in, SynonymEngine engine) {
		super(in);
		synonymStack = new Stack<String>();
		this.engine = engine;
		this.termAtt = addAttribute(CharTermAttribute.class);
		this.posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	}
	
	public boolean incrementToken() throws IOException {
		if (synonymStack.size() > 0) {
			String syn = synonymStack.pop();
			restoreState(current);
//			termAtt.setTermBuffer(syn);
//			termAtt.append(syn);
//			termBuf = termAtt.buffer();
			termAtt.copyBuffer(syn.toCharArray(), 0, syn.toCharArray().length); // set synonym into the termAttr Buf
			posIncrAtt.setPositionIncrement(0); // set position incr to 0, meaning this is a synonym
			return true;
		}
		if (!input.incrementToken())
			return false;
		if (addAliasesToStack()) {
			current = captureState();
		}
		return true;
	}
	
	private boolean addAliasesToStack() throws IOException {
		String[] synonyms = engine.getSynonyms(termAtt.toString());
		if (synonyms == null) {
			return false;
		}
		for (String synonym : synonyms) {
			synonymStack.push(synonym);
		}
		return true;
	}
}
