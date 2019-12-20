package com.zbw.big.studyLucene;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class BooksLikeThis {
	public static void main(String[] args) throws IOException {
		Directory dir = FSDirectory.open(Paths.get("d:\\indexDir"));
		IndexReader reader = DirectoryReader.open(dir);
		int numDocs = reader.maxDoc();
		
		BooksLikeThis blt = new BooksLikeThis(reader);
		for (int i = 0; i < numDocs; i++) {
			System.out.println();
			Document doc = reader.document(i);
			System.out.println(doc.get("title"));
			Document[] docs = blt.docsLike(i, 10);
			if (docs.length == 0) {
				System.out.println(" None like this");
			}
			for (Document likeThisDoc : docs) {
				System.out.println(" -> " + likeThisDoc.get("title"));
			}
		}
		reader.close();
		dir.close();
	}

	private IndexReader reader;
	private IndexSearcher searcher;

	public BooksLikeThis(IndexReader reader) {
		this.reader = reader;
		searcher = new IndexSearcher(reader);
	}

	public Document[] docsLike(int id, int max) throws IOException {
		Document doc = reader.document(id);

		String[] authors = doc.getValues("author");
		BooleanQuery.Builder authorQueryBuilder = new BooleanQuery.Builder();
		for (String author : authors) {
			authorQueryBuilder.add(new TermQuery(new Term("author", author)), BooleanClause.Occur.SHOULD);
		}
		BooleanQuery authorQuery = authorQueryBuilder.build();

		TermFreqVector vector = reader.getTermFreqVector(id, "subject");
		BooleanQuery.Builder subjectQueryBuilder = new BooleanQuery.Builder();
		for (String vecTerm : vector.getTerms()) {
			TermQuery tq = new TermQuery(new Term("subject", vecTerm));
			subjectQueryBuilder.add(tq, BooleanClause.Occur.SHOULD);
		}
		BooleanQuery subjectQuery = subjectQueryBuilder.build();

		BooleanQuery.Builder likeThisQueryBuilder = new BooleanQuery.Builder();
		likeThisQueryBuilder.add(authorQuery, BooleanClause.Occur.SHOULD);
		likeThisQueryBuilder.add(subjectQuery, BooleanClause.Occur.SHOULD);
		likeThisQueryBuilder.add(new TermQuery(new Term("isbn", doc.get("isbn"))), BooleanClause.Occur.MUST_NOT);
		BooleanQuery likeThisQuery = likeThisQueryBuilder.build();

		TopDocs hits = searcher.search(likeThisQuery, 10);
		int size = max;
		if (max > hits.scoreDocs.length)
			size = hits.scoreDocs.length;
		Document[] docs = new Document[size];
		for (int i = 0; i < size; i++) {
			docs[i] = reader.document(hits.scoreDocs[i].doc);
		}
		return docs;
	}
}