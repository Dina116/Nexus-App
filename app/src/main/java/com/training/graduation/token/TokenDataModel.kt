import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

data class TokenResponse(
    val token: String
)

interface TokenApi {
    @GET("/token")
    suspend fun getToken(
        @Query("name") name: String,
        @Query("email") email: String,
        @Query("room") room: String,
        @Query("moderator") isModerator: Boolean
    ): Response<TokenResponse>
}
