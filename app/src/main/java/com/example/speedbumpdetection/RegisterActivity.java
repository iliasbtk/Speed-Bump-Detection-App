package com.example.speedbumpdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {


    private TextInputEditText txt_input_email_reg, txt_input_password_reg;
    Button btn_register, btn_go_to_login;

    //Authentication variables
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        txt_input_email_reg = findViewById(R.id.txt_input_email_reg);
        txt_input_password_reg = findViewById(R.id.txt_input_password_reg);
        btn_register = findViewById(R.id.btn_register);
        btn_go_to_login = findViewById(R.id.btn_go_to_login);

        // Initialize Authentication variables
        mAuth = FirebaseAuth.getInstance();

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });

        btn_go_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });

    }

    private void registerUser(){
        String email = txt_input_email_reg.getText().toString();
        String password = txt_input_password_reg.getText().toString();
        if(TextUtils.isEmpty(email)){
            txt_input_email_reg.setError("Please enter an email address");
            txt_input_email_reg.requestFocus();
        }else if(TextUtils.isEmpty(password)){
            txt_input_password_reg.setError(("Please enter a password"));
            txt_input_password_reg.requestFocus();
        }else{
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(
                                RegisterActivity.this,"User Registered successfully"
                                ,Toast.LENGTH_LONG).show();
                        startActivity(new Intent(
                                RegisterActivity.this, LoginActivity.class));
                    }else{
                        Toast.makeText(
                                RegisterActivity.this
                                ,"Registration failed - Error: "+task.getException().getMessage()
                                ,Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }


}