package ga.jundbits.ensan7ayawanshay2.Adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.ensan7ayawanshay2.Models.UsersModel;
import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.Utils.HelperMethods;

public class UsersRecyclerAdapter extends RecyclerView.Adapter<UsersRecyclerAdapter.UsersViewHolder> {

    private Context context;
    private List<UsersModel> usersList;
    private Callback callback;
    private List<UsersModel> invitedUsers;

    public interface Callback {
        void invitationList(List<UsersModel> usersModel);
    }

    public UsersRecyclerAdapter(Context context, List<UsersModel> usersList, Callback callback) {
        this.context = context;
        this.usersList = usersList;
        this.callback = callback;
        this.invitedUsers = new ArrayList<>();
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();

        View view = LayoutInflater.from(context).inflate(R.layout.users_list_item, parent, false);
        return new UsersViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {

        UsersModel usersModel = usersList.get(position);

        String userID = usersModel.getId();
        String image = usersModel.getImage();
        String name = usersModel.getName();
        boolean online = usersModel.isOnline();

        if (HelperMethods.getCurrentUserID().equals(userID)) {

            HelperMethods.setCurrentUserModel(usersModel);

            holder.onlineView.setVisibility(View.GONE);
            holder.nameView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.GONE);
            holder.layoutView.setVisibility(View.GONE);
            holder.itemView.setVisibility(View.GONE);

        } else {

            Glide.with(context).load(image).into(holder.imageView);
            holder.onlineView.setVisibility(online ? View.VISIBLE : View.GONE);
            holder.nameView.setText(name);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (invitedUsers.contains(usersModel)) {
                        invitedUsers.remove(usersModel);
                        holder.itemView.setBackgroundColor(Color.TRANSPARENT);
                    } else {
                        invitedUsers.add(usersModel);
                        holder.itemView.setBackgroundColor(Color.GRAY);
                    }

                    callback.invitationList(invitedUsers);

                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layoutView;
        private CircleImageView imageView;
        private TextView nameView;
        private CircleImageView onlineView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            layoutView = itemView.findViewById(R.id.users_list_item_layout);
            imageView = itemView.findViewById(R.id.users_list_item_image);
            nameView = itemView.findViewById(R.id.users_list_item_name);
            onlineView = itemView.findViewById(R.id.users_list_item_online);

        }

    }

}
