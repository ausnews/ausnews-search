package web.api

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import javax.naming.directory.SearchResult

@Controller
@ExecuteOn(TaskExecutors.IO)
class SearchController(private val searchClient: SearchClient) {
    @Get("/search{?q,h,b,s,r,e,source*}")
    fun search(q: String?, h: Int?, b: String?, s: String?, r: String?, e: String?, source: List<String>?): Array<SearchResultElement> {
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
        try {
            return q?.let { searchClient.search(yql, q, b, s, "default", "bm25_freshness", hits, r, e).root.children }
                ?: arrayOf<SearchResultElement>()
        } catch (e: Exception) {
            return arrayOf<SearchResultElement>()
        }
    }

    @Get("/search/related{?id}")
    fun getRelated(id: String?): Array<SearchResultElement> {
        println("Searching related: $id")
        return id?.let { searchClient.related(id).root.children } ?: arrayOf<SearchResultElement>();
    }
}