package com.murphy.pokotalk.data.file.deprecated;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.data.DataCollection;
import com.murphy.pokotalk.data.file.json.Parser;
import com.murphy.pokotalk.data.file.json.Serializer;
import com.murphy.pokotalk.data.user.Stranger;
import com.murphy.pokotalk.data.user.StrangerList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class StrangerFile extends PokoSequencialAccessFile<Stranger> {
    protected StrangerList strangerList;

    public StrangerFile() {
        super();
        strangerList = DataCollection.getInstance().getStrangerList();
    }

    @Override
    public String getFileName() {
        return Constants.strangerFile;
    }

    @Override
    public int getItemListSize() {
        return strangerList.getList().size();
    }

    @Override
    public void addItem(Stranger item) {
        strangerList.updateItem(item);
    }

    @Override
    public Stranger getItemAt(int position) {
        return strangerList.getList().get(position);
    }

    @Override
    public void saveItem(Stranger item) throws IOException, JSONException {
        JSONObject jsonStranger = Serializer.makeStrangerJSON(item);
        outputStreamWriter.write(jsonStranger.toString());
    }

    @Override
    public Stranger read() throws IOException, JSONException {
        JSONObject jsonStranger = readJSON();
        if (jsonStranger == null)
            return null;

        return Parser.parseStranger(jsonStranger);
    }

}
