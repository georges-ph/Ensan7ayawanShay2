package ga.jundbits.ensan7ayawanshay2.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.UI.GameRoomActivity;
import ga.jundbits.ensan7ayawanshay2.UI.MainActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String TOPIC_NAME;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private FirebaseMessaging firebaseMessaging;

    private NotificationManagerCompat notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseMessaging = FirebaseMessaging.getInstance();

        firebaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                firebaseUser = firebaseAuth.getCurrentUser();

                if (firebaseUser != null) {

                    TOPIC_NAME = "GamesInvitations_" + firebaseUser.getUid();

                    firebaseMessaging.subscribeToTopic(TOPIC_NAME);

                }

            }
        });

        notificationManager = NotificationManagerCompat.from(this);

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);

        firebaseMessaging.subscribeToTopic(TOPIC_NAME);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");
        long gameInvitationID = Long.parseLong(remoteMessage.getData().get("content"));

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("game_id", gameInvitationID);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification = new NotificationCompat.Builder(this, getString(R.string.games_invitations_notification_channel_id))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify((int) gameInvitationID, notification);

    }

}
