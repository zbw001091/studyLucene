package com.zbw.big.studyLucene.main;

import com.zbw.big.studyLucene.analyzer.synonym.SynonymAnalyzer;
import com.zbw.big.studyLucene.analyzer.synonym.SynonymEngineImpl;
import com.zbw.big.studyLucene.backup.BackupIndex;

public class BackupFacade {

	public static void main(String[] args) throws Exception {
		new BackupIndex().backup("d:\\indexDir", new SynonymAnalyzer(new SynonymEngineImpl()));
		System.out.println("backup successfully");
	}

}
