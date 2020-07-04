package com.example.httpsocket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import retrofit2.Call;
import retrofit2.Response;

public class ServerWorker extends Worker {
    public static final int getUserToken = 1;
    public static final int getUserData = 2;
    public static final int setUserPrettyName = 3;
    private String LogLabel = "ServerWorker";
    public ServerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @SuppressLint("RestrictedApi")
    @NonNull
    @Override
    public Result doWork() {
        int workType = getInputData().getInt("workType", -1);
        String userName = getInputData().getString("userName");
        String token = getInputData().getString("token");
        if (token != null) {
            token = "token " + token;
        }
        String prettyName = getInputData().getString("prettyName");;
        try {
            switch (workType) {
                case getUserToken:
                    Response<MyServer.TokenResponse> resGetUserToken = ServerHolder.getInstance().server.getUserNameToken(userName).execute();
                    Log.d(LogLabel, "got response: " + resGetUserToken.code());
                    if (resGetUserToken.code() != 200 || !resGetUserToken.isSuccessful()) {
                        return Result.failure();
                    }
                    MyServer.TokenResponse resultToken = resGetUserToken.body();
                    if (resultToken == null) {
                        return Result.failure();
                    }
                    Data dataToken = new Data.Builder().putString("token", resultToken.data).build();
                    return Result.success(dataToken);
                case getUserData:
                    Response<MyServer.UserResponse> resGetUserData = ServerHolder.getInstance().server.getUserData(token).execute();
                    Log.d(LogLabel, "got response: " + resGetUserData.code());
                    if (resGetUserData.code() != 200 || !resGetUserData.isSuccessful()) {
                        return Result.failure();
                    }
                    MyServer.UserResponse resultGetUser = resGetUserData.body();
                    if (resultGetUser == null) {
                        return Result.failure();
                    }
                    Data dataGetUser = new Data.Builder().
                            putString("userName", resultGetUser.data.username).
                            putString("prettyName", resultGetUser.data.pretty_name).
                            putString("imageUrl", ServerHolder.serverBaseUrl + resultGetUser.data.image_url).
                            build();
                    return Result.success(dataGetUser);
                case setUserPrettyName:
                    Response<MyServer.UserResponse>  resSetUserPrettyName =
                            ServerHolder.getInstance().server.setUserPrettyName(token, new MyServer.SetUserPrettyNameRequest(prettyName)).execute();
                    Log.d(LogLabel, "got response: " + resSetUserPrettyName.code());
                    if (resSetUserPrettyName.code() != 200 || !resSetUserPrettyName.isSuccessful()) {
                        return Result.failure();
                    }
                    MyServer.UserResponse resultSetUserPrettyName = resSetUserPrettyName.body();
                    if (resultSetUserPrettyName == null) {
                        return Result.failure();
                    }
                    Data dataSetUserPrettyName = new Data.Builder().
                            putString("userName", resultSetUserPrettyName.data.username).
                            putString("prettyName", resultSetUserPrettyName.data.pretty_name).
                            putString("imageUrl",  ServerHolder.serverBaseUrl + resultSetUserPrettyName.data.image_url).
                            build();
                    return Result.success(dataSetUserPrettyName);
            }
        }
        catch (IOException e) {
            Log.e(LogLabel, "Error while trying to execute server api call");
            return Result.failure();
        }
        return null;
    }
}
