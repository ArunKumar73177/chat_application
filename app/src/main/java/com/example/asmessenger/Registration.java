package com.example.asmessenger;

import static android.content.Intent.createChooser;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class Registration extends AppCompatActivity {

    TextView loginbut;
    EditText rg_username, rg_email, rg_password, rg_repassword;
    Button rg_signup;
    CircleImageView rg_profileImg;
    FirebaseAuth auth;
    Uri imageURI;
    String imageuri;
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;

    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

        // Initialize components
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Establishing The Account");
        progressDialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        loginbut = findViewById(R.id.loginbut);
        rg_username = findViewById(R.id.rgusername);
        rg_email = findViewById(R.id.rgemail);
        rg_password = findViewById(R.id.rgpassword);
        rg_repassword = findViewById(R.id.rgrepassword);
        rg_profileImg = findViewById(R.id.profilerg0);
        rg_signup = findViewById(R.id.signupbutton);

        // Navigate to login screen
        loginbut.setOnClickListener(v -> {
            startActivity(new Intent(Registration.this, Login.class));
            finish();
        });

        // Sign up logic
        rg_signup.setOnClickListener(v -> {
            String namee = rg_username.getText().toString().trim();
            String emaill = rg_email.getText().toString().trim();
            String password = rg_password.getText().toString();
            String cPassword = rg_repassword.getText().toString();
            String status = "Hey I'm Using This Application";

            if (TextUtils.isEmpty(namee) || TextUtils.isEmpty(emaill) ||
                    TextUtils.isEmpty(password) || TextUtils.isEmpty(cPassword)) {
                Toast.makeText(Registration.this, "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!emaill.matches(emailPattern)) {
                rg_email.setError("Type a valid email");
                return;
            }

            if (password.length() < 6) {
                rg_password.setError("Password must be 6 characters or more");
                return;
            }

            if (!password.equals(cPassword)) {
                rg_repassword.setError("Passwords do not match");
                return;
            }

            progressDialog.show();

            auth.createUserWithEmailAndPassword(emaill, password)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            String id = task.getResult().getUser().getUid();
                            DatabaseReference reference = database.getReference().child("user").child(id);
                            StorageReference storageReference = storage.getReference().child("Upload").child(id);

                            if (imageURI != null) {
                                // User selected a profile image
                                storageReference.putFile(imageURI)
                                        .addOnCompleteListener(uploadTask -> {
                                            if (uploadTask.isSuccessful()) {
                                                storageReference.getDownloadUrl()
                                                        .addOnSuccessListener(uri -> {
                                                            imageuri = uri.toString();
                                                            Users users = new Users(id, namee, emaill, password, imageuri, status);
                                                            saveUser(reference, users);
                                                        });
                                            } else {
                                                Toast.makeText(Registration.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                // No image selected — use default
                                imageuri = "https://firebasestorage.googleapis.com/v0/b/asmessenger7.firebasestorage.app/o/man.png?alt=media&token=f93b1854-3bfd-46cb-9bd5-6bf49c12bcbc";
                                Users users = new Users(id, namee, emaill, password, imageuri, status);
                                saveUser(reference, users);
                            }
                        } else {
                            Toast.makeText(Registration.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Image picker
        rg_profileImg.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(createChooser(intent, "Select Picture"), 10);
        });

    }

    private void saveUser(DatabaseReference reference, Users users) {
        reference.setValue(users).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Intent intent = new Intent(Registration.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(Registration.this, "Error saving user", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && data != null) {
            imageURI = data.getData();
            rg_profileImg.setImageURI(imageURI);
        }

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
