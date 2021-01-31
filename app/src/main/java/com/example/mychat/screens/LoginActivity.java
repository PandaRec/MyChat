package com.example.mychat.screens;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mychat.ChatActivity;
import com.example.mychat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewRegistration;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editTextEmail = findViewById(R.id.editTextLoginEmail);
        editTextPassword = findViewById(R.id.editTextLoginPassword);
        textViewRegistration = findViewById(R.id.textViewNewAccount);
        mAuth = FirebaseAuth.getInstance();

        textViewRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegistrationActivity();
            }
        });
    }

    public void onClickLoginButtonPressed(View view) {
        String email = editTextEmail.getText().toString().trim();
        String pass = editTextPassword.getText().toString().trim();
        if(email.isEmpty() || pass.isEmpty()){
            Toast.makeText(this, "Не все поля заполнены корректно", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    goToChatActivity();
                }else {
                    Toast.makeText(LoginActivity.this, "Ошибка при регистрации:\n"+task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void goToChatActivity(){
        Intent intent = new Intent(LoginActivity.this, ChatActivity.class);
        startActivity(intent);
    }

    private void goToRegistrationActivity(){
        Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
        startActivity(intent);
    }
}