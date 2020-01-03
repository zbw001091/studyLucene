package com.zbw.big.studyLucene.main;

import com.zbw.big.studyLucene.analyzer.synonym.SynonymAnalyzer;
import com.zbw.big.studyLucene.analyzer.synonym.SynonymEngineImpl;
import com.zbw.big.studyLucene.indexer.DocumentIndexer;

public class DocumentDeleteFacade {

	public static void main(String[] args) throws Exception {
		new DocumentIndexer().delete("d:\\indexDir", new SynonymAnalyzer(new SynonymEngineImpl()));
		System.out.println("delete successfully");
	}

}
