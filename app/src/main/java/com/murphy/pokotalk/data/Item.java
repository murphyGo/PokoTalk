package com.murphy.pokotalk.data;

public abstract class Item {
    protected boolean surviveOnListUpdate = false; // does this item must exist after whole list update?

    public abstract void update(Item item);

    public boolean isSurviveOnListUpdate() {
        return surviveOnListUpdate;
    }

    public void setSurviveOnListUpdate(boolean surviveOnListUpdate) {
        this.surviveOnListUpdate = surviveOnListUpdate;
    }
}
