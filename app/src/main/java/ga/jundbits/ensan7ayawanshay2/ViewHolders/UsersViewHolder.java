package ga.jundbits.ensan7ayawanshay2.ViewHolders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;
import ga.jundbits.ensan7ayawanshay2.R;

public class UsersViewHolder extends RecyclerView.ViewHolder {

    public ConstraintLayout layoutView;
    public CircleImageView imageView;
    public TextView nameView;

    public UsersViewHolder(@NonNull View itemView) {
        super(itemView);

        layoutView = itemView.findViewById(R.id.users_list_item_layout);
        imageView = itemView.findViewById(R.id.users_list_item_image);
        nameView = itemView.findViewById(R.id.users_list_item_name);

    }

}