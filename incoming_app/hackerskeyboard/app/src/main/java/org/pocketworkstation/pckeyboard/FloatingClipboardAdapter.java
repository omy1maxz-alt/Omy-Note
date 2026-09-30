// File: app/src/main/java/org/pocketworkstation/pckeyboard/FloatingClipboardAdapter.java

package org.pocketworkstation.pckeyboard;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

// Import the ClipboardItem class from CustomClipboardManager
import org.pocketworkstation.pckeyboard.CustomClipboardManager.ClipboardItem;

public class FloatingClipboardAdapter extends ArrayAdapter<String> {

    private static final String TAG = "FloatingClipboardAdapter";
    private final Context context;
    // Use ClipboardItem directly from CustomClipboardManager
    private final List<ClipboardItem> entries;
    // Keep a reference to the CustomClipboardManager to update pin states
    private final CustomClipboardManager clipboardManager;

    // Listener interfaces remain the same
    public interface OnItemClickListener { void onItemClick(int position, String text); }
    public interface OnItemPinListener { void onItemPin(int position, boolean isPinned); }
    public interface OnItemEditListener { void onItemEdit(int position, String currentText); }
    public interface OnItemDeleteListener { void onItemDelete(int position); }

    @Nullable private OnItemClickListener itemClickListener;
    @Nullable private OnItemPinListener itemPinListener;
    @Nullable private OnItemEditListener itemEditListener;
    @Nullable private OnItemDeleteListener itemDeleteListener;
    /**
     * Replaces the current list of items in the adapter with a new list.
     * This helps maintain list stability and reduce jumping.
     * @param newItems The new list of strings to display.
     */
    public void setData(List<String> newItems) {
        super.clear();
        if (newItems != null) {
            super.addAll(newItems);
        }
    }

    /**
     * Updates the internal list with new ClipboardItems and refreshes the view.
     */
    public void updateData(@NonNull List<CustomClipboardManager.ClipboardItem> newItems) {
        this.entries.clear();
        this.entries.addAll(newItems);
        sortItems();
        notifyDataSetChanged();
    }
    public FloatingClipboardAdapter(@NonNull Context context, @NonNull CustomClipboardManager clipboardManager, @NonNull List<ClipboardItem> items) {
        // We pass an empty list to the super constructor because we will manage the data ourselves.
        super(context, 0, new ArrayList<>());
        this.context = context;
        this.clipboardManager = clipboardManager; // Store reference
        this.entries = new ArrayList<>(items); // Copy the list
        // Initial sort to bring pinned items to the top
        sortItems();
    }

    // Setters for listeners remain the same
    public void setOnItemClickListener(@Nullable OnItemClickListener listener) { this.itemClickListener = listener; }
    public void setOnItemPinListener(@Nullable OnItemPinListener listener) { this.itemPinListener = listener; }
    public void setOnItemEditListener(@Nullable OnItemEditListener listener) { this.itemEditListener = listener; }
    public void setOnItemDeleteListener(@Nullable OnItemDeleteListener listener) { this.itemDeleteListener = listener; }

    /**
     * Syncs the internal list with the adapter's display list.
     * We no longer sort by pinned status to keep the list chronological (newest first).
     */
    private void sortItems() {
        // We must update the ArrayAdapter's internal list to reflect the list order.
        super.clear();
        for (ClipboardItem entry : entries) {
            super.add(entry.text);
        }
        // notifyDataSetChanged() is called automatically by super.add()
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.clipboard_item_floating, parent, false);
            holder = new ViewHolder();
            holder.itemText = convertView.findViewById(R.id.item_text);
            holder.pinButton = convertView.findViewById(R.id.pin_button);
            holder.editButton = convertView.findViewById(R.id.edit_button);
            holder.deleteButton = convertView.findViewById(R.id.delete_button);
            holder.charCount = convertView.findViewById(R.id.char_count);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Get the data for this position from our managed list.
        final ClipboardItem entry = entries.get(position);


