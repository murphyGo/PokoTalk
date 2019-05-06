package com.murphy.pokotalk.data.event;

import android.util.SparseArray;

import com.murphy.pokotalk.data.list.ItemPokoList;

import java.util.ArrayList;
import java.util.List;

public class EventList extends ItemPokoList<Integer, PokoEvent> {
    private SparseArray<EventGroupRelation> eventGroupMapEventId;
    private SparseArray<EventGroupRelation> eventGroupMapGroupId;

    public EventList() {
        super();

        eventGroupMapEventId = new SparseArray<>();
        eventGroupMapGroupId = new SparseArray<>();
    }

    @Override
    public Integer getKey(PokoEvent event) {
        return event.getEventId();
    }

    public void putEventGroupRelation(int eventId, int groupId) {
        EventGroupRelation relation = new EventGroupRelation();
        relation.groupId = groupId;
        relation.eventId = eventId;
        eventGroupMapEventId.put(eventId, relation);
        eventGroupMapGroupId.put(groupId, relation);
    }

    public EventGroupRelation getEventGroupRelationByEventId(int eventId) {
        return eventGroupMapEventId.get(eventId);
    }

    public EventGroupRelation getEventGroupRelationByGroupId(int groupId) {
        return eventGroupMapGroupId.get(groupId);
    }

    public List<EventGroupRelation> getEventGroupRelations() {
        List<EventGroupRelation> arrayList = new ArrayList<>(eventGroupMapEventId.size());
        for (int i = 0; i < eventGroupMapEventId.size(); i++)
            arrayList.add(eventGroupMapEventId.valueAt(i));

        return arrayList;
    }

    public EventGroupRelation removeEventGroupRelationByEventId(int eventId) {
        EventGroupRelation relation = eventGroupMapEventId.get(eventId);

        if (relation == null)
            return null;

        eventGroupMapEventId.remove(eventId);
        eventGroupMapGroupId.remove(relation.getGroupId());
        return relation;
    }

    public EventGroupRelation removeEventGroupRelationByGroupId(int groupId) {
        EventGroupRelation relation = eventGroupMapGroupId.get(groupId);

        if (relation == null)
            return null;

        eventGroupMapGroupId.remove(groupId);
        eventGroupMapEventId.remove(relation.getEventId());
        return relation;
    }

    public static class EventGroupRelation {
        protected int eventId;
        protected int groupId;

        public int getEventId() {
            return eventId;
        }

        public void setEventId(int eventId) {
            this.eventId = eventId;
        }

        public int getGroupId() {
            return groupId;
        }

        public void setGroupId(int groupId) {
            this.groupId = groupId;
        }
    }
}
