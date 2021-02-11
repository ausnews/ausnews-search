package web.api

import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn

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
        @QueryValue select: String = "all(all(group(source) order(-count()) each(output(count()))))"
    ): SearchResponse

    @Get("/search/")
    public fun related(@QueryValue id: String,
        @QueryValue searchChain: String = "related"): SearchResponse
}

data class SearchResponse(val root: SearchRootElement) {}

data class SearchRootElement(val id: String, val relevance: Int, val children: Array<SearchResultElement>?) { }

data class SearchResultElement(val id: String, val relevance: Float, val children: Map<String, Any>?, val source: String?, val fields: SearchResultFields?) { }

data class SearchResultFields(val sddocname: String, val bodytext: String, val documentid: String,
                              val headline: String?, val url: String, val keywords: Array<String>?,
    val firstpubtime: Long?, val modtime: Long?, val sentiment: Float?, val wordcount: Int?, val abstract: String?, val bylines: Array<String>?,
    val source: String?) {}