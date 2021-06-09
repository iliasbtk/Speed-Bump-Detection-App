package com.example.speedbumpdetection;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText txt_input_email, txt_input_password;
    Button btn_go_to_register, btn_login;

    //Authentication variables
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        txt_input_email.findViewById(R.id.txt_input_email);
        txt_input_password.findViewById(R.id.txt_input_password);
        btn_go_to_register.findViewById(R.id.btn_go_to_register);
        btn_login.findViewById(R.id.btn_login);

        // Initialize Authentication variables
        mAuth = FirebaseAuth.getInstance();

        btn_go_to_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });
    }

    private void login(){
        String email = txt_input_email.getText().toString();
        String password = txt_input_password.getText().toString();
        if(TextUtils.isEmpty(email)){
            txt_input_email.setError("Please enter an email address");
            txt_input_email.requestFocus();
        }else if(TextUtils.isEmpty(password)){
            txt_input_password.setError(("Please enter a password"));
            txt_input_password.requestFocus();
        }else{
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }else{
                        Toast.makeText(LoginActivity.this,"Login failed - Error: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            });

        }
    }
}
