package com.zbw.big.studyLucene.searcher;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PointRangeQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanFirstQuery;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class DocumentSearcher extends BaseSearcher {

	@Override
	public Query getAQuery() throws ParseException, Exception {
		// 【1.1】TermQuery
	    Query queryByMachine = new TermQuery(new Term("contents", "01_camel"));
	    Query termQuery = new TermQuery(new Term("booknameString", "0bdOFS 7pVGGY"));
	    
	    // 【1.2】PhraseQuery
	    PhraseQuery.Builder phraseQueryBuilder = new PhraseQuery.Builder();
	    phraseQueryBuilder.add(new Term("content", "quick"), 4);
	    phraseQueryBuilder.add(new Term("content", "dog"), 5);
	    phraseQueryBuilder.setSlop(2);
	    PhraseQuery phraseQuery = phraseQueryBuilder.build();
	    
	    // 【1.3】MultiPhraseQuery, this is an advanced version of PhraseQuery
	    MultiPhraseQuery.Builder multiPhraseQueryBuilder = new MultiPhraseQuery.Builder();
	    Term term1 = new Term("content", "jumps");
	    Term term2 = new Term("content", "hops");
	    Term[] terms = {term1, term2};
	    multiPhraseQueryBuilder.add(new Term("content", "quick"));
	    multiPhraseQueryBuilder.add(terms); // put term[], with several terms at the same position
	    multiPhraseQueryBuilder.setSlop(2);
	    MultiPhraseQuery multiPhraseQuery = multiPhraseQueryBuilder.build();
	    
	    // 【1.4】程序构建MatchAllQuery
	    Query matchallquery = new MatchAllDocsQuery();
	    
	    // 【1.5】程序构建termQuery，term大于以c开头，小于以m开头，搜出docID list
	    TermRangeQuery termRangeQuery = new TermRangeQuery("bookname", new BytesRef("c"), new BytesRef("m"), true, true);
	    
	    PointRangeQuery newRangeQuery = (PointRangeQuery)IntPoint.newRangeQuery("publishyear", 2018, 2019);
	    
	    PointRangeQuery newExactQuery = (PointRangeQuery)IntPoint.newExactQuery("publishyear", 2018);
	    
	    PrefixQuery prefixQuery2 = new PrefixQuery(new Term("content", "jump"));
	    PrefixQuery prefixQuery = new PrefixQuery(new Term("author", "stock"));
	    
	    Query wildcardQuery = new WildcardQuery(new Term("author", "*it*"));
	    
	    Query fuzzyQuery = new FuzzyQuery(new Term("author", "smh")); //smth fuzzy with smith
	    
	    SpanTermQuery spanTermQuery = new SpanTermQuery(new Term("content", "jumps"));
	    
	    SpanFirstQuery spanFirstQuery = new SpanFirstQuery(new SpanTermQuery(new Term("content", "jumps")), 6);
	    
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
	    
	    SpanQuery[] over_quick_fox = new SpanQuery[]{over, quick, fox};
	    SpanNearQuery spanNearQueryReverse = new SpanNearQuery(over_quick_fox, 2, false);
	    
	    // 【2.1】前端页面用户输入的查询条件，构建Query
	    // 需要对Human输入的查询条件，先做一次analyze（分词/lowercase）
	    QueryParser parser = new QueryParser("contents", new StandardAnalyzer()); //SmartChineseAnalyzer
	    Query queryByHuman = parser.parse("mdosdco07");
//	    Query queryByHuman = parser.parse("+facebook -MOCK");
	    
	    // 【2.2】search a term in multiple fields, rather than only in one field 
	    Query multiFieldQueryParser = MultiFieldQueryParser.parse("fGy050",
										    		new String[]{"contents", "bookname"},
										    		new BooleanClause.Occur[]{BooleanClause.Occur.SHOULD,
										    		BooleanClause.Occur.SHOULD},
										    		new SimpleAnalyzer());
	    
	    Query multiFieldDefaultQueryParser = new MultiFieldQueryParser(new String[] {"contents", "author"},
	    										new SimpleAnalyzer()).parse("lebron");
	    
	    // customize scoring algorithm
	    // compile an expression:
	    Expression expr = JavascriptCompiler.compile("_score * ln(bookNo)");
	    // SimpleBindings just maps variables to SortField instances
	    SimpleBindings bindings = new SimpleBindings();
	    bindings.add(new SortField("_score", SortField.Type.SCORE));
	    bindings.add(new SortField("bookNo", SortField.Type.INT));
	    // create a query that matches based on 'originalQuery' but
	    // scores using expr
	    Query functionScoreQuery = new FunctionScoreQuery(fuzzyQuery, expr.getDoubleValuesSource(bindings));
	    
	    return multiPhraseQuery;
	}

}
