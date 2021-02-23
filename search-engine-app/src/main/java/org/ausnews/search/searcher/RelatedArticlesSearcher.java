package org.ausnews.search.searcher;

import com.google.inject.Inject;
import com.yahoo.document.DocumentId;
import com.yahoo.document.Document;
import com.yahoo.documentapi.DocumentAccess;
import com.yahoo.documentapi.SyncParameters;
import com.yahoo.documentapi.SyncSession;
import com.yahoo.language.Language;
import com.yahoo.language.Linguistics;
import com.yahoo.prelude.query.WeakAndItem;
import com.yahoo.prelude.query.WordItem;
import com.yahoo.search.Query;
import com.yahoo.search.Result;
import com.yahoo.search.Searcher;
import com.yahoo.search.result.Hit;
import com.yahoo.search.searchchain.Execution;
import com.yahoo.language.process.StemMode;
import com.yahoo.language.process.Token;
import com.yahoo.language.process.TokenType;
import com.yahoo.language.process.Tokenizer;
import com.yahoo.prelude.query.AndItem;
import com.yahoo.prelude.query.Item;
import com.yahoo.prelude.query.WeakAndItem;
import com.yahoo.prelude.query.WordItem;

import java.util.Set;

public class RelatedArticlesSearcher extends Searcher {
    public static final String summary = "full";

    private final Linguistics linguistics;
    private final DocumentAccess documentAccess;

    @Inject
    public RelatedArticlesSearcher(Linguistics linguistics, DocumentAccess documentAccess) {
        this.linguistics = linguistics;
        this.documentAccess = documentAccess;
    }


    @Override
    public Result search(Query query, Execution execution) {
        String relatedArticleId = query.properties().getString("id");
        Article article = fetchArticle(relatedArticleId, execution, query);
        addWeakAndItem(article, query);
        return execution.search(query);
    }

    private Article fetchArticle(String id, Execution execution, Query query) {
        SyncSession session = this.documentAccess.createSyncSession(new SyncParameters.Builder().build());
        Document d = session.get(new DocumentId((id)));
        session.destroy();
        String headline = null;
        String summary = null;
        if (d.getField("headline") != null) {
            headline = d.getField("headline").toString();
        }
        if (d.getField("abstract") != null) {
            summary = d.getField("abstract").toString();
        }
        if (headline != null && summary != null) {
            return new Article(headline, summary);
        } else {
            return null;
        }
    }

    private void addWeakAndItem(Article article, Query query) {
        WeakAndItem weakAndItem = new WeakAndItem();
        if (article.headline != null) {
            for (Token t : tokenize(article.headline)) {
                if (!stopwords.contains((t.getTokenString())) &&
                        (t.getType() == TokenType.ALPHABETIC || t.getType() == TokenType.NUMERIC)) {
                    WordItem tokenItem = new WordItem(t.getTokenString(), "default", true);
                    tokenItem.setWeight(200);
                    weakAndItem.addItem(tokenItem);
                }
            }
        }
        if (article.articleAbstract != null)  {
            for (Token t : tokenize(article.articleAbstract)) {
                if (!stopwords.contains((t.getTokenString())) && (t.getType() == TokenType.ALPHABETIC || t.getType() == TokenType.NUMERIC)) {
                    WordItem tokenItem = new WordItem(t.getTokenString(), "default", true);
                    tokenItem.setWeight(100);
                    weakAndItem.addItem(tokenItem);
                }
            }
        }

        // Combine
        Item root = query.getModel().getQueryTree().getRoot();
        AndItem andItem = new AndItem();
        andItem.addItem(root);
        andItem.addItem(weakAndItem);
        query.getModel().getQueryTree().setRoot(andItem);
    }

    private Iterable<Token> tokenize(String data) {
        Tokenizer tokenizer = this.linguistics.getTokenizer();
        return tokenizer.tokenize(data,
                Language.ENGLISH, StemMode.NONE, true);
    }

    protected static Set<String> stopwords = Set.of(
            "i", "me", "my", "myself", "we", "our", "ours", "ourselves",
            "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers",
            "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom",
            "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had",
            "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while",
            "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above",
            "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there",
            "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not",
            "only", "own", "same", "so", "than", "too", "very", "can", "will", "just", "don", "should", "now");

    private static class Article {
        final String headline;
        final String articleAbstract;

        Article(String headline, String articleAbstract) {
            this.headline = headline;
            this.articleAbstract = articleAbstract;
        }
    }
}
