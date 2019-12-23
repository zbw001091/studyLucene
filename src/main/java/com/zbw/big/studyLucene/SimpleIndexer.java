package com.zbw.big.studyLucene;

import java.nio.file.Paths;
import java.util.Random;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.codecs.lucene50.Lucene50StoredFieldsFormat.Mode;
import org.apache.lucene.codecs.lucene80.Lucene80Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SimpleIndexer {

	// 索引目录类,指定索引在硬盘中的位置
	private Directory directory;
	
	public static void main(String[] args) throws Exception {
		/**
		// 1 创建文档对象
		Document document = new Document();
		// 创建并添加字段信息。参数：字段的名称、字段的值、是否存储，这里选Store.YES代表存储到文档列表。Store.NO代表不存储
		StringField sfield = new StringField("id", "1", Field.Store.YES);
		TextField tfield = new TextField("title",
				"汽车之家科鲁兹 2016款频道,提供科鲁兹 2016款报价,雪佛兰在售科鲁兹 2016款图片,雪佛兰在售科鲁兹参数配置,科鲁兹最新文章,保养周期及费用等最新信息,最精彩科鲁兹2016",
				Field.Store.YES);
//        Field subjectField = new Field("subject", "subject value", Field.Store.YES, Field.Index.ANALYZED);
		NumericDocValuesField docvaluefield = new NumericDocValuesField("sortSeq", 110);

		document.add(sfield);
		// 这里我们title字段需要用TextField，即创建索引又会被分词。StringField会创建索引，但是不会被分词
		document.add(tfield);
		document.add(docvaluefield);
		**/
		
		new SimpleIndexer().index("d:\\indexDir", new StandardAnalyzer());
	}

	private IndexWriter getWriter(Analyzer analyzer) throws Exception{
//		Analyzer analyzer = new SmartChineseAnalyzer();
//		Analyzer analyzer = new StandardAnalyzer(); // 标准分词器
//		IKAnalyzer analyzer = new IKAnalyzer();
		
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		// In-Memory Buffer满1MB/满10个document，就自动flush到磁盘Directory
		iwc.setRAMBufferSizeMB(1);
		iwc.setMaxBufferedDocs(2);
		// 影响.fdt field data文件的压缩，compression speed or compression ratio
		// the default: for high performance
		iwc.setCodec(new Lucene80Codec(Mode.BEST_SPEED));
		// instead for higher performance (but slower):
		// conf.setCodec(new Lucene80Codec(Mode.BEST_COMPRESSION));
		
		IndexWriter writer = new IndexWriter(directory, iwc);
		return writer;
	}
	
	private void index(String indexDir, Analyzer analyzer) throws Exception{
		directory = FSDirectory.open(Paths.get(indexDir));
		IndexWriter writer = getWriter(analyzer);
		
		// 【submit document to lucene, begin analyze and inverted indexing】
		writer.addDocument(createDocumentOfBook()); // 添加文档
		writer.commit();
		writer.close();
	}
	
	public Document createDocumentOfBook() {
		Document document = new Document();
		StringField bookid = new StringField("bookid", "1", Field.Store.YES);
		
		// bookname random with 6 character
		String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 6; i++) {
			int number = random.nextInt(62);
			sb.append(str.charAt(number));
		}
		StringField bookname = new StringField("bookname", sb.toString(), Field.Store.YES);
		
		int intpublishyear = 2010 + random.nextInt(9);
		IntPoint publishyear = new IntPoint("publishyear", intpublishyear);
		System.out.println(intpublishyear);
		
		String[] authors = {"john", "smith", "stockton"};
		for (String author : authors) {
			document.add(new TextField("author", author, Store.YES));
		}
		
		document.add(bookid);
		document.add(bookname);
		document.add(publishyear);
		return document;
	}
}
