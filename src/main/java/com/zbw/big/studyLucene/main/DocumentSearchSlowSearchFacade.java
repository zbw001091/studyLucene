package com.zbw.big.studyLucene.main;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import com.zbw.big.studyLucene.searcher.slowsearch.SlowSearch;

public class DocumentSearchSlowSearchFacade {
	public static void main(String[] args) throws Exception {
		new SlowSearch().normalSearch("d:\\indexDir");
		new SlowSearch().slowSearch("d:\\indexDir");
	}
}
