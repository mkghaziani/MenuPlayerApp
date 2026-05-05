package com.menuplayer.api

import com.menuplayer.model.MenuResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    /**
     * Fetch menu/display data for a specific screen.
     *
     * ERPNext custom method: menu.get_screen_data
     * Expected URL: GET /api/method/menu.get_screen_data?screen=screen-1
     */
    @GET("api/method/menu.get_screen_data")
    suspend fun getScreenData(
        @Query("screen") screenId: String
    ): Response<MenuResponse>
}
