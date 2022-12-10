package ga.jundbits.ensan7ayawanshay2.Callbacks;

import ga.jundbits.ensan7ayawanshay2.Models.UsersModel;

public interface HelperMethodsCallback {
    void onSuccess(UsersModel usersModel);

    void onFailure(Exception e);

    void isOnline(Boolean online);
}