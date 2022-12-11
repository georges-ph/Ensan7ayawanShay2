package ga.jundbits.ensan7ayawanshay2.UI;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.Utils.HelperMethods;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings_frame_layout, new SettingsFragment())
                    .commit();
        }

        Toolbar settingsToolbar = findViewById(R.id.settings_toolbar);

        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setTitle(getString(R.string.settings));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                saveUserPrefs();
                finish();
                return true;

        }

        return false;

    }

    @Override
    public void onBackPressed() {
        saveUserPrefs();
        super.onBackPressed();
    }

    private void saveUserPrefs() {

        if (HelperMethods.getCurrentUserModel() != null)
            HelperMethods.userDocumentRef(this, HelperMethods.getCurrentUserID())
                    .set(HelperMethods.getCurrentUserModel());

    }

}