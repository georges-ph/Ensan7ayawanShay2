package ga.jundbits.ensan7ayawanshay2.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ga.jundbits.ensan7ayawanshay2.R;

public class GameRoomEntriesViewHolder extends RecyclerView.ViewHolder {

    public TextView textView;

    public GameRoomEntriesViewHolder(@NonNull View itemView) {
        super(itemView);

        textView = itemView.findViewById(R.id.game_room_entries_list_item_text);

    }

}
