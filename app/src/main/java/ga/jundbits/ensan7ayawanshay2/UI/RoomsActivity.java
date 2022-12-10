package ga.jundbits.ensan7ayawanshay2.UI;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import ga.jundbits.ensan7ayawanshay2.Adapters.RoomsRecyclerAdapter;
import ga.jundbits.ensan7ayawanshay2.Models.RoomsModel;
import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.Utils.AdMob;
import ga.jundbits.ensan7ayawanshay2.Utils.HelperMethods;
import ga.jundbits.ensan7ayawanshay2.Utils.UserOnlineActivity;

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

        Query roomsQuery = HelperMethods.roomsCollectionRef(this).whereArrayContains("players", HelperMethods.getCurrentUserID());
        roomsQuery.get()
                .addOnSuccessListener(this, new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (queryDocumentSnapshots.isEmpty())
                            return;

                        List<RoomsModel> roomsList = queryDocumentSnapshots.toObjects(RoomsModel.class);

                        RoomsRecyclerAdapter roomsRecyclerAdapter = new RoomsRecyclerAdapter(RoomsActivity.this, roomsList);

                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(RoomsActivity.this);
                        linearLayoutManager.setReverseLayout(true);
                        linearLayoutManager.setStackFromEnd(true);
                        roomsRecyclerView.setLayoutManager(linearLayoutManager);
                        roomsRecyclerView.setAdapter(roomsRecyclerAdapter);

                    }
                });

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