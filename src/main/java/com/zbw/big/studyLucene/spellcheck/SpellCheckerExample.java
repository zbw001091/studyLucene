package com.zbw.big.studyLucene.spellcheck;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.search.spell.LevenshteinDistance;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class SpellCheckerExample {
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.println("Usage: java com.zbw.big.studyLucene.CreateSpellCheckerIndex " + "SpellCheckerIndexDir wordToRespell");
			System.exit(1);
		}
		String spellCheckDir = args[0];
		String wordToRespell = args[1];
		
		Directory dir = FSDirectory.open(Paths.get(spellCheckDir));
//		if (!IndexReader.indexExists(dir)) {
//		System.out.println("\nERROR: No spellchecker index at path \"" +
//		spellCheckDir +
//		"\"; please run CreateSpellCheckerIndex first\n");
//		System.exit(1);
//		}
		SpellChecker spell = new SpellChecker(dir);
		spell.setStringDistance(new LevenshteinDistance());
		String[] suggestions = spell.suggestSimilar(wordToRespell, 5);
		System.out.println(suggestions.length + " suggestions for '" + wordToRespell + "':");
		for (String suggestion : suggestions)
			System.out.println(" " + suggestion);
	}
}
