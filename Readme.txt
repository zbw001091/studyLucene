1. customized SynonymAnalyzer, used in 2 scenario:
1.1 used in AnalyzerUtilsFacade.class to test on a string, just test on a string, no indexing and querying.
1.2 used to index a string, and then user termQuery and phraseQuery to try to search this doc.

2. add spellcheck package, use ngram for suggestion
