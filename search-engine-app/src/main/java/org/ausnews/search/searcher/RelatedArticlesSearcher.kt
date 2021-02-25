package org.ausnews.search.searcher

import com.google.inject.Inject
import com.yahoo.language.Linguistics
import com.yahoo.documentapi.DocumentAccess
import com.yahoo.documentapi.SyncParameters
import com.yahoo.document.DocumentId
import com.yahoo.language.Language
import com.yahoo.prelude.query.WeakAndItem
import com.yahoo.prelude.query.WordItem
import com.yahoo.prelude.query.AndItem
import com.yahoo.language.process.StemMode
import com.yahoo.language.process.Token
import com.yahoo.language.process.TokenType
import com.yahoo.search.Query
import com.yahoo.search.Result
import com.yahoo.search.Searcher
import com.yahoo.search.searchchain.Execution
import org.ausnews.search.processor.ArticleProcessor
import java.util.logging.Logger

class RelatedArticlesSearcher @Inject constructor(
    private val linguistics: Linguistics,
    private val documentAccess: DocumentAccess
) : Searcher() {
    override fun search(query: Query, execution: Execution): Result? {
        val relatedArticleId = query.properties().getString("id")
        var article: Article? = null;
        relatedArticleId?.let {
            article = fetchArticle(relatedArticleId)
        }
        val headline = query.properties().getString("headline")
        val summary = query.properties().getString("abstract")
        headline?.let {
            article = Article(headline, summary)
        }
        if (article != null) {
            addWeakAndItem(article!!, query)
            val r = execution.search(query)
            return r
        }
        return null
    }

    private fun fetchArticle(id: String): Article? {
        val session = documentAccess.createSyncSession(SyncParameters.Builder().build())
        val d = session[DocumentId(id)]
        if (d == null) {
            logger.severe("Couldn't get document $id")
            return null
        }
        var headline: String? = null
        var summary: String? = null
        if (d.getFieldValue(d.getField("headline")) != null) {
            headline = d.getFieldValue(d.getField("headline")).toString()
        }
        if (d.getFieldValue(d.getField("abstract")) != null) {
            summary = d.getFieldValue(d.getField("abstract")).toString()
        }
        session.destroy()
        return Article(headline, summary)
    }

    private fun addWeakAndItem(article: Article, query: Query) {
        val weakAndItem = WeakAndItem()
        if (article.headline != null) {
            for (t in tokenize(article.headline)) {
                if (!stopwords.contains(t.tokenString) &&
                    (t.type == TokenType.ALPHABETIC || t.type == TokenType.NUMERIC)
                ) {
                    val tokenItem = WordItem(t.tokenString, "default", true)
                    tokenItem.weight = 200
                    weakAndItem.addItem(tokenItem)
                }
            }
        }
        if (article.articleAbstract != null) {
            for (t in tokenize(article.articleAbstract)) {
                if (!stopwords.contains(t.tokenString) && (t.type == TokenType.ALPHABETIC || t.type == TokenType.NUMERIC)) {
                    val tokenItem = WordItem(t.tokenString, "default", true)
                    tokenItem.weight = 100
                    weakAndItem.addItem(tokenItem)
                }
            }
        }

        // Combine
        val root = query.model.queryTree.root
        val andItem = AndItem()
        andItem.addItem(root)
        andItem.addItem(weakAndItem)
        query.model.queryTree.root = andItem
    }

    private fun tokenize(data: String?): Iterable<Token> {
        val tokenizer = linguistics.tokenizer
        return tokenizer.tokenize(
            data,
            Language.ENGLISH, StemMode.NONE, true
        )
    }

    private class Article internal constructor(val headline: String?, val articleAbstract: String?)
    companion object {
        const val summary = "full"
        private val logger = Logger.getLogger(ArticleProcessor::class.java.name)
        protected var stopwords = mutableSetOf(
            "i",
            "me",
            "my",
            "myself",
            "we",
            "our",
            "ours",
            "ourselves",
            "you",
            "your",
            "yours",
            "yourself",
            "yourselves",
            "he",
            "him",
            "his",
            "himself",
            "she",
            "her",
            "hers",
            "herself",
            "it",
            "its",
            "itself",
            "they",
            "them",
            "their",
            "theirs",
            "themselves",
            "what",
            "which",
            "who",
            "whom",
            "this",
            "that",
            "these",
            "those",
            "am",
            "is",
            "are",
            "was",
            "were",
            "be",
            "been",
            "being",
            "have",
            "has",
            "had",
            "having",
            "do",
            "does",
            "did",
            "doing",
            "a",
            "an",
            "the",
            "and",
            "but",
            "if",
            "or",
            "because",
            "as",
            "until",
            "while",
            "of",
            "at",
            "by",
            "for",
            "with",
            "about",
            "against",
            "between",
            "into",
            "through",
            "during",
            "before",
            "after",
            "above",
            "below",
            "to",
            "from",
            "up",
            "down",
            "in",
            "out",
            "on",
            "off",
            "over",
            "under",
            "again",
            "further",
            "then",
            "once",
            "here",
            "there",
            "when",
            "where",
            "why",
            "how",
            "all",
            "any",
            "both",
            "each",
            "few",
            "more",
            "most",
            "other",
            "some",
            "such",
            "no",
            "nor",
            "not",
            "only",
            "own",
            "same",
            "so",
            "than",
            "too",
            "very",
            "can",
            "will",
            "just",
            "don",
            "should",
            "now"
        )
    }
}