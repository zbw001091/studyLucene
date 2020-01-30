package com.zbw.big.studyLucene.main;

import java.io.IOException;

import com.zbw.big.studyLucene.indexer.JoinDocumentIndexer;

public class JoinIndexAndSearchFacade {

	public static void main(String[] args) throws IOException {
		JoinDocumentIndexer join = new JoinDocumentIndexer();
		
		// one
		System.out.println("1>> Query-time join, need to save foreign-key in child docs during indexing phase");
		System.out.println("-----------------------");
		join.runQueryTimeJoin();
		
		// two
		System.out.println();
		System.out.println("2>> Index-time join, no foreign-key in child docs");
		System.out.println("-----------------------");
		join.runIndexTimeJoin();
	}

}
