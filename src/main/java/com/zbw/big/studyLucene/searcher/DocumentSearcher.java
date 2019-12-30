package com.zbw.big.studyLucene.searcher;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.expressions.Expression;
import org.apache.lucene.expressions.SimpleBindings;
import org.apache.lucene.expressions.js.JavascriptCompiler;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.function.FunctionScoreQuery;
import org.apache.lucene.queries.mlt.MoreLikeThis;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.DisjunctionMaxQuery;
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

public class DocumentSearcher extends BaseSearcher {

	@Override
	public Query getAQuery(IndexReader reader) throws ParseException, Exception {
		// 【1.1】TermQuery
	    Query queryByMachine = new TermQuery(new Term("contents", "01_camel"));
	    Query termQuery = new TermQuery(new Term("booknameString", "0bdOFS 7pVGGY"));
	    Query termQueryByStemWord = new TermQuery(new Term("content", "jump")); //原文是jumps，被stem成jump，这里用jump来搜索
	    Query termQueryByFrequency = new TermQuery(new Term("content", "fox")); //有2篇文档，1篇有1个fox，1篇有2个fox，按照fox搜，score应该有高低之分
	    
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
	    
	    // 【1.4】MatchAllQuery
	    Query matchallquery = new MatchAllDocsQuery();
	    
	    // 【1.5】TermRangeQuery，term大于以c开头，小于以m开头，搜出docID list
	    TermRangeQuery termRangeQuery = new TermRangeQuery("bookname", new BytesRef("c"), new BytesRef("m"), true, true);
	    
	    // 【1.6】Number/Numeric query
	    PointRangeQuery numberRangeQuery = (PointRangeQuery)IntPoint.newRangeQuery("publishyear", 2018, 2019);
	    PointRangeQuery numberExactQuery = (PointRangeQuery)IntPoint.newExactQuery("publishyear", 2012);
	    Query numberSetQuery = (Query)IntPoint.newSetQuery("publishyear", 2012, 2017);
	    
	    PrefixQuery prefixQuery2 = new PrefixQuery(new Term("content", "jump"));
	    PrefixQuery prefixQuery = new PrefixQuery(new Term("author", "stock"));
	    
	    Query wildcardQuery = new WildcardQuery(new Term("author", "*it*"));
	    
	    Query fuzzyQuery = new FuzzyQuery(new Term("content", "jmps")); //smth fuzzy with smith //jmps fuzzy with jumps
	    
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
	    
//	    MoreLikeThisQuery moreLikeThisQuery =  new MoreLikeThisQuery("likeText", new String[] {"title", "author"}, new StandardAnalyzer(), "contents");
	    
	    // extract termVector from one doc(for example, docId=0), build BooleanQuery, search in the reader scope, to find similar docs with document(docID=0) 
	    MoreLikeThis mlt = new MoreLikeThis(reader); // set the range in which mlt will be searched
	    mlt.setFieldNames(new String[] {"contents", "author"});
	    mlt.setMinTermFreq(1);
	    mlt.setMinDocFreq(1);
	    Query mltQuery = mlt.like(0); // set docId=0, search documents like this(docId=0)
	    
	    /**
	         * 在所有document中，按score从高到低的排名:
	         * 虽然是DisjunctionMaxQuery，但term在同1个field里的score，肯定比term拆散分散在多个field里的score，要高
	         * 有1个field含有brown pig，另1个field没有任何brown或者pig也无所谓
	         * 有2个field都有pig，但都没有brown
	         * 有1个field有brown，另1个field有pig
	         * 只有1个field有brown
	     */
	    Query termQueryDisjunction1 = new QueryParser("content", new StandardAnalyzer()).parse("brown pig");
	    Query termQueryDisjunction2 = new QueryParser("contents", new StandardAnalyzer()).parse("brown pig");
	    List<Query> termQueryDisjunction = new ArrayList<Query>();
	    termQueryDisjunction.add(termQueryDisjunction1);
	    termQueryDisjunction.add(termQueryDisjunction2);
	    DisjunctionMaxQuery disjunctionMaxQuery = new DisjunctionMaxQuery(termQueryDisjunction, 0.1f);
	    
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
	    
	    return numberSetQuery;
	}

}
