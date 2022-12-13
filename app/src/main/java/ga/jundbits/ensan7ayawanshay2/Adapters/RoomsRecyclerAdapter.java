package ga.jundbits.ensan7ayawanshay2.Adapters;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ga.jundbits.ensan7ayawanshay2.Callbacks.HelperMethodsCallback;
import ga.jundbits.ensan7ayawanshay2.Models.RoomsModel;
import ga.jundbits.ensan7ayawanshay2.Models.UsersModel;
import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.UI.GameRoomActivity;
import ga.jundbits.ensan7ayawanshay2.Utils.HelperMethods;
import ga.jundbits.ensan7ayawanshay2.Utils.HelperMethodsImplementations;
import ga.jundbits.ensan7ayawanshay2.ViewHolders.RoomsViewHolder;

public class RoomsRecyclerAdapter extends RecyclerView.Adapter<RoomsViewHolder> {

    private Context context;
    private List<RoomsModel> roomsList;

    public RoomsRecyclerAdapter(Context context, List<RoomsModel> roomsList) {
        this.context = context;
        this.roomsList = roomsList;
    }

    @NonNull
    @Override
    public RoomsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();

        View view = LayoutInflater.from(context).inflate(R.layout.rooms_list_item, parent, false);
        return new RoomsViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull RoomsViewHolder holder, int position) {

        RoomsModel roomsModel = roomsList.get(position);
        String roomCreatorID = roomsModel.getCreated_by();

        HelperMethods.getUserData(context, roomCreatorID, new HelperMethodsImplementations() {
            @Override
            public void onSuccess(UsersModel usersModel) {

                Glide.with(context).load(usersModel.getImage()).into(holder.imageView);

                holder.textView.setText(
                        roomCreatorID.equals(HelperMethods.getCurrentUserID())
                                ? Html.fromHtml("<b>" + context.getString(R.string.you) + "</b>" + " " + context.getString(R.string.created_a_room))
                                : Html.fromHtml("<b>" + usersModel.getName() + "</b>" + " " + context.getString(R.string.added_you_to_a_room))
                );

            }

            @Override
            public void onFailure(Exception e) {

                holder.textView.setText("Someone " + context.getString(R.string.created_a_room));

            }

        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent gamePlayIntent = new Intent(context, GameRoomActivity.class);
                gamePlayIntent.putExtra("game_id", roomsModel.getTimestamp_millis());
                context.startActivity(gamePlayIntent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return roomsList.size();
    }

}
