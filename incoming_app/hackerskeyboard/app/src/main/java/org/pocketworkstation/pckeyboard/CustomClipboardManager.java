// File: app/src/main/java/org/pocketworkstation/pckeyboard/CustomClipboardManager.java

package org.pocketworkstation.pckeyboard;

import android.content.Context;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.SharedPreferences; // Add this import
import android.text.TextUtils; // Add this import
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections; // Add this import for sorting
import android.util.Log;

public class CustomClipboardManager {
    private static final String TAG = "CustomClipboardManager";
    private Context context;
    private ClipboardManager systemClipboardManager;
    private List<ClipboardItem> clipboardHistory; // Change to List of ClipboardItem

    private String lastKnownClipboardContent = "";
    private ClipboardManager.OnPrimaryClipChangedListener clipboardListener;

    // --- Add these constants for SharedPreferences ---
    private static final String PREFS_NAME = "clipboard_history";
    private static final String CLIPBOARD_ITEMS_KEY = "clipboard_items";
    private static final String CLIPBOARD_PINNED_KEY = "clipboard_pinned"; // Key for pinned states
    // --- End additions ---

    // --- Inner class to hold text and pinned state ---
    public static class ClipboardItem {
        public String text;
        public boolean isPinned;

        public ClipboardItem(String text, boolean isPinned) {
            this.text = text;
            this.isPinned = isPinned;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ClipboardItem that = (ClipboardItem) obj;
            // Consider items equal if their text is the same
            return text != null ? text.equals(that.text) : that.text == null;
        }

        @Override
        public int hashCode() {
            return text != null ? text.hashCode() : 0;
        }
    }
    // --- End inner class ---

    public CustomClipboardManager(Context context) {
        this.context = context;
        this.systemClipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        this.clipboardHistory = new ArrayList<>();

        loadHistory(); // Load history (including pinned states) on initialization
        setupClipboardListener();
    }

