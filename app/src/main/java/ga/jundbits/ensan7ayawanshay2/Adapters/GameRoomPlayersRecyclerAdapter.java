package ga.jundbits.ensan7ayawanshay2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ga.jundbits.ensan7ayawanshay2.Callbacks.HelperMethodsCallback;
import ga.jundbits.ensan7ayawanshay2.Models.UsersModel;
import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.Utils.HelperMethods;
import ga.jundbits.ensan7ayawanshay2.ViewHolders.GameRoomPlayersViewHolder;

public class GameRoomPlayersRecyclerAdapter extends RecyclerView.Adapter<GameRoomPlayersViewHolder> {

    private Context context;
    private List<String> playersIdList;
    private List<String> namesList;
    private List<Integer> scoresList;

    public GameRoomPlayersRecyclerAdapter(Context context, List<String> playersIdList, List<String> namesList, List<Integer> scoresList) {
        this.context = context;
        this.playersIdList = playersIdList;
        this.namesList = namesList;
        this.scoresList = scoresList;
    }

    @NonNull
    @Override
    public GameRoomPlayersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();

        View view = LayoutInflater.from(context).inflate(R.layout.game_room_players_list_item, parent, false);
        return new GameRoomPlayersViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull GameRoomPlayersViewHolder holder, int position) {

        HelperMethods.isUserOnline(context, playersIdList.get(position), new HelperMethodsCallback() {
            @Override
            public void onSuccess(UsersModel usersModel) {

            }

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void isOnline(boolean online) {

                holder.onlineView.setVisibility(online ? View.VISIBLE : View.GONE);

            }
        });

        holder.nameView.setText(namesList.get(position));
        holder.scoreView.setText(String.valueOf(scoresList.get(position)));

    }

    @Override
    public int getItemCount() {
        return namesList.size();
    }

}
