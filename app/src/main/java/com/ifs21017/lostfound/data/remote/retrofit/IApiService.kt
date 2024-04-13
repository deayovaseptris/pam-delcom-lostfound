package com.ifs21017.lostfound.data.remote.retrofit

import com.ifs21017.lostfound.data.remote.response.DelcomAddObjectResponse
import com.ifs21017.lostfound.data.remote.response.DelcomLoginResponse
import com.ifs21017.lostfound.data.remote.response.DelcomObjectResponse
import com.ifs21017.lostfound.data.remote.response.DelcomObjectsResponse
import com.ifs21017.lostfound.data.remote.response.DelcomResponse
import com.ifs21017.lostfound.data.remote.response.DelcomUserResponse
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface IApiService {

    @FormUrlEncoded
    @POST("auth/register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): DelcomResponse

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): DelcomLoginResponse
    @GET("users/me")
    suspend fun getMe(): DelcomUserResponse

    @FormUrlEncoded
    @POST("objects")
    suspend fun postObject(
        @Field("title") title: String,
        @Field("description") description: String,
    ): DelcomAddObjectResponse
    @FormUrlEncoded
    @PUT("objects/{id}")
    suspend fun putObject(
        @Path("id") objectId: Int,
        @Field("title") title: String,
        @Field("description") description: String,
        @Field("is_finished") isFinished: Int,
    ): DelcomResponse
    @GET("objects")
    suspend fun getObjects(
        @Query("is_finished") isFinished: Int?,
    ): DelcomObjectsResponse
    @GET("objects/{id}")
    suspend fun getObject(
        @Path("id") objectId: Int,
    ): DelcomObjectResponse
    @DELETE("objects/{id}")
    suspend fun deleteObject(
        @Path("id") objectId: Int,
    ): DelcomResponse
}