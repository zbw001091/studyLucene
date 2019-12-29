package com.zbw.big.studyLucene.indexer;

import java.util.Random;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.util.BytesRef;

/**
 * usage:
 * new DocumentIndexer().index("d:\\indexDir", new SynonymAnalyzer(new SynonymEngineImpl()))
 * 
 * @author st78sr
 */
public class DocumentIndexer extends BaseIndexer {
	private static final String[] authors = {"john", "smith", "stockton", "jane", "helen", "oneil", "bryant", "johnson", "lebron", "antonie"};
	
	@Override
	public Document createADocument() {
//		Document doc = new Document();
//		doc.add(new TextField("content", "The quick brown fox jumps over the lazy dog", Field.Store.YES));
//		return doc;
		
		Document document = new Document();
		
		// bookname by random
		String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 6; i++) {
			int number = random.nextInt(62);
			sb.append(str.charAt(number));
		}
		sb.append(" ");
		for (int i = 0; i < 6; i++) {
			int number = random.nextInt(62);
			sb.append(str.charAt(number));
		}
		
		// bookid
		StringField bookid = new StringField("bookid", "1", Field.Store.YES);
		document.add(bookid);
		
		// bookNo, field used for customized score. functionScoreQuery.
		int randOfBookNo = random.nextInt(62);
		System.err.println("DocumentIndexer.bookNo: " + randOfBookNo);
		NumericDocValuesField bookNo = new NumericDocValuesField("bookNo", randOfBookNo);
		document.add(bookNo);
		document.add(new StoredField("bookNo", new BytesRef(Integer.toString(randOfBookNo).getBytes())));
		
		// bookNoDocValue, field of doc_values, for sorting and aggs
		SortedDocValuesField sortedDocValuesField = new SortedDocValuesField("bookNoDocValue", new BytesRef(Integer.toString(randOfBookNo).getBytes()));
		document.add(sortedDocValuesField);
		document.add(new StoredField("bookNoDocValue", new BytesRef(Integer.toString(randOfBookNo).getBytes())));
		
		// booknameString, index, but not tokenized
		StringField booknameString = new StringField("booknameString", sb.toString(), Field.Store.YES);
		document.add(booknameString);
		
		// booknameText, index, and tokenized
		TextField booknameText = new TextField("booknameText", sb.toString(), Field.Store.YES);
		document.add(booknameText);
		
		// publishyear
		int intpublishyear = 2010 + random.nextInt(9);
		IntPoint publishyear = new IntPoint("publishyear", intpublishyear);
		System.out.println(intpublishyear);
		document.add(publishyear);
		
		// authors, multi-value field
		for (int i = 0; i < 3; i++) {
			int randOfAuthor = random.nextInt(10);
			document.add(new TextField("author", authors[randOfAuthor], Store.YES));
		}
		
		document.add(new TextField("contents", "fGy050", Field.Store.YES));
		
//		FieldType fieldType = new FieldType();
//		fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);//set是否索引
//		fieldType.setStored(true);//set是否存储
//		fieldType.setTokenized(false);//set是否分类
//		fieldType.setStoreTermVectors(true);//向量存储,document based inverted index,docID.terms[]<freq,pos,offset,payload>
//		fieldType.setDocValuesType(DocValuesType.SORTED);
//		document.add(new Field("bookNoDocValue", Integer.toString(random.nextInt(62)), fieldType));
		
		return document;
	}
}
