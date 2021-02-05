package net.xblacky.animexstream.utils.rertofit

import io.reactivex.Observable
import net.xblacky.animexstream.utils.constants.C
import net.xblacky.animexstream.utils.model.PaheModel.ResolutionURLs.ResolutionURLs
import net.xblacky.animexstream.utils.model.PaheModel.SessionURLs.SessionsURLs
import net.xblacky.animexstream.utils.model.SuggestionModel
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

class NetworkInterface {

    //TODO To add Header for undectability

    interface FetchRecentSubOrDub {
        @Headers(
            C.USER_AGENT,
            C.ORIGIN,
            C.REFERER
        )
        @GET("https://ajax.gogocdn.net/ajax/page-recent-release.html")
        fun get(
            @Query("page") page: Int,
            @Query("type") type: Int
        ): Observable<ResponseBody>
    }

    interface FetchPopularFromAjax {

        @Headers(
            C.USER_AGENT,
            C.ORIGIN,
            C.REFERER
        )
        @GET("https://ajax.gogocdn.net/ajax/page-recent-release-ongoing.html")
        fun get(
            @Query("page") page: Int
        ): Observable<ResponseBody>
    }

    interface FetchMovies {
        @Headers(
            C.USER_AGENT,
            C.REFERER
        )
        @GET("/anime-movies.html")
        fun get(
            @Query("page") page: Int
        ): Observable<ResponseBody>
    }

    interface FetchNewestSeason {
        @Headers(
            C.USER_AGENT,
            C.REFERER
        )

        @GET("/new-season.html")
        fun get(
            @Query("page") page: Int
        ): Observable<ResponseBody>
    }

    interface FetchEpisodeMediaUrl {
        @Headers(
            C.USER_AGENT,
            C.REFERER
        )
        @GET
        fun get(
            @Url url: String
        ): Observable<ResponseBody>

    }

    interface FetchAnimeInfo {
        @Headers(
            C.USER_AGENT,
            C.REFERER
        )
        @GET
        fun get(
            @Url url: String
        ): Observable<ResponseBody>
    }

    interface FetchM3u8Url {
        @Headers(
            C.USER_AGENT,
            C.REFERER
        )
        @GET
        fun get(
            @Url url: String
        ): Observable<ResponseBody>
    }

    interface FetchM3u8Urlv2 {
        @Headers(
                C.USER_AGENT
        )
        @GET
        fun get(

                @Url url: String,
                @Header("Referer") ref : String  =""
        ): Observable<ResponseBody>
    }
    interface FetchEpisodeList{
        @Headers(
            C.USER_AGENT,
            C.ORIGIN,
            C.REFERER
        )
        @GET(C.EPISODE_LOAD_URL)
        fun get(
            @Query("ep_start") startEpisode: Int = 0,
            @Query("ep_end") endEpisode: String,
            @Query("id") id: String,
            @Query("default_ep") defaultEp: Int = 0,
            @Query("alias") alias: String
        ): Observable<ResponseBody>
    }

    interface FetchSearchData{
        @Headers(
            C.USER_AGENT,
            C.REFERER
        )
        @GET(C.SEARCH_URL)
        fun get(
            @Query("keyword") keyword: String,
            @Query("page") page: Int
        ): Observable<ResponseBody>
    }

    interface FetchSearchSuggestionData{
        @Headers(
            C.XML_HTTP_REQUEST
        )
        @GET(C.SUGGESTION_URL)
        fun get(
            @Query("keyword") keyword: String
        ): Call<SuggestionModel>
    }

    interface FetchPaheID {

        @GET("https://animepahe.com/api?m=search")
        @Headers(
            "user-agent:curl/7.54" )
        fun get(

            @Query("q") animename : String = ""

            ): Observable<ResponseBody>

    }
    interface FetchPaheEpisodeSessionList{

        @GET("https://animepahe.com/api?m=release&sort=episode_asc&page=1")
        @Headers("X-Requested-With:XMLHttpRequest",
            "user-agent:curl/7.54" )
        fun get(
            @Query("id") animeid : String = ""

            ): Observable<SessionsURLs>

    }

    interface FetchPaheEpisodeResolutionURL{

        @GET("https://animepahe.com/api?m=embed&p=kwik")
        @Headers(
            "user-agent:curl/7.54" )
        fun get(
            @Query("id") animeid2 : String = "",
            @Query("session") episodesession : String = ""
        ): Observable<ResolutionURLs>

    }
    interface FetchPaheEpisodeURL {

        @Headers("referer:https://kwik.cx",
            "user-agent:curl/7.54" )
        @GET("https://kwik.cx/e/{link}")
        fun get(
            @Path("link") kwiklink : String = ""

            ): Observable<ResponseBody>

    }

    interface MALAccessToken{

        @POST("https://myanimelist.net/v1/oauth2/token")

        fun get(
//        Parameter client_id: your Client ID. (REQUIRED)
//        Parameter code: the user's Authorisation Code received during the previous step. (REQUIRED)
//        Parameter code_verifier: the Code Verifier generated in Step 2. (REQUIRED)
//        Parameter grant_type: must be set to "authorization_code". (REQUIRED)
                @Query("client_id") client_id : String = C.MAL_CLIENT_ID,
                @Query("code") authcode : String = "",
                @Query("code_verifier") code_verifier : String = "",
                @Query("grant_type") grant_type : String = "authorization_code"

        ): Observable<ResponseBody>

    }

    interface MALRefreshAccessToken{

        @POST("https://myanimelist.net/v1/oauth2/token")

        fun get(

                @Query("client_id") client_id : String = C.MAL_CLIENT_ID,
                @Query("refresh_token") refresh_token : String = "",
                @Query("grant_type") grant_type : String = "refresh_token"

        ): Observable<ResponseBody>

    }

    interface MALAnimeID{

        @GET("https://api.jikan.moe/v3/search/anime")

        fun get(

                @Query("q") query : String = "",
                @Query("limit") limit : String = "1"

        ): Observable<ResponseBody>

    }

    interface MALUpdateTracking{

        @PUT("https://api.myanimelist.net/v2/anime/{anime_id}/my_list_status")

        fun set(
                @Header( "Authorization") access_token : String = "",
                @Path("anime_id") anime_id : String = "",
                @Query("num_watched_episodes") episode : String = ""


        ): Observable<ResponseBody>

    }

}