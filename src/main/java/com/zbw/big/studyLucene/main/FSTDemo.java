package com.zbw.big.studyLucene.main;

import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IntsRefBuilder;
import org.apache.lucene.util.fst.Builder;
import org.apache.lucene.util.fst.FST;
import org.apache.lucene.util.fst.PositiveIntOutputs;
import org.apache.lucene.util.fst.Util;

public class FSTDemo {

	public static void main(String[] args) {
		try {
			String inputValues[] = { "cat", "deep", "do", "dog", "dogs" };
			long outputValues[] = { 5, 7, 17, 18, 21 };
			PositiveIntOutputs outputs = PositiveIntOutputs.getSingleton();
			Builder<Long> builder = new Builder<Long>(FST.INPUT_TYPE.BYTE1, outputs);
			BytesRef scratchBytes = null;
			IntsRefBuilder scratchInts = new IntsRefBuilder();
			for (int i = 0; i < inputValues.length; i++) {
//				scratchBytes.copyChars(inputValues[i]);
				scratchBytes = new BytesRef(inputValues[i]);
				builder.add(Util.toIntsRef(scratchBytes, scratchInts), outputValues[i]);
			}
			FST<Long> fst = builder.finish();
			Long value = Util.get(fst, new BytesRef("dog"));
			System.out.println(value); // 18
		} catch (Exception e) {
			;
		}
	}

}
