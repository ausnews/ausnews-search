package web.api

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import java.time.Instant

@Client("\${vespa.url}")
@ExecuteOn(TaskExecutors.IO)
interface SearchClient {
    @Get("/search/")
    public fun search(
        @QueryValue yql: String,
        @QueryValue q: String,
        @QueryValue b: String?,
        @QueryValue s: String?,
        @QueryValue summary: String = "default",
        @QueryValue ranking: String = "bm25_freshness",
        @QueryValue hits: Int = 100,
        @QueryValue r: String?,
        @QueryValue e: String?,
        @QueryValue("presentation.timing") timing: String = "true",
        @QueryValue select: String = "all(all(group(source) order(-count()) each(output(count()))))",
        @QueryValue("ranking.features.query(twitterWeight)") twitterWeight: Float?,
        @QueryValue("ranking.features.query(twitterRetweet)") twitterRetweetWeight: Float?,
        @QueryValue("ranking.features.query(twitterFavourite)") twitterFavouriteWeight: Float?,
        @QueryValue("ranking.features.query(freshnessWeight)") freshnessWeight: Float?
    ): SearchResponse

    @Get("/search/")
    public fun related(@QueryValue id: String,
                       @QueryValue("presentation.timing") timing: String = "true",
                       @QueryValue searchChain: String = "related"): SearchResponse

    // ?presentation.timing=true&select=all(group(group_doc_id)++order(-count())+each(max(3)+each(output(summary()))))&hits=0&ranking.profile=twitter&yql=select+*+from+sources+newsarticle+WHERE+firstpubtime+%3E+1614311914+and+group_doc_id+matches+%22%5Eid%22%3B
    @Get("/search/")
    public fun topics(
        @QueryValue yql: String = "select * from sources newsarticle WHERE firstpubtime > @firstpubtime and group_doc_id matches \"^id\" and twitter_favourite_count > 0;",
        @QueryValue select: String = "all(group(group_doc_id) max(15) order(-sum(twitter_favourite_count)) each(max(3) each(output(summary()))))",
        @QueryValue("presentation.timing") timing: String = "true",
        @QueryValue hits: String = "0",
        @QueryValue firstpubtime: Long = Instant.now().epochSecond - 86400,
        @QueryValue("ranking.profile") ranking: String = "twitter"): SearchResponse
}

data class SearchResponse(val root: SearchRootElement, val timing: Map<String, Any>) {}

data class SearchRootElement(val id: String, val relevance: Int, val children: Array<SearchResultElement>?) { }

data class SearchResultElement(val id: String, val relevance: Float, val children: Map<String, Any>?, val source: String?, val fields: SearchResultFields?) { }

data class SearchResultFields(val sddocname: String, val bodytext: String, val documentid: String,
                              val headline: String?, val url: String, val keywords: Array<String>?,
    val firstpubtime: Long?, val modtime: Long?, val sentiment: Float?, val wordcount: Int?, val abstract: String?, val bylines: Array<String>?,
    val source: String?, val twitter_retweet_count: Int?, val twitter_favourite_count: Int?, val twitter_link: String?, val group_doc_id: String?,
    val summaryfeatures: Map<String, Any>?) {}