package com.example.eltgm.chat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.app.PendingIntent.getActivity;

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

/*        ChatApplication app = (ChatApplication) getApplication();
        mSocket = app.getSocket();*/

            try {
                URI uri = new URI(Constants.CHAT_SERVER_URL);
                mSocket = IO.socket(uri);
                System.err.println("Connect to " + Constants.CHAT_SERVER_URL + " "  + mSocket); //при старте приложения конфигурируем сокет
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }


        mPasswordView = (EditText) findViewById(R.id.password_input);
        mUsernameView = (EditText) findViewById(R.id.username_input); // подключаемся к сокету и находим вьюхи


        Button signInButton = (Button) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin(); //подключение к чату
            }
        });

        mSocket.on(Socket.EVENT_CONNECT,onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT,onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("user_authorized", onLogin); //слушаем событие сервера - "user_auth"
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mSocket.off("user_authorized", onLogin); //снимаем слушателя
        mSocket.off(Socket.EVENT_CONNECT,onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT,onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
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
/*        if (TextUtils.isEmpty(password)){
            mPasswordView.setError("required");
            mPasswordView.requestFocus();
            return;
        }*/

        mUsername = username;
        mPassword = password;
        try {
            data.put("user_id",username);
            data.put("room","e694c9ce");
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

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(getApplicationContext(), "Connect", Toast.LENGTH_LONG).show();
                    System.err.println("Connect");
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Disconnect", Toast.LENGTH_LONG).show();
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Error" , Toast.LENGTH_LONG).show();
                    System.err.println("Error");
                }
            });
        }
    };
}
