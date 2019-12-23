package com.zbw.big.studyLucene.analyzer.synonym;

import java.util.HashMap;

public class SynonymEngineImpl implements SynonymEngine {
	private static HashMap<String, String[]> map = new HashMap<String, String[]>();
	static {
		map.put("quick", new String[] { "fast", "speedy" });
		map.put("jumps", new String[] { "leaps", "hops" });
		map.put("over", new String[] { "above" });
		map.put("lazy", new String[] { "apathetic", "sluggish" });
		map.put("dog", new String[] { "canine", "pooch" });
	}

	public String[] getSynonyms(String s) {
		return map.get(s);
	}
}
