package com.mybookshelf

import org.vertx.groovy.core.buffer.Buffer
import org.vertx.groovy.core.http.RouteMatcher
import org.vertx.groovy.platform.Verticle
import org.vertx.java.core.json.JsonObject


class HttpServer extends Verticle {

    def start() {

        def eventbus = vertx.eventBus
        def server = vertx.createHttpServer()
        def routeMatcher = new RouteMatcher()

        routeMatcher.get("/") { request ->

            println "received request $request"
            def isbn = request.params.s
            println "isbn: ${request.params.s}"
            request.params.each { println it }

            def sb = new StringBuffer()

            // print header
            for (e in request.headers.entries) {
                sb << e.key << ": " << e.value << '\n'
            }
            request.response.putHeader("Content-Type", "text/plain")

            // call google with isbn
            def client = vertx.createHttpClient(port: 443, host: "www.googleapis.com")
                    .setSSL(true)
                    .setTrustAll(true)
            client.getNow("/books/v1/volumes?q=isbn:$isbn") { resp ->
                println "Got a response: ${resp.statusCode}"

                def body = new Buffer()
                resp.dataHandler { buffer ->
                    body << buffer
                }

                resp.endHandler {
                    def bodyStr = body.toString()
                    def jsonDico = new JsonObject(bodyStr).toMap()
                    eventbus.publish("writer.newdocument", jsonDico)
                    request.response.end "google response: ${bodyStr}"
                }
            }

        }

        routeMatcher.noMatch { req ->
            req.response.end "Nothing matched"
        }

        server.requestHandler(routeMatcher.asClosure()).listen(80, "192.168.0.48")
    }
}