    private void setupClipboardListener() {
        if (systemClipboardManager != null) {
            clipboardListener = new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    Log.d(TAG, "System clipboard changed, updating history");
                    String newContent = getCurrentText();
                    if (!newContent.equals(lastKnownClipboardContent)) {
                        lastKnownClipboardContent = newContent;
                        if (!newContent.isEmpty()) {
                            // Check if item already exists (potentially pinned)
                            boolean found = false;
                            for (ClipboardItem item : clipboardHistory) {
                                if (newContent.equals(item.text)) {
                                    // Move existing item to front, keep its pinned state
                                    clipboardHistory.remove(item);
                                    clipboardHistory.add(0, item);
                                    found = true;
                                    Log.d(TAG, "Existing item moved to front, pinned: " + item.isPinned);
                                    break;
                                }
                            }
                            if (!found) {
                                // Add new item as unpinned
                                addToHistoryOnly(newContent, false);
                            }
                            saveHistory(); // Save state whenever history changes
                        }
                    }
                }
            };
            systemClipboardManager.addPrimaryClipChangedListener(clipboardListener);
        }
    }

    // --- Modified to work with ClipboardItem ---
    private void addToHistoryOnly(String text, boolean isPinned) {
        if (text != null && !text.isEmpty()) {
            // Check if item already exists
            boolean found = false;
            for (ClipboardItem item : clipboardHistory) {
                if (text.equals(item.text)) {
                    // If found, update its position and potentially pinned state if it was unpinned
                    clipboardHistory.remove(item);
                    // Only override pinned state if the new item is explicitly pinned or the old one was unpinned
                    clipboardHistory.add(0, new ClipboardItem(text, isPinned || item.isPinned));
                    found = true;
                    Log.d(TAG, "addToHistoryOnly: Existing item moved to front, new pinned state: " + (isPinned || item.isPinned));
                    break;
                }
            }
            if (!found) {
                Log.d(TAG, "addToHistoryOnly: Adding new item, pinned: " + isPinned);
                clipboardHistory.add(0, new ClipboardItem(text, isPinned));

                if (clipboardHistory.size() > 50) {
                     // Remove the last unpinned item if history is full
                     for (int i = clipboardHistory.size() - 1; i >= 0; i--) {
                         if (!clipboardHistory.get(i).isPinned) {
                             ClipboardItem removedItem = clipboardHistory.remove(i);
                             Log.d(TAG, "History full, removed unpinned item: " + removedItem.text);
                             break;
                         }
                     }
                     // If all items are pinned, remove the last one anyway to prevent overflow
                     if (clipboardHistory.size() > 50) {
                          ClipboardItem removedItem = clipboardHistory.remove(clipboardHistory.size() - 1);
                          Log.w(TAG, "History full and all pinned, removed last item: " + removedItem.text);
                     }
                }
            }
            // Sorting is handled by the adapter now, but we could sort here if needed for internal logic
            // sortHistory(); // Optional: sort internal list if needed
        }
    }
    // --- End modification ---

    // --- Modified updateItem to work with ClipboardItem ---
    public void updateItem(int position, String newText) {
        if (position >= 0 && position < clipboardHistory.size()) {
            ClipboardItem oldItem = clipboardHistory.get(position);
            boolean wasPinned = oldItem.isPinned;

            clipboardHistory.remove(position);
            // Add updated item, preserving the pinned state
            clipboardHistory.add(position, new ClipboardItem(newText, wasPinned));

            ClipData clip = ClipData.newPlainText("label", newText);
            systemClipboardManager.setPrimaryClip(clip);
            lastKnownClipboardContent = newText;

            saveHistory(); // Save state
            Log.d(TAG, "Updated item at position " + position + " to: " + newText + ", pinned: " + wasPinned);
        }
    }
    // --- End modification ---

    // --- Modified removeItem to work with ClipboardItem ---
    public void removeItem(int position) {
        Log.d(TAG, "Removing item at position " + position);
        if (position >= 0 && position < clipboardHistory.size()) {
            ClipboardItem removedItem = clipboardHistory.remove(position);
            saveHistory(); // Save state
            Log.d(TAG, "Item removed: " + removedItem.text + ", was pinned: " + removedItem.isPinned);
        } else {
            Log.w(TAG, "removeItem: Invalid position " + position);
        }
    }
    // --- End modification ---

    // --- Modified addText to work with ClipboardItem ---
    public void addText(String text) {
        Log.d(TAG, "addText called with: " + text);
        if (text != null && !text.isEmpty()) {
            lastKnownClipboardContent = text;

            // Check if item already exists
            boolean found = false;
            for (ClipboardItem item : clipboardHistory) {
                if (text.equals(item.text)) {
                    // Move existing item to front, keep its pinned state
                    clipboardHistory.remove(item);
                    clipboardHistory.add(0, item);
                    found = true;
                    Log.d(TAG, "addText: Existing item moved to front, pinned: " + item.isPinned);
                    break;
                }
            }
            if (!found) {
                Log.d(TAG, "addText: Adding new item as unpinned");
                clipboardHistory.add(0, new ClipboardItem(text, false)); // New items are unpinned by default

                // Implement size limit logic, prioritizing pinned items
                if (clipboardHistory.size() > 50) {
                     // Remove the last unpinned item if history is full
                     for (int i = clipboardHistory.size() - 1; i >= 0; i--) {
                         if (!clipboardHistory.get(i).isPinned) {
                             ClipboardItem removedItem = clipboardHistory.remove(i);
                             Log.d(TAG, "History full, removed unpinned item: " + removedItem.text);
                             break;
                         }
                     }
                     // If all items are pinned, remove the last one anyway to prevent overflow
                     if (clipboardHistory.size() > 50) {
                          ClipboardItem removedItem = clipboardHistory.remove(clipboardHistory.size() - 1);
                          Log.w(TAG, "History full and all pinned, removed last item: " + removedItem.text);
                     }
                }
            }

            ClipData clip = ClipData.newPlainText("label", text);
            systemClipboardManager.setPrimaryClip(clip);
            saveHistory(); // Save state
        }
    }
    // --- End modification ---

    // --- Modified getHistory to return List<ClipboardItem> or just texts based on adapter needs ---
    // The adapter expects List<String>, but we need to pass pin state somehow.
    // Let's provide two methods:
    // 1. getHistoryTexts() for backward compatibility or simple text list
    // 2. getHistoryItems() for getting the full ClipboardItem list (used by adapter constructor if modified)
    // For now, we'll modify the adapter to use getHistoryItems internally.

    /**
     * Gets the list of clipboard history items including their text and pinned state.
     * @return A list of ClipboardItem objects.
     */
    public List<ClipboardItem> getHistoryItems() {
        Log.d(TAG, "getHistoryItems called");

        String currentSystemText = getCurrentText();
        Log.d(TAG, "Current system clipboard text: '" + currentSystemText + "'");

        if (currentSystemText != null && !currentSystemText.isEmpty()) {
            if (!currentSystemText.equals(lastKnownClipboardContent)) {
                Log.d(TAG, "Found new system clipboard content, updating history");
                lastKnownClipboardContent = currentSystemText;
                // Add new content, preserving existing pin state if it was already present
                boolean found = false;
                for (ClipboardItem item : clipboardHistory) {
                    if (currentSystemText.equals(item.text)) {
                        // Move existing item to front
                        clipboardHistory.remove(item);
                        clipboardHistory.add(0, item);
                        found = true;
                        Log.d(TAG, "getHistoryItems: Existing item moved to front, pinned: " + item.isPinned);
                        break;
                    }
                }
                if (!found) {
                    // Add new item as unpinned
                    addToHistoryOnly(currentSystemText, false);
                }
                saveHistory(); // Save state
            }
        }

        Log.d(TAG, "Returning history with " + clipboardHistory.size() + " items");
        // Return a copy to prevent external modification of the internal list
        return new ArrayList<>(clipboardHistory);
    }

    /**
     * Gets the list of clipboard history texts only.
     * @return A list of String objects.
     */
    public List<String> getHistoryTexts() {
         List<String> texts = new ArrayList<>();
         for (ClipboardItem item : clipboardHistory) {
             texts.add(item.text);
         }
         return texts;
    }

    // --- Modified setHistory to work with ClipboardItem ---
    // This might be used if loading from persistent storage directly into the manager
    public void setHistoryItems(List<ClipboardItem> newHistory) {
        clipboardHistory.clear();
        clipboardHistory.addAll(newHistory);
        saveHistory(); // Save the loaded state
    }
    // --- End modification ---

    public String getCurrentText() {
        if (systemClipboardManager.hasPrimaryClip()) {
            ClipData clip = systemClipboardManager.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).coerceToText(context);
                return text != null ? text.toString() : "";
            }
        }
        return "";
    }

    public boolean hasClipboardChanged() {
        String currentText = getCurrentText();
        boolean changed = !currentText.equals(lastKnownClipboardContent);
        if (changed) {
            lastKnownClipboardContent = currentText;
        }
        return changed;
    }

    // --- New method to set the pinned state of an item ---
    /**
     * Sets the pinned state of an item at a specific position.
     * @param position The position of the item in the history.
     * @param pinned True to pin, false to unpin.
     */
    public void setItemPinned(int position, boolean pinned) {
        if (position >= 0 && position < clipboardHistory.size()) {
            ClipboardItem item = clipboardHistory.get(position);
            if (item.isPinned != pinned) { // Only update if state changes
                item.isPinned = pinned;
                Log.d(TAG, "Item at position " + position + " set pinned to " + pinned + ": " + item.text);
                saveHistory(); // Save state whenever pin state changes
            } else {
                Log.d(TAG, "Item at position " + position + " pin state unchanged (" + pinned + "): " + item.text);
            }
        } else {
            Log.w(TAG, "setItemPinned: Invalid position " + position);
        }
    }
    // --- End new method ---

    public void clearHistory() {
        clipboardHistory.clear();
        saveHistory(); // Save empty state
    }

    public void cleanup() {
        if (systemClipboardManager != null && clipboardListener != null) {
            systemClipboardManager.removePrimaryClipChangedListener(clipboardListener);
        }
    }

    // --- Methods for saving/loading history including pinned states ---
    /**
     * Saves the current clipboard history (texts and pinned states) to SharedPreferences.
     */
    private void saveHistory() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();

            // Save texts
            List<String> texts = new ArrayList<>();
            for (ClipboardItem item : clipboardHistory) {
                texts.add(item.text);
            }
            String joinedTexts = TextUtils.join("|||", texts);
            editor.putString(CLIPBOARD_ITEMS_KEY, joinedTexts);

            // Save pinned states
            List<String> pinnedStates = new ArrayList<>();
            for (ClipboardItem item : clipboardHistory) {
                pinnedStates.add(String.valueOf(item.isPinned));
            }
            String joinedPinned = TextUtils.join("|||", pinnedStates);
            editor.putString(CLIPBOARD_PINNED_KEY, joinedPinned);

            editor.apply();
            Log.d(TAG, "Clipboard history saved (" + clipboardHistory.size() + " items)");
        } catch (Exception e) {
            Log.e(TAG, "Error saving clipboard history", e);
        }
    }

    /**
     * Loads the clipboard history (texts and pinned states) from SharedPreferences.
     */
    private void loadHistory() {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            String joinedTexts = prefs.getString(CLIPBOARD_ITEMS_KEY, "");
            String joinedPinned = prefs.getString(CLIPBOARD_PINNED_KEY, "");

            clipboardHistory.clear();

            if (!joinedTexts.isEmpty()) {
                String[] texts = joinedTexts.split("\\|\\|\\|");
                String[] pinnedStates = joinedPinned.isEmpty() ? new String[0] : joinedPinned.split("\\|\\|\\|");

                int size = texts.length;
                for (int i = 0; i < size; i++) {
                    String text = texts[i];
                    boolean isPinned = false;
                    if (i < pinnedStates.length) {
                        isPinned = Boolean.parseBoolean(pinnedStates[i]);
                    }
                    clipboardHistory.add(new ClipboardItem(text, isPinned));
                }
                Log.d(TAG, "Clipboard history loaded (" + clipboardHistory.size() + " items)");
            } else {
                Log.d(TAG, "No saved clipboard history found.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading clipboard history", e);
            clipboardHistory.clear(); // Ensure it's clean on error
        }
    }
    // --- End save/load methods ---
}