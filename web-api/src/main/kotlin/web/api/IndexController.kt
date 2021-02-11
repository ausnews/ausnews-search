package web.api

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

@Controller
class IndexController {
    @Get("/")
    fun index(): String {
        return "Hi there"
    }
}