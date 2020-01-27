package com.zbw.big.studyLucene.main;

import com.zbw.big.studyLucene.indexer.SpatialDocumentIndexer;

public class SpatialDocumentIndexFacade {

	public static void main(String[] args) throws Exception {
		SpatialDocumentIndexer sdi = new SpatialDocumentIndexer();
		sdi.init();
		sdi.indexPoints();
	}

}
