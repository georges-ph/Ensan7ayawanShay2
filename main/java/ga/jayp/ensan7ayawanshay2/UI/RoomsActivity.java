package ga.jayp.ensan7ayawanshay2.UI;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.ads.AdView;
import com.google.firebase.firestore.Query;

import ga.jayp.ensan7ayawanshay2.Adapters.RoomsRecyclerAdapter;
import ga.jayp.ensan7ayawanshay2.Models.RoomsModel;
import ga.jayp.ensan7ayawanshay2.R;
import ga.jayp.ensan7ayawanshay2.Utils.AdMob;
import ga.jayp.ensan7ayawanshay2.Utils.FirebaseHelper;
import ga.jayp.ensan7ayawanshay2.Utils.UserOnlineActivity;

public class RoomsActivity extends UserOnlineActivity {

    // UI
    private Toolbar roomsToolbar;
    private RecyclerView roomsRecyclerView;
    private AdView roomsAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rooms);

        initVars();
        setupToolbar();
        loadRooms();

    }

    private void initVars() {

        // UI
        roomsToolbar = findViewById(R.id.rooms_toolbar);
        roomsRecyclerView = findViewById(R.id.rooms_recycler_view);
        roomsAdView = findViewById(R.id.rooms_ad_view);

        // Google
        AdMob.requestBannerAd(roomsAdView);

    }

    private void setupToolbar() {

        setSupportActionBar(roomsToolbar);
        getSupportActionBar().setTitle(getString(R.string.rooms));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void loadRooms() {

        Query roomsQuery = FirebaseHelper.appDocument.collection("Rooms").whereArrayContains("players", FirebaseHelper.currentUserID);

        FirestoreRecyclerOptions<RoomsModel> options = new FirestoreRecyclerOptions.Builder<RoomsModel>()
                .setLifecycleOwner(this)
                .setQuery(roomsQuery, RoomsModel.class)
                .build();

        RoomsRecyclerAdapter roomsRecyclerAdapter = new RoomsRecyclerAdapter(options, this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        roomsRecyclerView.setLayoutManager(linearLayoutManager);
        roomsRecyclerView.setAdapter(roomsRecyclerAdapter);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            default:
                return false;

        }

    }

}