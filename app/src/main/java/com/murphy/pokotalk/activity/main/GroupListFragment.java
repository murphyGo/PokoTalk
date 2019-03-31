package com.murphy.pokotalk.activity.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.murphy.pokotalk.Constants;
import com.murphy.pokotalk.R;
import com.murphy.pokotalk.activity.chat.GroupAddActivity;
import com.murphy.pokotalk.adapter.ViewCreationCallback;
import com.murphy.pokotalk.adapter.group.GroupListAdapter;
import com.murphy.pokotalk.data.DataLock;
import com.murphy.pokotalk.data.group.Group;
import com.murphy.pokotalk.data.group.GroupPokoList;
import com.murphy.pokotalk.data.group.GroupPokoListUI;
import com.murphy.pokotalk.data.user.Contact;
import com.murphy.pokotalk.server.ActivityCallback;
import com.murphy.pokotalk.server.PokoServer;
import com.murphy.pokotalk.server.Status;
import com.murphy.pokotalk.view.GroupItem;

import java.util.ArrayList;

import static com.murphy.pokotalk.server.parser.PokoParser.collection;

public class GroupListFragment extends Fragment {
    private PokoServer server;
    private GroupListAdapter groupListAdapter;
    private ListView groupListView;
    private Button groupAddButton;
    private Listener listener;

    public interface Listener {
        void startGroupChat(Group group);
        void openGroupOptionDialog(Group group);
    }

    @Override
    public void onAttach(Context context) {
        try {
            listener = (GroupListFragment.Listener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " should implement listener");
        }
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.group_list_layout, null, false);

        // Find views
        groupListView = view.findViewById(R.id.groupList);
        groupAddButton = view.findViewById(R.id.groupAddButton);

        // Add listeners
        groupAddButton.setOnClickListener(groupAddButtonClickListener);

        try {
            DataLock.getInstance().acquireWriteLock();

            try {
                // Get group list
                GroupPokoList groupList = collection.getGroupList();
                
                // Create group list adapter
                groupListAdapter = new GroupListAdapter(getContext());
                groupListAdapter.setViewCreationCallback(groupCreationCallback);
                
                // Copy group list
                GroupPokoListUI groupListUI = (GroupPokoListUI) groupListAdapter.getPokoList();
                groupListUI.copyFromPokoList(groupList);
                
                // Set adapter
                groupListView.setAdapter(groupListAdapter);
            } finally {
                DataLock.getInstance().releaseWriteLock();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Get server
        server = PokoServer.getInstance(getContext());

        server.attachActivityCallback(Constants.getGroupListName, getGroupListCallback);
        server.attachActivityCallback(Constants.addGroupName, addGroupCallback);
        server.attachActivityCallback(Constants.exitGroupName, removeGroupCallback);
        server.attachActivityCallback(Constants.readMessageName, readMessageCallback);
        server.attachActivityCallback(Constants.sendMessageName, newMessageCallback);
        server.attachActivityCallback(Constants.newMessageName, newMessageCallback);
        server.attachActivityCallback(Constants.getMemberJoinHistory, newMessageCallback);
        server.attachActivityCallback(Constants.joinContactChatName, joinContactChatCallback);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        server.detachActivityCallback(Constants.getGroupListName, getGroupListCallback);
        server.detachActivityCallback(Constants.addGroupName, addGroupCallback);
        server.detachActivityCallback(Constants.exitGroupName, removeGroupCallback);
        server.detachActivityCallback(Constants.readMessageName, readMessageCallback);
        server.detachActivityCallback(Constants.sendMessageName, newMessageCallback);
        server.detachActivityCallback(Constants.newMessageName, newMessageCallback);
        server.detachActivityCallback(Constants.getMemberJoinHistory, newMessageCallback);
        server.detachActivityCallback(Constants.joinContactChatName, joinContactChatCallback);
    }

    // User event listeners
    private View.OnClickListener groupAddButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), GroupAddActivity.class);
            startActivityForResult(intent, Constants.RequestCode.GROUP_ADD.value);
        }
    };

    // Activity result listener
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == Constants.RequestCode.GROUP_ADD.value) {
                String name = data.getStringExtra("groupName");
                ArrayList<String> emails = data.getStringArrayListExtra("emails");
                server.sendAddGroup(name, emails);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    // View creation callbacks
    private ViewCreationCallback groupCreationCallback = new ViewCreationCallback<Group>() {
        @Override
        public void run(View view, Group g) {
            GroupItem groupView = (GroupItem) view;
            final Group group = groupView.getGroup();

            groupView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.startGroupChat(group);
                }
            });
            groupView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    listener.openGroupOptionDialog(group);
                    return true;
                }
            });
        }
    };

    // Activity callbacks
    private ActivityCallback joinContactChatCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, final Object... args) {
            final Contact contact = (Contact) getData("contact");
            final Group group = (Group) getData("group");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (group != null) {
                        GroupPokoListUI groupListUI = (GroupPokoListUI) groupListAdapter.getPokoList();
                        groupListUI.updateItem(group);
                        groupListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback getGroupListCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (groupListAdapter != null) {
                        GroupPokoList groupList = collection.getGroupList();
                        GroupPokoListUI groupListUI = (GroupPokoListUI) groupListAdapter.getPokoList();
                        groupListUI.copyFromPokoList(groupList);
                        groupListUI.addEveryContactChatGroupThatHasMessage();
                        groupListUI.sortItemsByKey();
                        groupListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback addGroupCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Group group = (Group) getData("group");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (groupListAdapter != null) {
                        if (group != null) {
                            GroupPokoListUI groupListUI = (GroupPokoListUI) groupListAdapter.getPokoList();
                            groupListUI.updateItem(group);
                            groupListAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback removeGroupCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Integer groupId = (Integer) getData("groupId");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (groupListAdapter != null) {
                        GroupPokoListUI groupListUI = (GroupPokoListUI) groupListAdapter.getPokoList();
                        groupListUI.removeItemByKey(groupId);
                        groupListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback readMessageCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Group group = (Group) getData("group");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (groupListAdapter != null && group != null) {
                        GroupPokoListUI groupListUI = (GroupPokoListUI) groupListAdapter.getPokoList();
                        groupListUI.addContactChatGroupIfHasMessage(group);
                        groupListUI.moveItemSortedByKey(group);
                        groupListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    private ActivityCallback newMessageCallback = new ActivityCallback() {
        @Override
        public void onSuccess(Status status, Object... args) {
            final Group group = (Group) getData("group");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (groupListAdapter != null && group != null) {
                        GroupPokoListUI groupListUI = (GroupPokoListUI) groupListAdapter.getPokoList();
                        groupListUI.addContactChatGroupIfHasMessage(group);
                        groupListUI.moveItemSortedByKey(group);
                        groupListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }

        @Override
        public void onError(Status status, Object... args) {

        }
    };

    // Actions
    public void notifyDataSetChanged() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                groupListAdapter.notifyDataSetChanged();
            }
        });
    }
}
