package com.zbw.big.studyLucene;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.codecs.lucene50.Lucene50StoredFieldsFormat.Mode;
import org.apache.lucene.codecs.lucene80.Lucene80Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SimpleIndexer {
	
	public static void main(String[] args) {
		// 【Step1 prepare document】
		// TODO 循环创建document
		//1 创建文档对象
        Document document = new Document();
        // 创建并添加字段信息。参数：字段的名称、字段的值、是否存储，这里选Store.YES代表存储到文档列表。Store.NO代表不存储
        StringField sfield = new StringField("id", "1", Field.Store.YES);
        TextField tfield = new TextField("title", "汽车之家科鲁兹 2016款频道,提供科鲁兹 2016款报价,雪佛兰在售科鲁兹 2016款图片,雪佛兰在售科鲁兹参数配置,科鲁兹最新文章,保养周期及费用等最新信息,最精彩科鲁兹2016", Field.Store.YES);
//        Field subjectField = new Field("subject", "subject value", Field.Store.YES, Field.Index.ANALYZED);
        NumericDocValuesField docvaluefield = new NumericDocValuesField("sortSeq", 110);
        
        document.add(sfield);
        // 这里我们title字段需要用TextField，即创建索引又会被分词。StringField会创建索引，但是不会被分词
        document.add(tfield);
        document.add(docvaluefield);
        
        //2 索引目录类,指定索引在硬盘中的位置
        Directory directory;
        IndexWriter indexWriter;
		try {
			directory = FSDirectory.open(new File("d:\\indexDir").toPath());
			//3 创建分词器对象
			Analyzer analyzer = new SmartChineseAnalyzer();
//	        Analyzer analyzer = new StandardAnalyzer();
	        //4 索引IndexWriter工具的配置对象
	        IndexWriterConfig conf = new IndexWriterConfig(analyzer);
	        // In-Memory Buffer满1MB/满10个document，就自动flush到磁盘Directory
	        conf.setRAMBufferSizeMB(1);
	        conf.setMaxBufferedDocs(10);
	        
	        // 影响.fdt field data文件的压缩，compression speed or compression ratio
	        // the default: for high performance
	        conf.setCodec(new Lucene80Codec(Mode.BEST_SPEED));
	        // instead for higher performance (but slower):
	        // conf.setCodec(new Lucene80Codec(Mode.BEST_COMPRESSION));
	        
	        //5 创建索引的写出工具类。参数：索引的目录和配置信息
	        indexWriter = new IndexWriter(directory, conf);
	        
	        // 【Step2 submit document to lucene, begin analyze and inverted indexing】
	        //6 把文档交给IndexWriter
	        indexWriter.addDocument(document);
	        //7 提交(flush后仍不能被search，commit后可以被search)
	        indexWriter.commit();
	        //8 关闭
	        indexWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
