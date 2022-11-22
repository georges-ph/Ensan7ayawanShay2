package ga.jundbits.ensan7ayawanshay2.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.ensan7ayawanshay2.Models.UsersModel;
import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.Utils.FirebaseHelper;

public class UsersRecyclerAdapter extends FirestoreRecyclerAdapter<UsersModel, UsersRecyclerAdapter.UsersViewHolder> {

    private Context context;
    private Callback callback;
    private long timestampTotalMillis;

    public interface Callback {
        void sendInvitation(String userID, String name, long timestampTotalMillis);
    }

    public UsersRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UsersModel> options, Context context, Callback callback, long timestampTotalMillis) {
        super(options);
        this.context = context;
        this.callback = callback;
        this.timestampTotalMillis = timestampTotalMillis;
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();

        View view = LayoutInflater.from(context).inflate(R.layout.users_list_item, parent, false);
        return new UsersViewHolder(view);

    }

    @Override
    protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull UsersModel model) {

        String userID = model.getId();
        String image = model.getImage();
        String name = model.getName();
        boolean online = model.isOnline();

        if (FirebaseHelper.currentUserID.equals(userID)) {

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

                    inviteUser(userID, name, timestampTotalMillis);

                }
            });

        }

    }

    private void inviteUser(String userID, String name, long timestampTotalMillis) {

        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.new_game_invitation))
                .setMessage(context.getString(R.string.invite) + " " + name + " " + context.getString(R.string.to_a_new_room))
                .setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        callback.sendInvitation(userID, name, timestampTotalMillis);

                    }
                })
                .setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();

    }

    public class UsersViewHolder extends RecyclerView.ViewHolder {

        private ConstraintLayout layoutView;
        private CircleImageView imageView;
        private TextView nameView;
        private CircleImageView onlineView;

        public UsersViewHolder(@NonNull View itemView) {
            super(itemView);

            onlineView = itemView.findViewById(R.id.users_list_item_online);
            layoutView = itemView.findViewById(R.id.users_list_item_layout);
            imageView = itemView.findViewById(R.id.users_list_item_image);
            nameView = itemView.findViewById(R.id.users_list_item_name);

        }

    }

}
