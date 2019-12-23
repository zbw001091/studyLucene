package com.zbw.big.studyLucene.analyzer.synonym;

import java.io.IOException;

public interface SynonymEngine {
	String[] getSynonyms(String s) throws IOException;
}
