package com.example.llmchatbot.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LLMApiService {

    @POST("api/chat")
    Call<LLMResponse> sendMessage(@Body LLMRequest request);
}
