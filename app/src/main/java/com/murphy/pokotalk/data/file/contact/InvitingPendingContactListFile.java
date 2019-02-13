package com.murphy.pokotalk.data.file.contact;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.user.PendingContactList;

public class InvitingPendingContactListFile extends PendingContactListFile {
    @Override
    public String getFileName() {
        return Constants.invitingPendingContactFile;
    }

    @Override
    public PendingContactList getPendingContactList() {
        return DataCollection.getInstance().getInvitingContactList();
    }
}
