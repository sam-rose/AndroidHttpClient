package com.example.httpsocket;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    private String LogLabel = "MainActivity";
    SharedPreferences sp;
    private String userDetailsfileName = "userDetails";
    private String tokenKey = "token";
    private String userNameKey = "userName";
    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = getSharedPreferences(userDetailsfileName, MODE_PRIVATE);
        String userName = sp.getString(userNameKey, null);

        if(userName != null) {
            visibleUIForUserNameExist();
            setUserNameInServerWorkRequest(userName);
        }
        else {
            Button setUserNameButton = findViewById(R.id.setUserNameButton);
            setUserNameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditText nameEdit = findViewById(R.id.UserNameEditText);
                    String reqUserName = nameEdit.getText().toString();
                    SharedPreferences.Editor editor = sp.edit();
                    if (!reqUserName.isEmpty() && reqUserName.matches("^[a-zA-Z0-9]+$")) {
                        Log.d(LogLabel, "setting user name to " + reqUserName);
                        editor.putString(userNameKey, reqUserName);
                        editor.apply();
                        visibleUIForUserNameExist();
                        setUserNameInServerWorkRequest(reqUserName);
                    }
                    else {
                        new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Invalid User Name")
                        .setMessage("User Name Should Contain Letters and Digits Only")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                            }
                        })
                        .create()
                        .show();
                    }
                }
            });
        }
        Button setPrettyNameButton = findViewById(R.id.setPrettyNameButton);
        setPrettyNameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView userDetailsTextView = findViewById(R.id.userDetailstextView);
                userDetailsTextView.setText("Loading...");
                EditText reqPrettyName = findViewById(R.id.prettyNameEditText);
                setInServerUserPrettyNameWorkRequest(MainActivity.this.token, reqPrettyName.getText().toString());
            }
        });
    }

    private void setUserNameInServerWorkRequest(String userName) {
        WorkRequest workerRequest = new OneTimeWorkRequest.Builder(ServerWorker.class).
                setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()) //c0df01f1-17f4-4dc9-99b8-2797ab857e7e
                .setInputData(new Data.Builder().putInt("workType", ServerWorker.getUserToken).putString("userName", userName).build())
                .build();
        WorkManager.getInstance(this).enqueue(workerRequest);
        Log.d(LogLabel, "did enqueue setUserNameInServerWorkRequest of type ServerWorker");

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workerRequest.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if(workInfo == null || workInfo.getState() == WorkInfo.State.FAILED) {
                    Log.e(LogLabel, "ServerWorker falied in setUserNameInServerWorkRequest");
                    return;
                }
                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    String token = workInfo.getOutputData().getString("token");
                    MainActivity.this.token = token;
                    Log.d(LogLabel, token);
                    getFromServerUserByTokenWorkRequest(token);
                }
            }
        });
    }

    private void getFromServerUserByTokenWorkRequest(String token) {
        WorkRequest workerRequest = new OneTimeWorkRequest.Builder(ServerWorker.class).
                setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()) //c0df01f1-17f4-4dc9-99b8-2797ab857e7e
                .setInputData(new Data.Builder().putInt("workType", ServerWorker.getUserData).putString("token", token).build())
                .build();
        WorkManager.getInstance(this).enqueue(workerRequest);
        Log.d(LogLabel, "did enqueue getFromServerUserByTokenWorkRequest of type ServerWorker");

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workerRequest.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if(workInfo == null || workInfo.getState() == WorkInfo.State.FAILED) {
                    Log.e(LogLabel, "ServerWorker falied in getFromServerUserByTokenWorkRequest");
                    return;
                }
                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    setUserDetailsView(workInfo);
                }
            }
        });
    }

    private void setInServerUserPrettyNameWorkRequest(String token, String newPrettyName) {
        WorkRequest workerRequest = new OneTimeWorkRequest.Builder(ServerWorker.class).
                setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()) //c0df01f1-17f4-4dc9-99b8-2797ab857e7e
                .setInputData(new Data.Builder().putInt("workType", ServerWorker.setUserPrettyName).putString("token", token).putString("prettyName", newPrettyName).build())
                .build();
        WorkManager.getInstance(this).enqueue(workerRequest);
        Log.d(LogLabel, "did enqueue setInServerUserPrettyNameWorkRequest of type ServerWorker");

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workerRequest.getId()).observe(this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(WorkInfo workInfo) {
                if(workInfo == null || workInfo.getState() == WorkInfo.State.FAILED) {
                    Log.e(LogLabel, "ServerWorker failed in setInServerUserPrettyNameWorkRequest");
                    return;
                }
                if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
                    setUserDetailsView(workInfo);
                }
            }
        });
    }

    private void visibleUIForUserNameExist() {
        findViewById(R.id.prettyNameEditText).setVisibility(View.VISIBLE);
        findViewById(R.id.setPrettyNameButton).setVisibility(View.VISIBLE);
        findViewById(R.id.imageView).setVisibility(View.VISIBLE);
        findViewById(R.id.setUserNameButton).setVisibility(View.GONE);
        findViewById(R.id.UserNameEditText).setVisibility(View.GONE);
        TextView userDetailsTextView = findViewById(R.id.userDetailstextView);
        userDetailsTextView.setText("Loading...");
        findViewById(R.id.userDetailstextView).setVisibility(View.VISIBLE);
    }

    private void setUserDetailsView(WorkInfo workInfo) {
        String userName = workInfo.getOutputData().getString("userName");
        String prettyName = workInfo.getOutputData().getString("prettyName");
        String imageUrl = workInfo.getOutputData().getString("imageUrl");
        Log.d(LogLabel, "success getting User info gor user " + userName);
        String msgForUser = "Welcome";
        String nameForWelcomeMsg = userName;
        if (!prettyName.isEmpty()) {
            msgForUser += " again";
            nameForWelcomeMsg = prettyName;
        }
        msgForUser += " " + nameForWelcomeMsg;
        TextView welcomeView = findViewById(R.id.userDetailstextView);
        welcomeView.setText(msgForUser + "!");

        ImageView image = findViewById(R.id.imageView);
        Picasso.with(MainActivity.this).load(imageUrl).resize(150, 150).into(image);

    }
}
