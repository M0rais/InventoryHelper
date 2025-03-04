package br.com.blecaute.inventory.event;

import br.com.blecaute.inventory.property.InventoryProperty;
import br.com.blecaute.inventory.type.InventoryItem;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * The event called when the player clicks on an item.
 *
 * @param <T> The type of @{@link InventoryItem}
 */
public class ItemClickEvent<T extends InventoryItem> extends InventoryEvent<T>{

    public ItemClickEvent(@NotNull InventoryClickEvent event,
                          @NotNull ItemStack itemStack,
                          @NotNull InventoryProperty properties) {

        super(event, itemStack, properties, null);
    }

}
