package com.zbw.big.studyLucene.searcher.slowsearch;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.BitSet;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.search.ScoreMode;
import org.apache.lucene.search.SimpleCollector;
import org.apache.lucene.search.TimeLimitingCollector;
import org.apache.lucene.search.TimeLimitingCollector.TimeExceededException;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Counter;
import org.apache.lucene.util.ThreadInterruptedException;

public class SlowSearch {
	private Counter counter = Counter.newCounter(true);
	
	public void normalSearch(String indexDir) throws IOException {
		DirectoryReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
		IndexSearcher searcher = new IndexSearcher(indexReader);
		Query query = new MatchAllDocsQuery();
//		TopScoreDocCollector topDocs = TopScoreDocCollector.create(10, 5);
		MyHitCollector myHc = new MyHitCollector();
		Collector collector = new TimeLimitingCollector(myHc, this.counter, 1);
		try {
			searcher.search(query, collector);
			System.out.println(myHc.hitCount());
		} catch (TimeExceededException tee) {
			System.out.println("Too much time taken.");
		}
		indexReader.close();
	}
	
	public void slowSearch(String indexDir) throws IOException {
		DirectoryReader indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
		IndexSearcher searcher = new IndexSearcher(indexReader);
		Query query = new MatchAllDocsQuery();
		MyHitCollector myHc = new MyHitCollector();
		myHc.setSlowDown(100);
		Collector collector = new TimeLimitingCollector(myHc, this.counter, 1);
		try {
			searcher.search(query, collector);
			System.out.println(myHc.hitCount());
		} catch (TimeExceededException tee) {
			System.out.println("Too much time taken.");
		}
		indexReader.close();
	}
	
	private static class MyHitCollector extends SimpleCollector {
	    private final BitSet bits = new BitSet();
	    private int slowdown = 0;
	    private int lastDocCollected = -1;
	    private int docBase = 0;

	    /**
	     * amount of time to wait on each collect to simulate a long iteration
	     */
	    public void setSlowDown( int milliseconds ) {
	      slowdown = milliseconds;
	    }
	    
	    public int hitCount() {
	      return bits.cardinality();
	    }

	    public int getLastDocCollected() {
	      return lastDocCollected;
	    }

	    @Override
	    public void setScorer(Scorable scorer) throws IOException {
	      // scorer is not needed
	    }
	    
	    @Override
	    public void collect(final int doc) throws IOException {
	      int docId = doc + docBase;
	      if( slowdown > 0 ) {
	        try {
	          Thread.sleep(slowdown);
	        } catch (InterruptedException ie) {
	          throw new ThreadInterruptedException(ie);
	        }
	      }
	      assert docId >= 0: " base=" + docBase + " doc=" + doc;
	      bits.set( docId );
	      lastDocCollected = docId;
	    }
	    
	    @Override
	    protected void doSetNextReader(LeafReaderContext context) throws IOException {
	      docBase = context.docBase;
	    }
	    
	    @Override
	    public ScoreMode scoreMode() {
	      return ScoreMode.COMPLETE;
	    }

	  }

}
