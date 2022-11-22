package ga.jundbits.ensan7ayawanshay2.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.ensan7ayawanshay2.Models.RoomsModel;
import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.UI.GameRoomActivity;
import ga.jundbits.ensan7ayawanshay2.Utils.FirebaseHelper;

public class RoomsRecyclerAdapter extends FirestoreRecyclerAdapter<RoomsModel, RoomsRecyclerAdapter.RoomsViewHolder> {

    private Context context;

    public RoomsRecyclerAdapter(@NonNull FirestoreRecyclerOptions<RoomsModel> options, Context context) {
        super(options);
        this.context = context;
    }

    @NonNull
    @Override
    public RoomsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();

        View view = LayoutInflater.from(context).inflate(R.layout.rooms_list_item, parent, false);
        return new RoomsViewHolder(view);

    }

    @Override
    protected void onBindViewHolder(@NonNull RoomsViewHolder holder, int position, @NonNull RoomsModel model) {

        List<String> participants = model.getPlayers();

        FirebaseHelper.getUserName(participants.get(0), new FirebaseHelper.Callback() {
            @Override
            public void onFinished(String name) {

                holder.textView.setText(
                        participants.get(0).equals(FirebaseHelper.currentUserID)
                                ? Html.fromHtml("<b>" + context.getString(R.string.you) + "</b>" + " " + context.getString(R.string.created_a_room))
                                : Html.fromHtml("<b>" + name + "</b>" + " " + context.getString(R.string.added_you_to_a_room))
                );

            }

            @Override
            public void isOnline(boolean online) {

            }
        });

        FirebaseHelper.getUserImage(participants.get(0), new FirebaseHelper.Callback() {
            @Override
            public void onFinished(String data) {

                Glide.with(context).load(data).into(holder.imageView);

            }

            @Override
            public void isOnline(boolean online) {

            }
        });

    }

    public class RoomsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CircleImageView imageView;
        private TextView textView;

        public RoomsViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.rooms_list_item_image);
            textView = itemView.findViewById(R.id.rooms_list_item_text);

            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {

            Intent gamePlayIntent = new Intent(context, GameRoomActivity.class);
            gamePlayIntent.putExtra("game_id", Long.parseLong(getSnapshots().getSnapshot(getAdapterPosition()).getId()));
            context.startActivity(gamePlayIntent);

        }

    }

}
