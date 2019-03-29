package com.murphy.pokotalk.activity.event;

import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class EditTextObserver {
    protected EditText editText;
    protected TextWatcher watcher;
    protected Handler handler;
    protected Runnable runnable;
    protected Runnable textChangeCallback;
    protected Runnable textEmptyCallback;

    public static final int TRIGGER_DELAY = 500;

    public EditTextObserver() {
        handler = null;
        runnable = null;
        textChangeCallback = null;
        textEmptyCallback = null;
        editText = null;

        createTextWatcher();
    }

    public EditTextObserver(EditText editText) {
        handler = null;
        runnable = null;
        textChangeCallback = null;
        textEmptyCallback = null;

        createTextWatcher();
        setEditText(editText);
    }

    public EditTextObserver setEditText(EditText editText) {
        // Set EditText to observe
        this.editText = editText;

        // Add text watcher as text change listener
        this.editText.addTextChangedListener(watcher);

        return this;
    }

    public EditTextObserver setTextChangedCallback(final Runnable callback) {
        textChangeCallback = callback;

        return this;
    }

    public EditTextObserver setTextEmptyCallback(final Runnable callback) {
        textEmptyCallback = callback;

        return this;
    }

    private void createTextWatcher() {
        // Create text watcher
        watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // If callback is scheduled, cancel it
                if (handler != null && runnable != null) {
                    handler.removeCallbacks(runnable);
                }

                // Check if the text is empty
                if (s.length() == 0) {
                    // Get user text empty callback
                    Runnable callback = getTextEmptyCallback();

                    // Start user text empty callback
                    if (callback != null) {
                        callback.run();
                    }

                    return;
                }

                // Create new runnable
                runnable = new Runnable() {
                    @Override
                    public void run() {
                        // Set handler and runnable to null
                        // so that this text change event do not try to remove this handler
                        handler = null;
                        runnable = null;

                        // Get user text changed callback
                        Runnable callback = getTextChangedCallback();

                        // Start user text changed callback
                        if (callback != null) {
                            callback.run();
                        }
                    }
                };

                // Create handler
                handler = new Handler();

                // Handler will start on UI thread after delay
                handler.postDelayed(runnable, TRIGGER_DELAY);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }

    public Runnable getTextChangedCallback() {
        return textChangeCallback;
    }

    public Runnable getTextEmptyCallback() {
        return textEmptyCallback;
    }
}
