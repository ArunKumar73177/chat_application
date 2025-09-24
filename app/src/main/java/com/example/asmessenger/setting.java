package com.example.asmessenger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import com.squareup.picasso.Picasso;

public class setting extends AppCompatActivity {

    ImageView setprofile;
    EditText setname, setstatus;
    Button donebut;
    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;

    String email, password;
    String existingProfileImageUrl = "";
    Uri setImageUri;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        setprofile = findViewById(R.id.settingprofile);
        setname = findViewById(R.id.settingname);
        setstatus = findViewById(R.id.settingstatus);
        donebut = findViewById(R.id.donebut);

        DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
        StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());

        // Load user data
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    email = snapshot.child("mail").getValue(String.class);
                    password = snapshot.child("password").getValue(String.class);
                    String name = snapshot.child("userName").getValue(String.class);
                    String profile = snapshot.child("profilepic").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);

                    setname.setText(name);
                    setstatus.setText(status);
                    existingProfileImageUrl = profile;

                    if (profile != null && !profile.isEmpty()) {
                        Picasso.get().load(profile).placeholder(R.drawable.photocamera).into(setprofile);
                    } else {
                        setprofile.setImageResource(R.drawable.photocamera); // default image
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(setting.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });

        // Select new profile image
        setprofile.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
        });

        // Save button logic
        donebut.setOnClickListener(v -> {
            String name = setname.getText().toString();
            String status = setstatus.getText().toString();

            if (name.isEmpty() || status.isEmpty()) {
                Toast.makeText(setting.this, "Name and Status cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if (setImageUri != null) {
                // New image selected: upload it
                storageReference.putFile(setImageUri).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                            String finalImageUri = uri.toString();
                            saveUserData(reference, name, finalImageUri, status);
                        });
                    } else {
                        Toast.makeText(setting.this, "Image upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                // No new image selected: use existing image URL
                saveUserData(reference, name, existingProfileImageUrl, status);
            }
        });
    }

    private void saveUserData(DatabaseReference reference, String name, String imageUri, String status) {
        Users users = new Users(auth.getUid(), name, email, password, imageUri, status);
        reference.setValue(users).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(setting.this, "Data is saved", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(setting.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(setting.this, "Something went wrong...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            setImageUri = data.getData();
            setprofile.setImageURI(setImageUri); // Temporarily show selected image
        }
    }
}
