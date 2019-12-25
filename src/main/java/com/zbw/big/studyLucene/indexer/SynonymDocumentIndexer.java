package com.zbw.big.studyLucene.indexer;

import java.util.Random;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.util.BytesRef;

/**
 * usage:
 * new SynonymDocumentIndexer().index("d:\\indexDir", new SynonymAnalyzer(new SynonymEngineImpl()))
 * 
 * @author st78sr
 */
public class SynonymDocumentIndexer extends BaseIndexer {

	@Override
	public Document createADocument() {
//		Document doc = new Document();
//		doc.add(new TextField("content", "The quick brown fox jumps over the lazy dog", Field.Store.YES));
//		return doc;
		
		Document document = new Document();
		
		// bookname random with 6 character
		String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		
		StringField bookid = new StringField("bookid", "1", Field.Store.YES);
		for (int i = 0; i < 6; i++) {
			int number = random.nextInt(62);
			sb.append(str.charAt(number));
		}
//		NumericDocValuesField bookNo = new NumericDocValuesField("bookNo", random.nextInt(62));
		
		StringField bookname = new StringField("bookname", sb.toString(), Field.Store.YES);
		
		int intpublishyear = 2010 + random.nextInt(9);
		IntPoint publishyear = new IntPoint("publishyear", intpublishyear);
		System.out.println(intpublishyear);
		
		String[] authors = {"john", "smith", "stockton"};
		for (String author : authors) {
			document.add(new TextField("author", author, Store.YES));
		}
		
//		FieldType fieldType = new FieldType();
//		fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);//set是否索引
//		fieldType.setStored(true);//set是否存储
//		fieldType.setTokenized(false);//set是否分类
//		fieldType.setStoreTermVectors(true);//向量存储,document based inverted index,docID.terms[]<freq,pos,offset,payload>
//		fieldType.setDocValuesType(DocValuesType.SORTED);
//		document.add(new Field("bookNoDocValue", Integer.toString(random.nextInt(62)), fieldType));
		int rand = random.nextInt(62);
		System.err.println("SynonymDocumentIndexer: " + rand);
		SortedDocValuesField sortedDocValuesField = new SortedDocValuesField("bookNoDocValue", new BytesRef(Integer.toString(rand).getBytes()));
		document.add(sortedDocValuesField);
		document.add(new StoredField("bookNoDocValue", new BytesRef(Integer.toString(rand).getBytes())));
		
		document.add(bookid);
		document.add(bookname);
		document.add(publishyear);
		return document;
	}
}
