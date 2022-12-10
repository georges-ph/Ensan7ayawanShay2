package ga.jundbits.ensan7ayawanshay2.Utils;

import androidx.appcompat.app.AppCompatActivity;

public class UserOnlineActivity extends AppCompatActivity {

    @Override
    protected void onPause() {
        super.onPause();
        HelperMethods.setCurrentUserOnline(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        HelperMethods.setCurrentUserOnline(true);
    }

}
