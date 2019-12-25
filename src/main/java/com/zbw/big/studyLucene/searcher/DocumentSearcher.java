package com.zbw.big.studyLucene.searcher;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PointRangeQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.BytesRef;

public class DocumentSearcher extends BaseSearcher {

	@Override
	public Query getAQuery() throws ParseException {
		// 【1.1】程序构建Query
	    Query queryByMachine = new TermQuery(new Term("contents", "01_camel"));
	    
	    // 【1.2】程序构建PhraseQuery
	    PhraseQuery.Builder builder = new PhraseQuery.Builder();
	    builder.add(new Term("title", "中文"), 4);
	    builder.add(new Term("title", "跳槽"), 5);
	    builder.setSlop(8);
	    PhraseQuery phraseQuery = builder.build();
	    
	    // 【1.3】程序构建MatchAllQuery
	    Query matchallquery = new MatchAllDocsQuery();
	    
	    // 【1.4】程序构建termQuery，term大于以c开头，小于以m开头，搜出docID list
	    TermRangeQuery termRangeQuery = new TermRangeQuery("bookname", new BytesRef("c"), new BytesRef("m"), true, true);
	    
	    PointRangeQuery newRangeQuery = (PointRangeQuery)IntPoint.newRangeQuery("publishyear", 2018, 2019);
	    
	    PointRangeQuery newExactQuery = (PointRangeQuery)IntPoint.newExactQuery("publishyear", 2018);
	    
	    PrefixQuery prefixQuery2 = new PrefixQuery(new Term("content", "jump"));
	    PrefixQuery prefixQuery = new PrefixQuery(new Term("author", "stock"));
	    
	    Query wildcardQuery = new WildcardQuery(new Term("author", "*it*"));
	    
	    Query fuzzyQuery = new FuzzyQuery(new Term("author", "smh")); //smth fuzzy with smith
	    
	    SpanTermQuery spanTermQuery = new SpanTermQuery(new Term("content", "jumps"));
	    
	    SpanFirstQuery spanFirstQuery = new SpanFirstQuery(new SpanTermQuery(new Term("content", "jumps")), 5);
	    
	    SpanTermQuery quick = new SpanTermQuery(new Term("content", "quick"));
	    SpanTermQuery brown = new SpanTermQuery(new Term("content", "brown"));
	    SpanTermQuery fox = new SpanTermQuery(new Term("content", "fox"));
	    SpanTermQuery jumps = new SpanTermQuery(new Term("content", "jumps"));
	    SpanTermQuery over = new SpanTermQuery(new Term("content", "over"));
	    SpanTermQuery lazy = new SpanTermQuery(new Term("content", "lazy"));
	    SpanTermQuery dog = new SpanTermQuery(new Term("content", "dog"));
	    SpanQuery[] quick_brown_dog = new SpanQuery[]{quick, brown, dog};
	    SpanNearQuery spanNearQuery1 = new SpanNearQuery(quick_brown_dog, 5, true);
	    
	    SpanQuery[] quick_fox_over = new SpanQuery[]{quick, fox, over};
	    SpanNearQuery spanNearQuery2 = new SpanNearQuery(quick_fox_over, 2, true);
	    
	    SpanQuery[] over_quick_fox = new SpanQuery[]{over, dog};
	    SpanNearQuery spanNearQueryReverse = new SpanNearQuery(over_quick_fox, 6, false);
	    
	    // 【2】前端页面用户输入的查询条件，构建Query
	    // 需要对Human输入的查询条件，先做一次analyze（分词/lowercase）
	    QueryParser parser = new QueryParser("contents", new StandardAnalyzer()); //SmartChineseAnalyzer
	    Query queryByHuman = parser.parse("mdosdco07");
//	    Query queryByHuman = parser.parse("+facebook -MOCK");
	    
	    return fuzzyQuery;
	}

}
