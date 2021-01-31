package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mychat.adapters.ChatAdapter;
import com.example.mychat.pojo.Message;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1;
    private static final int RC_IMAGE_SEND = 2;

    private RecyclerView recyclerViewChat;
    private ImageView imageViewSendMessage;
    private EditText editTextMessage;
    private ImageView imageViewAddImage;
    private ChatAdapter adapter;
    private FirebaseFirestore database;
    //private String author = "жопа";
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private SharedPreferences preferences;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.signOut){
            mAuth.signOut();
            goToRegistration();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RC_IMAGE_SEND && resultCode==RESULT_OK){
            if(data!=null) {
                Uri uri = data.getData();

                final StorageReference referenceToImage = storageRef.child("images/"+uri.getLastPathSegment());
                referenceToImage.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if(!task.isSuccessful()){
                            Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                        return referenceToImage.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){
                            Uri uri1 = task.getResult();
                            Toast.makeText(ChatActivity.this, uri1.toString(), Toast.LENGTH_SHORT).show();
                            sendMessage(uri1.toString());

                            Log.i("myImage",uri1.toString());
                        }else {

                        }
                    }
                });

                Toast.makeText(this, "" + uri, Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "data is empty", Toast.LENGTH_SHORT).show();
            }
        }


        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = mAuth.getCurrentUser();
                if(user!=null){
                    preferences.edit().putString("author", user.getEmail()).apply();
                    Toast.makeText(this, user.getEmail(), Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "user=null", Toast.LENGTH_SHORT).show();
                }
                // ...
            } else {
                if(response!=null) {
                    Toast.makeText(this, ""+response.getError(), Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                }

                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        database.collection("messages").orderBy("date").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value!=null) {
                    List<Message> messages = value.toObjects(Message.class);
                    adapter.setMessages(messages);
                    recyclerViewChat.scrollToPosition(adapter.getItemCount()-1);
                }
            }
        });


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerViewChat = findViewById(R.id.recyclerViewMessages);
        imageViewSendMessage = findViewById(R.id.imageViewSendMessage);
        editTextMessage = findViewById(R.id.editTextMessage);
        imageViewAddImage = findViewById(R.id.imageViewAddImage);
        adapter = new ChatAdapter(this);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        recyclerViewChat.setAdapter(adapter);
        database = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef= storage.getReference();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        imageViewSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(null);
            }
        });
        imageViewAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY,true);
                startActivityForResult(intent,RC_IMAGE_SEND);
            }
        });




        if(mAuth.getCurrentUser()==null){
            goToRegistration();
        }else {
            preferences.edit().putString("author",mAuth.getCurrentUser().getEmail()).apply();
        }


    }

    private void sendMessage(String urlToImage){
        String mess = editTextMessage.getText().toString().trim();
        Message message = null;
        String author = preferences.getString("author","anon");
        if(mess!=null && !mess.isEmpty()){
            message = new Message(author,mess,System.currentTimeMillis(),null);
        }else if(urlToImage!=null && !urlToImage.isEmpty()){
            message = new Message(author,null,System.currentTimeMillis(),urlToImage);
        }

        database.collection("messages").add(message).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                editTextMessage.getText().clear();
                recyclerViewChat.scrollToPosition(adapter.getItemCount()-1);


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChatActivity.this, "Ошибка отправки сообщения", Toast.LENGTH_SHORT).show();
            }
        });

    }
    private void goToRegistration(){
        AuthUI.getInstance().signOut(ChatActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    // Choose authentication providers
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build());

// Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }else {
                    Toast.makeText(ChatActivity.this, "ошибка выхода из аккаунта", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}