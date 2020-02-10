package org.lukhnos.lucenestudy;

/**
 * Copyright (c) 2015 Lukhnos Liu
 *
 * Licensed under the MIT License.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Searcher implements AutoCloseable {
  final Analyzer analyzer;
  final IndexReader indexReader;
  final IndexSearcher indexSearcher;

  public Searcher(String indexRoot) throws IOException {
    Path indexRootPath = Paths.get(indexRoot);
    analyzer = Indexer.getAnalyzer();
    Directory mainIndexDir = FSDirectory.open(Indexer.getMainIndexPath(indexRootPath));
    indexReader = DirectoryReader.open(mainIndexDir);
    indexSearcher = new IndexSearcher(indexReader);
  }

  public SearchResult search(String queryStr, int maxCount) throws ParseException, IOException {
    return search(queryStr, null, maxCount);
  }

  public SearchResult search(String queryStr, SortBy sortBy, int maxCount)
      throws ParseException, IOException {
    Query query = Indexer.parseQuery(analyzer, queryStr);

    Sort sort = null;
    if (sortBy != null) {
      sort = sortBy.sort;
    }

    return searchAfter(null, query, sort, maxCount);
  }

  public SearchResult searchAfter(SearchResult result, int maxCount) throws IOException {
    if (!result.hasMore()) {
      throw new AssertionError("No more search results to be fetched after this");
    }

    return searchAfter(result.lastScoreDoc, result.query, result.sort, maxCount);
  }

  @Override
  public void close() throws Exception {
    indexReader.close();
  }

  SearchResult searchAfter(ScoreDoc lastScoreDoc, Query query, Sort sort, int maxCount)
      throws IOException {
    if (maxCount < 1) {
      throw new AssertionError("maxCount must be at least 1, but instead: " + maxCount);
    }

    TopDocs topDocs;
    int actualMaxCount = maxCount + 1;
    if (lastScoreDoc == null) {
      if (sort == null) {
        topDocs = indexSearcher.search(query, actualMaxCount);
      } else {
        topDocs = indexSearcher.search(query, actualMaxCount, sort);
      }
    } else {
      if (sort == null) {
        topDocs = indexSearcher.searchAfter(lastScoreDoc, query, actualMaxCount);
      } else {
        topDocs = indexSearcher.searchAfter(lastScoreDoc, query, actualMaxCount, sort);
      }
    }

    ScoreDoc nextSearchAfterDoc = null;
    int topDocsLen;
    if (topDocs.scoreDocs.length > maxCount) {
      nextSearchAfterDoc = topDocs.scoreDocs[maxCount - 1];
      topDocsLen = maxCount;
    } else {
      topDocsLen = topDocs.scoreDocs.length;
    }

    HighlightingHelper highlightingHelper = new HighlightingHelper(query, analyzer);

    List<Document> docs = new ArrayList<>();
    for (int i = 0; i < topDocsLen; i++) {
      org.apache.lucene.document.Document luceneDoc = indexReader.document(topDocs.scoreDocs[i].doc);
      Document doc = Indexer.fromLuceneDocument(luceneDoc);
      docs.add(doc);
    }

    return new SearchResult((int)topDocs.totalHits.value, docs, nextSearchAfterDoc, query, sort, highlightingHelper);
  }

  public enum SortBy {
    RELEVANCE(Sort.RELEVANCE),
    DOCUMENT_ORDER(Sort.INDEXORDER),
    TITLE(new Sort(new SortField(Indexer.TITLE_FIELD_NAME, SortField.Type.STRING))),
    YEAR(new Sort(new SortField(Indexer.YEAR_FIELD_NAME, SortField.Type.INT, true))),
    RATING(new Sort(new SortField(Indexer.RATING_FIELD_NAME, SortField.Type.INT, true)));

    final Sort sort;
    SortBy(Sort sort) {
      this.sort = sort;
    }
  }
}
