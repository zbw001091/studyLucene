package com.zbw.big.studyLucene.analyzer;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

public class AnalyzerUtils {
	
	// get tokenStream based on analyzer
	public static void displayTokens(Analyzer analyzer, String text) throws IOException {
		displayTokens(analyzer.tokenStream("contents", new StringReader(text)));
	}
	
	// use tokenStream to iterate each token
	public static void displayTokens(TokenStream tokenStream) throws IOException {
		// 添加一个引用，可以获得每个关键词
		CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
		
		// 添加一个引用，可以获得每个关键词与前一个关键词之间的position increment
		PositionIncrementAttribute posIncrAttribute = tokenStream.addAttribute(PositionIncrementAttribute.class);
		
		// 添加一个偏移量的引用，记录了关键词的开始位置以及结束位置
        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        
        // 添加一个type引用
        TypeAttribute type = tokenStream.addAttribute(TypeAttribute.class);
        
        // 将指针调整到列表的头部
        tokenStream.reset();
        
        int position = 0;
		while (tokenStream.incrementToken()) {
			int increment = posIncrAttribute.getPositionIncrement();
			if (increment > 0) {
				position = position + increment;
				System.out.print(position + ": ");
			}
			
			System.out.print("[" + charTermAttribute + "]: ");
			
			// term起始位置
            System.out.print("start->" + offsetAttribute.startOffset() + ", ");
			// term结束位置
            System.out.print("end->" + offsetAttribute.endOffset() + ": ");
            System.out.print("type->" + type.type());
            System.out.println();
		}
		
		tokenStream.close();
	}
	
}
