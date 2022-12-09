package ga.jundbits.ensan7ayawanshay2.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.ensan7ayawanshay2.R;

public class GameRoomPlayersViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView onlineView;
    public TextView nameView, scoreView;

    public GameRoomPlayersViewHolder(@NonNull View itemView) {
        super(itemView);

        onlineView = itemView.findViewById(R.id.game_room_players_list_item_online);
        nameView = itemView.findViewById(R.id.game_room_players_list_item_name);
        scoreView = itemView.findViewById(R.id.game_room_players_list_item_score);

    }

}