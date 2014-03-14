import com.mongodb.MongoClient

def eventbus = vertx.eventBus

def mongoClient = new MongoClient()
def db = mongoClient.getDB("mybookshell")

eventbus.registerHandler("writer.newdocument") { message ->
    println "write document to mongo: $message "
    // insert document to google collection
    db.google.insert message

    // transform to bookshell volume
    def volume = [:]
    volume["title"] = message.volumeInfo.title
    volume["authors"] = message.volumeInfo.authors
    volume["publishedDate"] = Date.parse("yyyy-MM-dd", message.volumeInfo.publishedInfo)
    volume["industryIdentifiers"] = message.volumeInfo.industryIdentifiers
    volume["pageCount"] = message.volumeInfo.pageCount
    volume["language"] = message.volumeInfo.language

}