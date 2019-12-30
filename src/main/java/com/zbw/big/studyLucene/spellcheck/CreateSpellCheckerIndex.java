package com.zbw.big.studyLucene.spellcheck;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class CreateSpellCheckerIndex {
	public static void main(String[] args) throws IOException {
		if (args.length != 3) {
			System.out.println("Usage: java com.zbw.big.studyLucene.CreateSpellCheckerIndex " + "SpellCheckerIndexDir IndexDir IndexField");
			System.exit(1);
		}
		String spellCheckDir = args[0];
		String indexDir = args[1];
		String indexField = args[2];
		
		System.out.println("Now building SpellChecker index...");
		Directory dir = FSDirectory.open(Paths.get(spellCheckDir));
		SpellChecker spell = new SpellChecker(dir);
		long startTime = System.currentTimeMillis();
		
		Directory dir2 = FSDirectory.open(Paths.get(indexDir));
		IndexReader r = DirectoryReader.open(dir2);
		
		IndexWriterConfig iwc = new IndexWriterConfig(new StandardAnalyzer());
		
		try {
			spell.indexDictionary(new LuceneDictionary(r, indexField), iwc, true);
		} finally {
			r.close();
		}
		dir.close();
		dir2.close();
		long endTime = System.currentTimeMillis();
		System.out.println(" took " + (endTime - startTime) + " milliseconds");
	}
}
