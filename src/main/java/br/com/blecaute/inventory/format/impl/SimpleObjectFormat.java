package br.com.blecaute.inventory.format.impl;

import br.com.blecaute.inventory.InventoryBuilder;
import br.com.blecaute.inventory.callback.ObjectCallback;
import br.com.blecaute.inventory.event.ObjectClickEvent;
import br.com.blecaute.inventory.format.InventoryFormat;
import br.com.blecaute.inventory.type.InventoryItem;
import lombok.Data;
import lombok.NonNull;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Data
public class SimpleObjectFormat<T extends InventoryItem> implements InventoryFormat<T> {

    private final int slot;
    @NonNull private final T object;
    @Nullable private final ObjectCallback<T> callBack;

    @Override
    public boolean isValid(int slot) {
        return this.slot == slot;
    }

    @Override
    public void accept(@NotNull InventoryClickEvent event, @NotNull InventoryBuilder<T> builder) {
        if (this.callBack != null) {
            this.callBack.accept(new ObjectClickEvent<>(event, event.getCurrentItem(), builder.getProperties(), object));
        }
    }

    @Override
    public void format(@NotNull Inventory inventory, @NotNull InventoryBuilder<T> builder) {
        inventory.setItem(slot, object.getItem(inventory, builder.getProperties()));
    }

}
