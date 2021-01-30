package com.example.mychat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mychat.adapters.ChatAdapter;
import com.example.mychat.pojo.Message;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerViewChat;
    private ImageView imageViewSendMessage;
    private EditText editTextMessage;
    private ChatAdapter adapter;
    private FirebaseFirestore database;
    private String tempAuthor = "жопа";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerViewChat = findViewById(R.id.recyclerViewMessages);
        imageViewSendMessage = findViewById(R.id.imageViewSendMessage);
        editTextMessage = findViewById(R.id.editTextMessage);
        adapter = new ChatAdapter();
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        recyclerViewChat.setAdapter(adapter);
        database = FirebaseFirestore.getInstance();
       // List<Message> messages = new ArrayList<>();
        //messages.add(new Message("1","2",1));
        //adapter.setMessages(messages);
        imageViewSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();

            }
        });

        database.collection("messages").orderBy("date").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value!=null) {
                    List<Message> messages = value.toObjects(Message.class);
                    adapter.setMessages(messages);
                }
            }
        });





    }

    private void sendMessage(){
        String mess = editTextMessage.getText().toString().trim();
        if(mess.isEmpty()){
            Toast.makeText(this, "Вы не ввели сообщение", Toast.LENGTH_SHORT).show();
            return;
        }

        database.collection("messages").add(new Message(tempAuthor,mess,System.currentTimeMillis())).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
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
}