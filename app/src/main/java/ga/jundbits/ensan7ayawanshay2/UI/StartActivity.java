package ga.jundbits.ensan7ayawanshay2.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdView;
import com.google.firebase.Timestamp;

import java.util.concurrent.TimeUnit;

import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.Utils.AdMob;
import ga.jundbits.ensan7ayawanshay2.Utils.UserOnlineActivity;

public class StartActivity extends UserOnlineActivity {

    // UI
    private Toolbar startToolbar;
    private AdView startAdView;
    private Button startCreateRoomButton, startJoinRoomButton;

    // String
    private String feedbackURL = "http://bit.ly/3s5wvyI";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        initVars();
        setupToolbar();
        setOnClicks();

    }

    private void initVars() {

        // UI
        startToolbar = findViewById(R.id.start_toolbar);
        startAdView = findViewById(R.id.start_ad_view);
        startCreateRoomButton = findViewById(R.id.start_create_room_button);
        startJoinRoomButton = findViewById(R.id.start_join_room_button);

        // Google
        AdMob.requestBannerAd(startAdView);

    }

    private void setupToolbar() {

        setSupportActionBar(startToolbar);
        getSupportActionBar().setTitle(getString(R.string.app_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void setOnClicks() {

        startCreateRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                createRoom();

            }
        });

        startJoinRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                joinRoom();

            }
        });

    }

    private void createRoom() {

        Timestamp timestamp = Timestamp.now();
        long timestampSeconds = timestamp.getSeconds();
        long timestampNanoSeconds = timestamp.getNanoseconds();
        long timestampSecondsToMillis = TimeUnit.SECONDS.toMillis(timestampSeconds);
        long timestampNanoSecondsToMillis = TimeUnit.NANOSECONDS.toMillis(timestampNanoSeconds);
        final long timestampTotalMillis = timestampSecondsToMillis + timestampNanoSecondsToMillis;

        Intent usersIntent = new Intent(this, UsersActivity.class);
        usersIntent.putExtra("timestamp_total_millis", timestampTotalMillis);
        startActivity(usersIntent);

    }

    private void joinRoom() {

        Intent roomsIntent = new Intent(this, RoomsActivity.class);
        startActivity(roomsIntent);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.start_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            case R.id.start_menu_feedback:
                sendFeedback();
                return true;

            default:
                return false;

        }

    }

    private void sendFeedback() {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW);
        browserIntent.setData(Uri.parse(feedbackURL));
        startActivity(browserIntent);

    }


}