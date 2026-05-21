package com.example.voyage.ai;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface OpenAiApiService {

    @POST("v1/responses")
    Call<OpenAiResponse> getResponse(
            @Header("Authorization") String authHeader,
            @Body OpenAiRequest request
    );
}
