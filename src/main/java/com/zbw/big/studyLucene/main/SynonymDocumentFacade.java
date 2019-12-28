package com.zbw.big.studyLucene.main;

import com.zbw.big.studyLucene.analyzer.synonym.SynonymAnalyzer;
import com.zbw.big.studyLucene.analyzer.synonym.SynonymEngineImpl;
import com.zbw.big.studyLucene.indexer.DocumentIndexer;

public class SynonymDocumentFacade {

	public static void main(String[] args) throws Exception {
		new DocumentIndexer().index("d:\\indexDir", new SynonymAnalyzer(new SynonymEngineImpl()));
		System.out.println("index successfully");
	}

}
