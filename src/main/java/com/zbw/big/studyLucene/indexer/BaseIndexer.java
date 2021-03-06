package com.zbw.big.studyLucene.indexer;

import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.codecs.lucene50.Lucene50StoredFieldsFormat.Mode;
import org.apache.lucene.codecs.lucene80.Lucene80Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.NoDeletionPolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * usage: new BaseIndex() or its subclass().index("d:\\indexDir", new
 * StandardAnalyzer())
 * 
 * @author st78sr
 */
public abstract class BaseIndexer {
	// 索引目录类,指定索引在硬盘中的位置
	private Directory directory;

	public BaseIndexer() {
		super();
	}

	public void index(String indexDir, Analyzer analyzer) throws Exception {
		directory = FSDirectory.open(Paths.get(indexDir));
		IndexWriter writer = getWriter(analyzer);

		// 【submit document to lucene, begin analyze and inverted indexing】
		writer.addDocument(createADocument()); // 添加文档
		writer.commit();
		writer.close();
	}

	public void update(String indexDir, Analyzer analyzer) throws Exception {
		directory = FSDirectory.open(Paths.get(indexDir));
		IndexWriter writer = getWriter(analyzer);
	}

	public void delete(String indexDir, Analyzer analyzer) throws Exception {
		directory = FSDirectory.open(Paths.get(indexDir));
		IndexWriter writer = getWriter(analyzer);

		writer.deleteDocuments(new Term("booknameText", "hkeyck"));
		writer.prepareCommit();
		writer.commit();
		writer.close();
	}

	private IndexWriter getWriter(Analyzer analyzer) throws Exception {
//		Analyzer analyzer = new SmartChineseAnalyzer();
//		Analyzer analyzer = new StandardAnalyzer(); // 标准分词器
//		IKAnalyzer analyzer = new IKAnalyzer();

		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		System.out.println("MergePolicy: " + iwc.getMergePolicy());
		System.out.println("MergeScheduler: " + iwc.getMergeScheduler());

		// debugging an index
		iwc.setInfoStream(System.out);

		// 全局变量。In-Memory Buffer满1MB，就触发"自动flush"，到磁盘Directory
		iwc.setRAMBufferSizeMB(1);
		// 单DWPT变量。In-Memory Buffer达到2个doc，就触发"自动flush"，到磁盘Directory
		iwc.setMaxBufferedDocs(2);
		// 控制每个DWPT什么时候该"自动flush"出1个new segment，达到RAM limit就该自动flush
		iwc.setRAMPerThreadHardLimitMB(1);

		// commit point的默认删除机制，segments_N file delete policy
		System.out.println("IndexDeletionPolicy: " + iwc.getIndexDeletionPolicy());
		// only keep the latest commit point, segments_N file
//		iwc.setIndexDeletionPolicy(NoDeletionPolicy.INSTANCE);

		// 影响.fdt field data文件的压缩，compression speed or compression ratio
		// the default: for high performance
		iwc.setCodec(new Lucene80Codec(Mode.BEST_SPEED));
		// instead for higher performance (but slower):
		// conf.setCodec(new Lucene80Codec(Mode.BEST_COMPRESSION));

		iwc.setCommitOnClose(true);
		
		IndexWriter writer = new IndexWriter(directory, iwc);
		return writer;
	}

	public abstract Document createADocument();
}
