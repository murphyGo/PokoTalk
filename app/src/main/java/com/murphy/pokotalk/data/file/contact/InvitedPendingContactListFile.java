package com.murphy.pokotalk.data.file.contact;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.user.PendingContactList;

public class InvitedPendingContactListFile extends PendingContactListFile{
    @Override
    public String getFileName() {
        return Constants.invitedPendingContactFile;
    }

    @Override
    public PendingContactList getPendingContactList() {
        return DataCollection.getInstance().getInvitedContactList();
    }
}
