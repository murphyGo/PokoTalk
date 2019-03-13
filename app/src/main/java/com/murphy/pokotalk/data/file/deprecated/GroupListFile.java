package com.murphy.pokotalk.data.file.deprecated;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.file.deprecated.PokoSequencialAccessFile;
import com.murphy.pokotalk.data.file.json.Parser;
import com.murphy.pokotalk.data.file.json.Serializer;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class GroupListFile extends PokoSequencialAccessFile<Group> {
    protected GroupList groupList;

    public GroupListFile() {
        super();
        groupList = DataCollection.getInstance().getGroupList();
    }

    @Override
    public String getFileName() {
        return Constants.groupFile;
    }

    @Override
    public int getItemListSize() {
        return groupList.getList().size();
    }

    @Override
    public void addItem(Group item) {
        groupList.updateItem(item);
    }

    @Override
    public Group getItemAt(int position) {
        return groupList.getList().get(position);
    }

    @Override
    public void saveItem(Group item) throws IOException, JSONException {
        JSONObject jsonGroup = Serializer.makeGroupJSON(item);
        outputStreamWriter.write(jsonGroup.toString());
    }

    @Override
    public Group read() throws IOException, JSONException {
        JSONObject jsonGroup = readJSON();
        if (jsonGroup == null) {
            return null;
        }

        return Parser.parseGroup(jsonGroup);
    }
}
