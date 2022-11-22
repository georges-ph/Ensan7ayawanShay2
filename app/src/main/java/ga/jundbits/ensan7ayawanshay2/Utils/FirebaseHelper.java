package ga.jundbits.ensan7ayawanshay2.Utils;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import ga.jundbits.ensan7ayawanshay2.R;

public class FirebaseHelper {

    // UI
    private Activity activity;

    // Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;

    // String
    public static String currentUserID;

    // Firebase References
    public static DocumentReference appDocument;
    public static CollectionReference usersCollection;

    public FirebaseHelper(Activity activity) {
        this.activity = activity;

        initVars();

        if (firebaseUser != null)
            retrieveInfo();

    }

    private void initVars() {

        // Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Firebase References
        appDocument = firebaseFirestore.collection(activity.getString(R.string.app_name)).document("AppCollections");
        usersCollection = appDocument.collection("Users");

    }

    private void retrieveInfo() {

        currentUserID = getCurrentUserID();

    }

    private String getCurrentUserID() {

        return firebaseUser.getUid();

    }

    public static void getUserName(String userID, Callback callback) {

        usersCollection.document(userID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        String userName = documentSnapshot.getString("name");
                        callback.onFinished(userName);

                    }
                });

    }

    public static void getUserImage(String userID, Callback callback) {

        usersCollection.document(userID)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        String image = documentSnapshot.getString("image");
                        callback.onFinished(image);

                    }
                });

    }

    public static void getUserOnline(String userID, Callback callback) {

        usersCollection.document(userID)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException error) {

                        boolean online = documentSnapshot.getBoolean("online");
                        callback.isOnline(online);

                    }
                });

    }

    public static void setOnline(boolean online) {

        usersCollection.document(currentUserID)
                .update("online", online);

    }

    public interface Callback {
        void onFinished(String data);

        void isOnline(boolean online);
    }

}
