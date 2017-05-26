package com.example.eltgm.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class LoginActivity extends Activity {

    private EditText mUsernameView;
    private EditText mPasswordView;

    private String mUsername;
    private String mPassword;

    private Socket mSocket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ChatApplication app = (ChatApplication) getApplication();
        mSocket = app.getSocket();

        mPasswordView = (EditText) findViewById(R.id.password_input);
        mUsernameView = (EditText) findViewById(R.id.username_input); // подключаемся к сокету и находим вьюхи


        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin(); //подключение к чату
            }
        });

        mSocket.on("user_authorized", onLogin); //слушаем событие сервера - "user_auth"
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSocket.off("user_authorized", onLogin); //снимаем слушателя
    }

    private void attemptLogin() {
        JSONObject data = new JSONObject();

        mUsernameView.setError(null);

        String username = mUsernameView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        if (TextUtils.isEmpty(username)){

            mUsernameView.setError("required");
            mUsernameView.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)){
            mPasswordView.setError("required");
            mPasswordView.requestFocus();
            return;
        }

        mUsername = username;
        mPassword = password;
        try {
            data.put("user_id",username);
            data.put("room","main");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mSocket.emit("user_authorization", data);
    }

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject data = (JSONObject) args[0];

            int numUsers;
            try {
                numUsers = data.getInt("numUsers");
                //TODO убрать это санье
            } catch (JSONException e) {
                return;
            }

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra("username", mUsername);
            startActivity(intent); //как событие прошло - передаем логин и пароль и переходим на новую активность с чатом
            //intent.putExtra("numUsers", numUsers);

        }
    };
}
