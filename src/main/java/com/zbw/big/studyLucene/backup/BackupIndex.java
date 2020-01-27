package com.zbw.big.studyLucene.backup;

import java.nio.file.Paths;
import java.util.Collection;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexDeletionPolicy;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.SnapshotDeletionPolicy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class BackupIndex {

	private Directory directory;
	private IndexDeletionPolicy deletePolicy;
	private SnapshotDeletionPolicy snapshotter;
	private IndexWriter writer;
	
	public void backup(String indexDir, Analyzer analyzer) throws Exception {
		this.directory = FSDirectory.open(Paths.get(indexDir));
		this.writer = getWriter(analyzer);
		IndexCommit commit = null;
		
		try {
			System.out.println("Started Backup ... ");
			commit = this.snapshotter.snapshot();
			Collection<String> fileNames = commit.getFileNames();
			
			/* <iterate over & copy files from fileNames> */
			for(String fileName : fileNames) {
				System.out.println("Backing up file: " + fileName);
			}
			Thread.sleep(20000);
		} finally {
			this.snapshotter.release(commit);
			System.out.println("Finished Backup ... ");
		}
	}

	private IndexWriter getWriter(Analyzer analyzer) throws Exception {
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

		// commit point的默认删除机制，segments_N file delete policy
		this.deletePolicy = new KeepOnlyLastCommitDeletionPolicy();
		this.snapshotter = new SnapshotDeletionPolicy(deletePolicy);
		iwc.setIndexDeletionPolicy(snapshotter);
		System.out.println("IndexDeletionPolicy: " + iwc.getIndexDeletionPolicy());

		IndexWriter writer = new IndexWriter(directory, iwc);
		return writer;
	}

}
