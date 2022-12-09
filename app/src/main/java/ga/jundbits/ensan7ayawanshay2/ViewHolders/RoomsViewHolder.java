package ga.jundbits.ensan7ayawanshay2.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.ensan7ayawanshay2.R;

public class RoomsViewHolder extends RecyclerView.ViewHolder {

    public CircleImageView imageView;
    public TextView textView;

    public RoomsViewHolder(@NonNull View itemView) {
        super(itemView);

        imageView = itemView.findViewById(R.id.rooms_list_item_image);
        textView = itemView.findViewById(R.id.rooms_list_item_text);

    }

}