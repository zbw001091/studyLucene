package com.zbw.big.studyLucene.main;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import com.zbw.big.studyLucene.searcher.DocumentSearcher;

public class DocumentSearchFacade {

	public static void main(String[] args) throws Exception {
		new DocumentSearcher().search("d:\\indexDir", new StandardAnalyzer(), true);;
	}

}
