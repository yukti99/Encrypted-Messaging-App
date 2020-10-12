package com.example.mychatapp.Fragments;

import com.example.mychatapp.Notifications.MyResponse;
import com.example.mychatapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

    @Headers(
            {
                    "Content-Type:application.json",
                    "Authorization:key=AAAAL0o7OR8:APA91bHhVcJn-hFbg6WtJGT8eUuxn61XvLmwEapBrnpNXvWMud_YYhiiAStsrnkLOSCEkmSrxia_mf38Hv9V4qco1dUzTFtO48vbuW9FLysXV6FFjBe0UO9HbZA5tRsW5AISCWpPk_2U"

            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