        if (entry != null) {
            String displayText = entry.text;
            if (displayText != null && displayText.length() > 500) {
                displayText = displayText.substring(0, 500) + "...";
            }
            holder.itemText.setText(displayText);

            holder.charCount.setText(String.valueOf(entry.text.length()) + " chars");

            holder.pinButton.setImageResource(entry.isPinned ? R.drawable.ic_pin_on : R.drawable.ic_pin_off);

            holder.itemText.setOnClickListener(v -> {
                if (itemClickListener != null) itemClickListener.onItemClick(position, entry.text);
            });

            holder.pinButton.setOnClickListener(v -> {
                // 1. Toggle the pinned state in our local data object.
                entry.isPinned = !entry.isPinned;

                // 2. Immediately update the icon for instant feedback.
                holder.pinButton.setImageResource(entry.isPinned ? R.drawable.ic_pin_on : R.drawable.ic_pin_off);

                // 3. Update the state in the CustomClipboardManager.
                // The position in the sorted list might have changed, so we need to find the original index
                // in the unsorted list from the clipboardManager. However, since we are managing the state
                // locally and saving it, we can just tell the manager the current position in our sorted list
                // and the new state. The manager should update its internal list accordingly.
                // But the manager's list order might differ. Let's find the index in the manager's list.
                // A safer approach is to find the item by text in the manager's list.
                // However, the simplest and most direct way is to pass the position relative to our sorted list
                // and the new state to the manager, assuming the manager reloads its list from us or we
                // synchronize back. Since we are the UI representation, we should drive the state.
                // Let's assume the manager's list is kept in sync with ours.
                // So, we update the manager's state at the same position in our sorted list.
                // This requires the manager's list to be sorted the same way, which might not be the case initially.
                // Better approach: Find the item in the manager's list by text and update its state.
                // But we don't have direct access to the manager's list here.
                // Let's add a method to CustomClipboardManager to update pin state by text or position.
                // We added setItemPinned(int position, boolean pinned) to CustomClipboardManager.
                // We need to map our sorted position back to the original position in the manager's list.
                // This is complex. Simpler: Just tell the manager to update the item at this text.
                // Even simpler: Pass the position in our sorted list and the new state.
                // The manager should have a method like updateItemPinStateByText(String text, boolean pinned)
                // But we don't have that. Let's add it or use the position-based one carefully.
                // For now, let's assume the manager's internal list order matches our initial load order
                // or gets updated correctly. We will call the manager's setItemPinned method.
                // We need to find the corresponding position in the manager's list.
                // This is tricky. Let's assume the manager's list is a copy of ours at initialization
                // and gets updated through our actions. So the position should correspond.
                // This might break if the manager reloads independently.
                // A robust solution would be for the manager to emit events or for the adapter to
                // be the sole source of truth and the manager to reflect it.
                // For now, let's proceed with the position-based update, acknowledging the potential issue.
                // A better long-term fix would be to have the manager be the single source of truth
                // and the adapter reflect it, or vice-versa with clear synchronization points.
                // Let's try updating the manager's state. The position passed is the sorted position.
                // The manager needs to know which item this corresponds to. We can pass the text.
                // Add a method updateItemPinStateByText(String text, boolean pinned) to CustomClipboardManager.
                // But we already have setItemPinned. Let's use it with caution or improve it.
                // Find the index of this entry.text in the manager's list.
                List<ClipboardItem> managerItems = clipboardManager.getHistoryItems();
                int managerIndex = -1;
                for(int i = 0; i < managerItems.size(); i++) {
                    if (managerItems.get(i).text.equals(entry.text)) {
                        managerIndex = i;
                        break;
                    }
                }
                if (managerIndex != -1) {
                     clipboardManager.setItemPinned(managerIndex, entry.isPinned);
                     Log.d(TAG, "Pin state updated in manager for item '" + entry.text + "' at index " + managerIndex + " to " + entry.isPinned);
                } else {
                     Log.w(TAG, "Could not find item '" + entry.text + "' in manager's list to update pin state.");
                }


                // 4. Re-sort the entire list to move the item if necessary.
                sortItems();

                // 5. Notify any external listeners.
                if (itemPinListener != null) itemPinListener.onItemPin(position, entry.isPinned);
            });

            holder.editButton.setOnClickListener(v -> {
                if (itemEditListener != null) itemEditListener.onItemEdit(position, entry.text);
            });

            holder.deleteButton.setOnClickListener(v -> {
                if (itemDeleteListener != null) {
                    // When deleting, we also need to remove from our internal list.
                    // And also inform the CustomClipboardManager.
                    // Find index in manager's list
                    List<ClipboardItem> managerItems = clipboardManager.getHistoryItems();
                    int managerIndex = -1;
                    for(int i = 0; i < managerItems.size(); i++) {
                        if (managerItems.get(i).text.equals(entry.text)) {
                            managerIndex = i;
                            break;
                        }
                    }
                    if (managerIndex != -1) {
                         clipboardManager.removeItem(managerIndex);
                         Log.d(TAG, "Item deleted from manager: '" + entry.text + "' at index " + managerIndex);
                    } else {
                         Log.w(TAG, "Could not find item '" + entry.text + "' in manager's list to delete.");
                    }
                    entries.remove(position);
                    sortItems(); // Re-sort and refresh the list.
                    itemDeleteListener.onItemDelete(position);
                }
            });
        } else {
            holder.itemText.setText("Error: Item not found");
            holder.charCount.setText("0 chars");
            holder.pinButton.setEnabled(false);
            holder.editButton.setEnabled(false);
            holder.deleteButton.setEnabled(false);
        }
        return convertView;
    }

    // Override data-modifying methods to keep our internal list in sync.
    // These should ideally also update the CustomClipboardManager
    @Override
    public void add(@Nullable String object) {
        if (object != null) {
            // Add as unpinned by default
            entries.add(new ClipboardItem(object, false));
            clipboardManager.addText(object); // This should handle adding/updating in manager
            sortItems();
        }
    }

    @Override
    public void addAll(@NonNull Collection<? extends String> collection) {
        // This is tricky because we don't know the pin state of the items being added.
        // If they are just strings, assume unpinned.
        // It's better if the caller adds them one by one via add() or if there's an addAllItems method.
        // For now, add them all as unpinned.
        for (String item : collection) {
            entries.add(new ClipboardItem(item, false));
            clipboardManager.addText(item); // Add to manager
        }
        sortItems();
    }

    // The remove method in the adapter is primarily called via the delete button listener
    // which already handles removal from the manager. This override might be less used.
    @Override
    public void remove(@Nullable String object) {
        if (object == null) return;
        // Find and remove from our list
        ClipboardItem itemToRemove = null;
        for (ClipboardItem item : entries) {
            if (object.equals(item.text)) {
                itemToRemove = item;
                break;
            }
        }
        if (itemToRemove != null) {
            entries.remove(itemToRemove);
            // Also remove from manager? The delete button listener should handle this.
            // To be safe, let's try to remove from manager too.
            // Find index in manager's list
             List<ClipboardItem> managerItems = clipboardManager.getHistoryItems();
             int managerIndex = -1;
             for(int i = 0; i < managerItems.size(); i++) {
                 if (managerItems.get(i).text.equals(object)) {
                     managerIndex = i;
                     break;
                 }
             }
             if (managerIndex != -1) {
                  clipboardManager.removeItem(managerIndex);
                  Log.d(TAG, "Item removed from manager via adapter.remove(): '" + object + "' at index " + managerIndex);
             } else {
                  Log.w(TAG, "Could not find item '" + object + "' in manager's list to remove via adapter.remove().");
             }
            sortItems();
        }
    }

    @Override
    public void clear() {
        entries.clear();
        super.clear();
        // Should we clear the manager too? Probably not, as clear might be called for UI updates.
        // If the intent is to clear everything, the caller should clear the manager separately.
        // Or, this adapter's clear could clear the manager. Let's assume it does for consistency.
        // clipboardManager.clearHistory(); // Uncomment if adapter clear should clear manager
    }

    private static class ViewHolder {
        TextView itemText;
        ImageButton pinButton;
        ImageButton editButton;
        ImageButton deleteButton;
        TextView charCount;
    }
}