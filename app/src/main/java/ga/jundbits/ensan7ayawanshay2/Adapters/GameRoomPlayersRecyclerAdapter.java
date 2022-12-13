package ga.jundbits.ensan7ayawanshay2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ga.jundbits.ensan7ayawanshay2.Callbacks.HelperMethodsCallback;
import ga.jundbits.ensan7ayawanshay2.Models.PlayerDataModel;
import ga.jundbits.ensan7ayawanshay2.Models.UsersModel;
import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.Utils.HelperMethods;
import ga.jundbits.ensan7ayawanshay2.ViewHolders.GameRoomPlayersViewHolder;

public class GameRoomPlayersRecyclerAdapter extends RecyclerView.Adapter<GameRoomPlayersViewHolder> {

    private Context context;
    private List<PlayerDataModel> playerDataModelList;

    public GameRoomPlayersRecyclerAdapter(Context context, List<PlayerDataModel> playerScoreList) {
        this.context = context;
        this.playerDataModelList = playerScoreList;
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

        PlayerDataModel playerScore = playerDataModelList.get(position);

        HelperMethods.isUserOnline(playerScore.getId(), new HelperMethodsCallback() {
            @Override
            public void onSuccess(UsersModel usersModel) {

            }

            @Override
            public void onFailure(Exception e) {

            }

            @Override
            public void isOnline(Boolean online) {

                holder.onlineView.setVisibility(online ? View.VISIBLE : View.GONE);

            }

        });

        holder.nameView.setText(playerScore.getName());
        holder.scoreView.setText(String.valueOf(playerScore.getScore()));

    }

    @Override
    public int getItemCount() {
        return playerDataModelList.size();
    }

}
