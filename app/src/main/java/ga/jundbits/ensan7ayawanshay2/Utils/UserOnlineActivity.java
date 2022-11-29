package ga.jundbits.ensan7ayawanshay2.Utils;

import androidx.appcompat.app.AppCompatActivity;

public class UserOnlineActivity extends AppCompatActivity {

    @Override
    protected void onPause() {
        super.onPause();

//        FirebaseHelper.setOnline(false);

    }

    @Override
    protected void onResume() {
        super.onResume();

//        FirebaseHelper.setOnline(true);

    }

}
