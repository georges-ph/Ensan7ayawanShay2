package ga.jundbits.ensan7ayawanshay2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.ViewHolders.GameRoomEntriesViewHolder;

public class GameRoomEntriesRecyclerAdapter extends RecyclerView.Adapter<GameRoomEntriesViewHolder> {

    private Context context;
    private List<String> list;

    public GameRoomEntriesRecyclerAdapter(Context context, List<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public GameRoomEntriesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        context = parent.getContext();

        View view = LayoutInflater.from(context).inflate(R.layout.game_room_entries_list_item, parent, false);
        return new GameRoomEntriesViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull GameRoomEntriesViewHolder holder, int position) {

        holder.textView.setText(list.get(position));

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

}
