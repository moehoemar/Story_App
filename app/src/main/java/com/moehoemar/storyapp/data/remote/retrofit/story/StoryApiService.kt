package com.moehoemar.storyapp.data.remote.retrofit.story

import com.moehoemar.storyapp.data.remote.response.story.AddNewStoryResponse
import com.moehoemar.storyapp.data.remote.response.story.DetailStoryResponse
import com.moehoemar.storyapp.data.remote.response.story.GetAllStoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface   StoryApiService {
    @GET("stories")
    suspend fun getAllStories(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("location") location: Int
    ): Response<GetAllStoriesResponse>

    @Multipart
    @POST("stories")
    suspend fun createNewStory(
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("lat") lat: RequestBody? = null,
        @Part("lon") long: RequestBody? = null
    ): Response<AddNewStoryResponse>

    @GET("stories")
    suspend fun getStoriesWithLocation(
        @Query("location") location : Int = 1,
    ): Response<GetAllStoriesResponse>

    @GET("stories/{id}")
    suspend fun getStoryById(
        @Path("id") id: String
    ): Response<DetailStoryResponse>
}