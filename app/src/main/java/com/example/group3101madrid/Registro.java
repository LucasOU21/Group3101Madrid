package com.example.group3101madrid;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Registro extends AppCompatActivity {

    // Inicializar variables
    TextInputEditText etUsername, etEmail, etPassword, etPasswordAgain;
    Button btnRegistro;
    ProgressBar progressBar;
    CheckBox cbTerms;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
        // Asignar los componentes a la vista
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordAgain = findViewById(R.id.etPasswordAgain);
        btnRegistro = findViewById(R.id.btnRegistro);
        cbTerms = findViewById(R.id.cbTerms);


        etUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                etUsername.setText(" ");
            }
        });

        etEmail.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                etEmail.setText(""); // Borrar el text al tocar
            }
        });
        etPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                etPassword.setText(""); // Borrar el text al tocar
            }
        });

        etPasswordAgain.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                etPasswordAgain.setText(""); // Borrar el text al tocar
            }
        });

        // Obtener una instancia de FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        // Crear el registro en firebase a partir del botón registro
        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Activo la progressBar

                String email, password, passwordAgain;
                email = String.valueOf(etEmail.getText());
                password = String.valueOf(etPassword.getText());
                passwordAgain = String.valueOf(etPassword.getText());
                // Comprobar si los campos están vacíos
                if (email.isEmpty()) {
                    Toast.makeText(Registro.this, "Introduce un email", Toast.LENGTH_SHORT).show();
                }
                if (password.isEmpty()) {
                    Toast.makeText(Registro.this, "Introduce una contraseña", Toast.LENGTH_SHORT).show();
                }
                if (password != passwordAgain) {
                    Toast.makeText(Registro.this, "¡Las contraseñas no coinciden!", Toast.LENGTH_SHORT).show();
                }
                // https://firebase.google.com/docs/auth/android/password-auth?hl=es-419

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(Registro.this, new OnCompleteListener<AuthResult>() {
                            @SuppressLint("RestrictedApi")
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Ocultar la progressBar ya que la acción de registro se ha completado satisfactoriamente
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "createUserWithEmail:success");
                                    Toast.makeText(Registro.this, "Cuenta creada con éxito", Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(getApplicationContext(), Login.class);
                                    startActivity(i);
                                } else {
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(Registro.this, "Ha habido un error al crear la cuenta de usuario", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

}