package net.vapormusic.animexstream.utils.constants

class C {
    companion object{

        const val GIT_DOWNLOAD_URL = "https://github.com/vapormusic/AnimeXStream/"

        //Error Codes
        const val RESPONSE_UNKNOWN: Int = 1000
        const val ERROR_CODE_DEFAULT: Int = -1000
        const val NO_INTERNET_CONNECTION = 1001

        //Base URLS
        var BASE_URL = "https://gogoanime.pe/"

        const val EPISODE_LOAD_URL = "https://ajax.gogo-load.com/ajax/load-list-episode"
        const val SUGGESTION_URL = "https://vidstreaming.io/ajax-search.html"
        const val SEARCH_URL = "/search.html"

        //MAL integration
      //
        //  const val AUTH_DEEP_LINK = "animexstream://auth"
        const val AUTH_DEEP_LINK = "net.myanimelist://login.input"
        const val MAL_OAUTH2_BASE = "https://myanimelist.net/v1/oauth2/"
        const val MAL_STATE = "animexstreamauth"
        const val MAL_GET_TRACKING = 900
        const val MAL_SET_TRACKING = 901
        const val MAL_NEW_ACCESS = 902
        const val MAL_REFRESH_ACCESS = 903

        //Model Type
        const val TYPE_RECENT_SUB = 1
        const val TYPE_POPULAR_ANIME =2
        const val TYPE_RECENT_DUB = 3
        const val TYPE_GENRE = 4
        const val TYPE_MOVIE = 5
        const val TYPE_NEW_SEASON = 6
        const val TYPE_DEFAULT= -1

        // Retrofit Request TYPE

        const val RECENT_SUB = 1
        const val RECENT_DUB = 2

        const val MAX_LIMIT_FOR_SUB_DUB = 10


        const val NEWEST_SEASON_POSITION = 2
        const val RECENT_SUB_POSITION = 0
        const val RECENT_DUB_POSITION = 1
        const val POPULAR_POSITION = 4
        const val MOVIE_POSITION = 3

        //Episode URL Type
        const val TYPE_MEDIA_URL = 100
        const val TYPE_M3U8_URL = 101
        const val TYPE_M3U8_PREPROCESS_URL = 102

        //Anime Info URL Type
        const val TYPE_ANIME_INFO = 1000
        const val TYPE_EPISODE_LIST = 1001
        const val M3U8_REGEX_PATTERN = "(http|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?"

        //Anime Search Types
        const val TYPE_SEARCH_NEW = 2000
        const val TYPE_SEARCH_UPDATE = 2001

        //Network Requests Header
        const val USER_AGENT = "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36"
        const val USER_AGENT_MAL = "User-Agent: MAL (android, 1.0.8)"
        const val ORIGIN = "origin: https://gogoanime.pe/"
        const val REFERER = "referer: https://gogoanime.lol/"
        const val XML_HTTP_REQUEST = "x-requested-with: XMLHttpRequest"

        //Realm
        const val MAX_TIME_M3U8_URL = 2 * 60 * 60 *1000
        const val MAX_TIME_FOR_ANIME = 2 * 24 * 60 *60 * 1000
    }
}