package com.zbw.big.studyLucene.indexer;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.codecs.lucene50.Lucene50StoredFieldsFormat.Mode;
import org.apache.lucene.codecs.lucene80.Lucene80Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * usage:
 * new BaseIndex() or its subclass().index("d:\\indexDir", new StandardAnalyzer())
 * 
 * @author st78sr
 */
public abstract class BaseIndexer {
	// 索引目录类,指定索引在硬盘中的位置
	private Directory directory;
	
	public BaseIndexer() {
		super();
	}
	
	public void index(String indexDir, Analyzer analyzer) throws Exception{
		directory = FSDirectory.open(Paths.get(indexDir));
		IndexWriter writer = getWriter(analyzer);
		
		// 【submit document to lucene, begin analyze and inverted indexing】
		writer.addDocument(createADocument()); // 添加文档
		writer.commit();
		writer.close();
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
	
	public abstract Document createADocument();
}
