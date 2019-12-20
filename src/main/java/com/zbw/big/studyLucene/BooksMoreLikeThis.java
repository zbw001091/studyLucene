package com.zbw.big.studyLucene;

import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class BooksMoreLikeThis {
	public static void main(String[] args) throws Throwable {
//		String indexDir = System.getProperty("index.dir");
		FSDirectory directory = FSDirectory.open(Paths.get("d:\\indexDir"));
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		int numDocs = reader.maxDoc();
		MoreLikeThis mlt = new MoreLikeThis(reader);
		mlt.setFieldNames(new String[] { "contents" });
		mlt.setMinTermFreq(1);
		mlt.setMinDocFreq(1);
		
		for (int docID = 0; docID < numDocs; docID++) {
			System.out.println();
			Document doc = reader.document(docID);
			System.out.println("docID: " + docID + ": " + doc.get("path"));
			Query query = mlt.like(docID);
			System.out.println(" query=" + query);
			TopDocs similarDocs = searcher.search(query, 10);
			if (similarDocs.totalHits.value == 0)
				System.out.println(" None like this");
			for (int i = 0; i < similarDocs.scoreDocs.length; i++) {
				if (similarDocs.scoreDocs[i].doc != docID) {
					doc = reader.document(similarDocs.scoreDocs[i].doc);
					System.out.println(" -> " + doc.getField("path").stringValue());
				}
			}
		}
//		searcher.close();
		reader.close();
		directory.close();
	}
}