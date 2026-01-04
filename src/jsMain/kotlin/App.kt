import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.dom.html.ReactHTML.h3
import react.useEffectOnce
import react.useState

suspend fun fetchVideo(id: Int): Video {
    val response = window
        .fetch("https://my-json-server.typicode.com/kotlin-hands-on/kotlinconf-json/videos/$id")
        .await()
        .text()
        .await()
    return Json.decodeFromString(response)
}

suspend fun fetchVideos(): List<Video> = coroutineScope {
    (1..25).map { id ->
        async {
            fetchVideo(id)
        }
    }.awaitAll()
}

val mainScope = MainScope()

val App = FC<Props> {
    // "by" creates a delegated property - https://kotlinlang.org/docs/delegated-properties.html
    var currentVideo: Video? by useState(null)
    var unwatchedVideos: List<Video> by useState(emptyList())
    var watchedVideos: List<Video> by useState(emptyList())

    useEffectOnce {
        mainScope.launch {
            unwatchedVideos = fetchVideos()
        }
    }

    h1 { +"KotlinConf Explorer" }

    div {
        h3 { +"Videos to watch" }
        VideoList {
            videos = unwatchedVideos
            selectedVideo = currentVideo
            onSelectVideo = { video ->
                currentVideo = video
            }
        }

        h3 { +"Videos watched" }
        VideoList {
            videos = watchedVideos
            selectedVideo = currentVideo
            onSelectVideo = { video ->
                currentVideo = video
            }
        }
    }

    // "let" is a scope function - https://kotlinlang.org/docs/scope-functions.html
    currentVideo?.let { curr ->
        VideoPlayer {
            video = curr
            unwatchedVideo = curr in unwatchedVideos
            onToggleWatched = {
                // Kotlin defines "+" and "-" operators for collections - https://kotlinlang.org/docs/collection-plus-minus.html
                if (video in unwatchedVideos) {
                    unwatchedVideos = unwatchedVideos - video
                    watchedVideos = watchedVideos + video
                } else {
                    watchedVideos = watchedVideos - video
                    unwatchedVideos = unwatchedVideos + video
                }
            }
        }
    }
}