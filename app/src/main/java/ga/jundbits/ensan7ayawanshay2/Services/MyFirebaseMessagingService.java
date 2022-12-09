package ga.jundbits.ensan7ayawanshay2.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;

import ga.jundbits.ensan7ayawanshay2.R;
import ga.jundbits.ensan7ayawanshay2.UI.MainActivity;
import ga.jundbits.ensan7ayawanshay2.Utils.HelperMethods;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private NotificationManagerCompat notificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = NotificationManagerCompat.from(this);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        FirebaseAuth.getInstance()
                .addAuthStateListener(firebaseAuth -> {

                    if (firebaseAuth.getCurrentUser() != null)
                        saveToken(token);

                });

    }

    private void saveToken(String token) {

        HelperMethods
                .userDocumentRef(getApplicationContext(), HelperMethods.getCurrentUserID())
                .update("token", token);

    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        // logRemoteMessageInfo(remoteMessage);

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        long gameInvitationID = Long.parseLong(remoteMessage.getData().get("invitation_id"));

        int flags = PendingIntent.FLAG_ONE_SHOT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("invitation_id", gameInvitationID);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, flags);

        Notification notification = new NotificationCompat.Builder(this, getString(R.string.games_invitations_notification_channel_id))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify((int) gameInvitationID, notification);

    }

    private void logRemoteMessageInfo(RemoteMessage remoteMessage) {

        Log.d("msggg", "Remote message: " + remoteMessage +
                "\nMessageId: " + remoteMessage.getMessageId() +
                "\nData: " + remoteMessage.getData() +
                "\nMessageType: " + remoteMessage.getMessageType() +
                "\nFrom: " + remoteMessage.getFrom() +
                "\nNotification: " + remoteMessage.getNotification() +
                "\nNotification data: " +
                "\n----LightSettings: " + Arrays.toString(remoteMessage.getNotification().getLightSettings()) +
                "\n----VibrateTimings: " + Arrays.toString(remoteMessage.getNotification().getVibrateTimings()) +
                "\n----Title: " + remoteMessage.getNotification().getTitle() +
                "\n----TitleLocalizationKey: " + remoteMessage.getNotification().getTitleLocalizationKey() +
                "\n----TitleLocalizationKey: " + remoteMessage.getNotification().getTitleLocalizationKey() +
                "\n----TitleLocalizationArgs: " + Arrays.toString(remoteMessage.getNotification().getTitleLocalizationArgs()) +
                "\n----Body: " + remoteMessage.getNotification().getBody() +
                "\n----BodyLocalizationKey: " + remoteMessage.getNotification().getBodyLocalizationKey() +
                "\n----BodyLocalizationKey: " + remoteMessage.getNotification().getBodyLocalizationKey() +
                "\n----BodyLocalizationArgs: " + Arrays.toString(remoteMessage.getNotification().getBodyLocalizationArgs()) +
                "\n----Icon: " + remoteMessage.getNotification().getIcon() +
                "\n----ImageUrl: " + remoteMessage.getNotification().getImageUrl() +
                "\n----Sound: " + remoteMessage.getNotification().getSound() +
                "\n----Tag: " + remoteMessage.getNotification().getTag() +
                "\n----Color: " + remoteMessage.getNotification().getColor() +
                "\n----ClickAction: " + remoteMessage.getNotification().getClickAction() +
                "\n----ChannelId: " + remoteMessage.getNotification().getChannelId() +
                "\n----Link: " + remoteMessage.getNotification().getLink() +
                "\n----Ticker: " + remoteMessage.getNotification().getTicker() +
                "\n----Sticky: " + remoteMessage.getNotification().getSticky() +
                "\n----LocalOnly: " + remoteMessage.getNotification().getLocalOnly() +
                "\n----DefaultSound: " + remoteMessage.getNotification().getDefaultSound() +
                "\n----DefaultVibrateSettings: " + remoteMessage.getNotification().getDefaultVibrateSettings() +
                "\n----DefaultLightSettings: " + remoteMessage.getNotification().getDefaultLightSettings() +
                "\n----NotificationPriority: " + remoteMessage.getNotification().getNotificationPriority() +
                "\n----Visibility: " + remoteMessage.getNotification().getVisibility() +
                "\n----NotificationCount: " + remoteMessage.getNotification().getNotificationCount() +
                "\n----EventTime: " + remoteMessage.getNotification().getEventTime() +
                "\n----LightSettings: " + Arrays.toString(remoteMessage.getNotification().getLightSettings()) +
                "\n----VibrateTimings: " + Arrays.toString(remoteMessage.getNotification().getVibrateTimings()) +
                "\nCollapseKey: " + remoteMessage.getCollapseKey() +
                "\nOriginalPriority: " + remoteMessage.getOriginalPriority() +
                "\nPriority: " + remoteMessage.getPriority() +
                "\nRawData: " + Arrays.toString(remoteMessage.getRawData()) +
                "\nSenderId: " + remoteMessage.getSenderId() +
                "\nSentTime: " + remoteMessage.getSentTime() +
                "\nTo: " + remoteMessage.getTo() +
                "\nTtl: " + remoteMessage.getTtl());

    }

}
