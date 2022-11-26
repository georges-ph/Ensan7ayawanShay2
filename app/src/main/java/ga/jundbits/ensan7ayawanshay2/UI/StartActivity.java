package ga.jundbits.ensan7ayawanshay2.UI;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.ads.AdView;

import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.Utils.AdMob;
import ga.jundbits.ensan7ayawanshay2.Utils.HelperMethods;

public class StartActivity extends AppCompatActivity {

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

        startCreateRoomButton.setOnClickListener(v -> createRoom());
        startJoinRoomButton.setOnClickListener(v -> joinRoom());

    }

    private void createRoom() {

        Intent usersIntent = new Intent(this, UsersActivity.class);
        usersIntent.putExtra("timestamp_millis", HelperMethods.getCurrentTimestamp());
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

    @Override
    protected void onStart() {
        super.onStart();
        HelperMethods.setCurrentUserOnline(this, true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HelperMethods.setCurrentUserOnline(this, false);
    }

}