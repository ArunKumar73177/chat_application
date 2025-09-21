package com.example.asmessenger;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdpter extends RecyclerView.Adapter<UserAdpter.viewholder> {
    Context mainActivity;
    ArrayList<Users> usersArrayList;

    public UserAdpter(MainActivity mainActivity, ArrayList<Users> usersArrayList) {
        this.mainActivity = mainActivity;
        this.usersArrayList = usersArrayList;
    }

    @NonNull
    @Override
    public UserAdpter.viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.user_item, parent, false);
        return new viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdpter.viewholder holder, int position) {
        Users users = usersArrayList.get(position);

        holder.username.setText(users.getUserName());
        holder.userstatus.setText(users.getStatus());

        if (users.getProfilepic() != null && !users.getProfilepic().isEmpty()) {
            Picasso.get().load(users.getProfilepic())
                    .placeholder(R.drawable.man)
                    .error(R.drawable.man)
                    .into(holder.userimg);
        } else {
            holder.userimg.setImageResource(R.drawable.man);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mainActivity, chatWin.class);
            intent.putExtra("nameeee", users.getUserName());
            intent.putExtra("reciverImg", users.getProfilepic());
            intent.putExtra("uid", users.getUserId());
            mainActivity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return usersArrayList.size();
    }

    public class viewholder extends RecyclerView.ViewHolder {
        CircleImageView userimg;
        TextView username;
        TextView userstatus;

        public viewholder(@NonNull View itemview) {
            super(itemview);
            userimg = itemView.findViewById(R.id.userimg);
            username = itemView.findViewById(R.id.username);
            userstatus = itemView.findViewById(R.id.userstatus);
        }
    }
}
