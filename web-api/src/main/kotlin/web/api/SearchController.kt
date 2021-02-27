package web.api

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import javax.naming.directory.SearchResult

@Controller
@ExecuteOn(TaskExecutors.IO)
class SearchController(private val searchClient: SearchClient) {
    @Get("/search{?q,h,b,s,r,e,source*,ranking,twitterWeight,twitterFavouriteWeight,twitterRetweetWeight}")
    fun search(q: String?, h: Int?, b: String?, s: String?, r: String?, e: String?, source: List<String>?, ranking: String?,
                twitterWeight: Float?, twitterFavouriteWeight: Float?, twitterRetweetWeight: Float?, freshnessWeight: Float?): Result {
        var yql = "select * from sources newsarticle where userInput(@q)"
        b?.let {
            yql += " AND bylines contains @b "
        }
        s?.let {
            yql += " AND source contains @s "
        }
        r?.let {
            yql += " AND firstpubtime > @r AND firstpubtime < @e "
        }
        source?.let  {
            if (source.isNotEmpty()) {
                yql += " AND ("
                yql += source.map {
                    "source contains '$it'"
                }.joinToString(" OR ")
                yql += ")"
            }
        }
        yql += ";"
        var hits = h ?: 25
        return try {
            q?.let {
                val result = searchClient.search(yql, q, b, s, "default", ranking ?: "bm25_freshness", hits, r, e, twitterWeight = twitterWeight,
                    twitterFavouriteWeight = twitterFavouriteWeight, twitterRetweetWeight = twitterRetweetWeight, freshnessWeight = freshnessWeight)
                Result(result.timing, result.root.children)
            } ?: Result(null, arrayOf<SearchResultElement>())
        } catch (e: Exception) {
            Result(null, arrayOf<SearchResultElement>())
        }
    }

    @Get("/search/topics")
    fun getTopics(): Result {
        val result = searchClient.topics()
        return Result(result.timing, result.root.children)
    }

    @Get("/search/related{?id}")
    fun getRelated(id: String?): Result {
        return id?.let {
            val r = searchClient.related(id)
            Result(r.timing, r.root.children)
        } ?: Result(null, arrayOf<SearchResultElement>())
    }
}

data class Result(val timing: Map<String, Any>?, val results: Array<SearchResultElement>?)