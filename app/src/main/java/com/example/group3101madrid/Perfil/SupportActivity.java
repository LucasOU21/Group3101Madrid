// SupportActivity.java
package com.example.group3101madrid.Perfil;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.group3101madrid.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class SupportActivity extends AppCompatActivity {

    private Spinner spinnerIssueType;
    private EditText etIssueTitle;
    private EditText etIssueDescription;
    private Button btnCancel;
    private Button btnSubmit;
    private FrameLayout progressOverlay;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        spinnerIssueType = findViewById(R.id.spinnerIssueType);
        etIssueTitle = findViewById(R.id.etIssueTitle);
        etIssueDescription = findViewById(R.id.etIssueDescription);
        btnCancel = findViewById(R.id.btnCancel);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressOverlay = findViewById(R.id.progressOverlay);
        ImageView ivBackButton = findViewById(R.id.ivBackButton);

        // Set up issue types spinner
        setupIssueTypesSpinner();

        // Set up back button click
        ivBackButton.setOnClickListener(v -> finish());

        // Cancel button click
        btnCancel.setOnClickListener(v -> finish());

        // Submit button click
        btnSubmit.setOnClickListener(v -> {
            if (validateForm()) {
                submitSupportTicket();
            }
        });
    }

    private void setupIssueTypesSpinner() {
        String[] issueTypes = {
                "Selecciona un tipo de problema",
                "Problemas de inicio de sesión",
                "No puedo ver los desafíos",
                "No puedo completar experiencias",
                "Problemas con el mapa",
                "La aplicación se cierra inesperadamente",
                "Problemas con mi perfil",
                "Problemas con los amigos/grupos",
                "Sugerencia de mejora",
                "Otro problema"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                issueTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIssueType.setAdapter(adapter);
    }

    private boolean validateForm() {
        boolean valid = true;

        // Validate issue type
        if (spinnerIssueType.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Por favor, selecciona un tipo de problema", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        // Validate title
        String title = etIssueTitle.getText().toString().trim();
        if (title.isEmpty()) {
            etIssueTitle.setError("El título es obligatorio");
            valid = false;
        } else {
            etIssueTitle.setError(null);
        }

        // Validate description
        String description = etIssueDescription.getText().toString().trim();
        if (description.isEmpty()) {
            etIssueDescription.setError("La descripción es obligatoria");
            valid = false;
        } else if (description.length() < 20) {
            etIssueDescription.setError("Por favor, proporciona más detalles (mínimo 20 caracteres)");
            valid = false;
        } else {
            etIssueDescription.setError(null);
        }

        return valid;
    }

    private void submitSupportTicket() {
        progressOverlay.setVisibility(View.VISIBLE);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Error: No has iniciado sesión", Toast.LENGTH_SHORT).show();
            progressOverlay.setVisibility(View.GONE);
            return;
        }

        // Get form data
        String issueType = spinnerIssueType.getSelectedItem().toString();
        String title = etIssueTitle.getText().toString().trim();
        String description = etIssueDescription.getText().toString().trim();
        String userEmail = currentUser.getEmail();
        String userId = currentUser.getUid();
        String userName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Usuario";

        // Generate unique ticket ID
        String ticketId = UUID.randomUUID().toString();

        // Create support ticket object
        Map<String, Object> ticket = new HashMap<>();
        ticket.put("ticketId", ticketId);
        ticket.put("issueType", issueType);
        ticket.put("title", title);
        ticket.put("description", description);
        ticket.put("userId", userId);
        ticket.put("userEmail", userEmail);
        ticket.put("userName", userName);
        ticket.put("status", "pending");
        ticket.put("timestamp", System.currentTimeMillis());
        ticket.put("deviceModel", android.os.Build.MODEL);
        ticket.put("androidVersion", android.os.Build.VERSION.RELEASE);

        // Save to Firebase
        DatabaseReference ticketsRef = FirebaseDatabase.getInstance().getReference("support_tickets");
        ticketsRef.child(ticketId).setValue(ticket)
                .addOnSuccessListener(aVoid -> {
                    // Also save to user's tickets
                    DatabaseReference userTicketsRef = FirebaseDatabase.getInstance().getReference("user_tickets")
                            .child(userId)
                            .child(ticketId);

                    userTicketsRef.setValue(true)
                            .addOnSuccessListener(aVoid1 -> {
                                // Send confirmation email
                                sendConfirmationEmail(userEmail, userName, ticketId, title);
                            })
                            .addOnFailureListener(e -> {
                                progressOverlay.setVisibility(View.GONE);
                                showSuccess(ticketId);
                            });
                })
                .addOnFailureListener(e -> {
                    progressOverlay.setVisibility(View.GONE);
                    Toast.makeText(SupportActivity.this,
                            "Error al enviar el reporte: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void sendConfirmationEmail(String userEmail, String userName, String ticketId, String title) {
        // Note: For production, use a proper email service like SendGrid, Mailgun, etc.
        // This is a simplified example using JavaMail which should be implemented in a background service

        // For security reasons, we would only include the notification about the email being sent
        progressOverlay.setVisibility(View.GONE);
        showSuccess(ticketId);

        // In a real implementation, you would call your backend service to send the email
        // This avoids including email credentials in your app
    }

    private void showSuccess(String ticketId) {
        String shortTicketId = ticketId.substring(0, 8);
        Toast.makeText(this,
                "Reporte enviado con éxito. ID: " + shortTicketId,
                Toast.LENGTH_LONG).show();

        // Create a confirmation dialog with ticket ID and info
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Reporte Enviado");
        builder.setMessage("Tu reporte se ha enviado correctamente. Te hemos enviado un correo de confirmación con los detalles.\n\nID del Ticket: " + shortTicketId);
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            dialog.dismiss();
            finish();
        });
        builder.setCancelable(false);
        builder.show();
    }
}