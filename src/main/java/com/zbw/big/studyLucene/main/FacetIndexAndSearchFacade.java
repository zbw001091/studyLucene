package com.zbw.big.studyLucene.main;

import java.util.List;

import org.apache.lucene.facet.FacetResult;

import com.zbw.big.studyLucene.indexer.FacetDocumentIndexer2;

public class FacetIndexAndSearchFacade {

	public static void main(String[] args) throws Exception {
		// one
//		System.out.println("1>> Facet counting example:");
//		System.out.println("-----------------------");
		FacetDocumentIndexer2 example = new FacetDocumentIndexer2();
//		List<FacetResult> results1 = example.runFacetOnly();
//		System.out.println("Author: " + results1.get(0));
//		System.out.println("Publish Date: " + results1.get(1));

		// two
		System.out.println("2>> Facet counting example (combined facets and search):");
		System.out.println("-----------------------");
		List<FacetResult> results = example.runFacetsWithSearch();
		System.out.println("Author: " + results.get(0));
		System.out.println("Publish Date: " + results.get(1));

		// three
		System.out.println("3>> Facet drill-down example (Publish Date/2010):");
		System.out.println("---------------------------------------------");
		System.out.println("Author: " + example.runDrillDown());

		// four
		System.out.println("4>> Facet drill-sideways example (Publish Date/2010):");
		System.out.println("---------------------------------------------");
		for (FacetResult result : example.runDrillSideways()) {
			System.out.println(result);
		}
		
		// five - rangeFacets
		System.out.println("5>> Facet range:");
		System.out.println("---------------------------------------------");
		System.out.println("Author: " + example.runFacetsWithRange());
		
		// six - longFacets
		System.out.println("6>> Facet long:");
		System.out.println("---------------------------------------------");
		System.out.println("Author: " + example.runFacetsWithLong());
	}

}
