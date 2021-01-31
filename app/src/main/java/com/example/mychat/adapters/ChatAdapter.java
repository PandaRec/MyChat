package com.example.mychat.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mychat.R;
import com.example.mychat.pojo.Message;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    List<Message> messages;
    private static final int MY_MESSAGES_LAYOUT=0;
    private static final int OTHER_MESSAGES_LAYOUT=1;
    private Context context;


    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public ChatAdapter(Context context) {
        this.messages = new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=null;
        if(viewType==MY_MESSAGES_LAYOUT){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_messages_item,parent,false);

        }else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.other_message_item,parent,false);

        }
        return new ChatViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        String author = message.getAuthor();
        if(author!=null && author.equals(PreferenceManager.getDefaultSharedPreferences(context).getString("author","anon"))){
            return MY_MESSAGES_LAYOUT;
        }else {
            return OTHER_MESSAGES_LAYOUT;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messages.get(position);
        String text = message.getTextOfMessage();
        String urlImage = message.getUrlImage();
        holder.textViewAuthor.setText(message.getAuthor());
        if(text!=null && !text.isEmpty()){
            holder.imageViewImage.setVisibility(View.GONE);
            holder.textViewMessage.setVisibility(View.VISIBLE);
            holder.textViewMessage.setText(text);
        }else if(urlImage!=null && !urlImage.isEmpty()){
            holder.imageViewImage.setVisibility(View.VISIBLE);
            holder.textViewMessage.setVisibility(View.GONE);
            Picasso.get().load(urlImage).into(holder.imageViewImage);
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{
        private TextView textViewAuthor;
        private TextView textViewMessage;
        private ImageView imageViewImage;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAuthor = itemView.findViewById(R.id.textViewAuthor);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            imageViewImage = itemView.findViewById(R.id.imageViewImage);
        }
    }
}
