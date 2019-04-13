package com.murphy.pokotalk.data.db.deprecated;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.user.PendingContactPokoList;

public class InvitingPendingContactListFile extends PendingContactListFile {
    @Override
    public String getFileName() {
        return Constants.invitingPendingContactFile;
    }

    @Override
    public PendingContactPokoList getPendingContactList() {
        return DataCollection.getInstance().getInvitingContactList();
    }
}
