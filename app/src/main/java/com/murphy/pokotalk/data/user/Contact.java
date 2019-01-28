package com.murphy.pokotalk.data.user;

import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.group.Group;

import java.util.Calendar;

public class Contact extends User {
    private Calendar lastSeen;
    private Integer groupId;      /**NOTE: data duplication, but needed to indicate contact group */

    public Contact() {
        super();
    }

    public Contact(int id, String email, String nickname, String picture, Calendar lastSeen) {
        super(id, email, nickname, picture);
        this.lastSeen = lastSeen;
    }

    public void update(Contact contact) {
        super.update(contact);

        setGroupId(contact.getGroupId());
        setLastSeen(contact.getLastSeen());
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Calendar getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Calendar lastSeen) {
        this.lastSeen = lastSeen;
    }

    public Group getContactGroup() {
        return DataCollection.getInstance().getGroupList().getItemByKey(groupId);
    }
}
