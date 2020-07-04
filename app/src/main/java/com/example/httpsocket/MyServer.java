package com.example.httpsocket;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface MyServer {

    public class TokenResponse {
        public String data;
    }

    class UserResponse {
        public User data;
    }

    class SetUserPrettyNameRequest {
        public SetUserPrettyNameRequest(String pretty_name) {
            this.pretty_name = pretty_name;
        }
        public String pretty_name;
    }

    @GET("/users/{userName}/token/")
    public Call<TokenResponse> getUserNameToken(@Path("userName") String userName);

    @GET("/user/")
    public Call<UserResponse> getUserData(@Header("Authorization") String token);

    @Headers({"Content-Type: application/json"})
    @POST("/user/edit/")
    public Call<UserResponse> setUserPrettyName(@Header("Authorization") String token,
                                                @Body SetUserPrettyNameRequest request);
}