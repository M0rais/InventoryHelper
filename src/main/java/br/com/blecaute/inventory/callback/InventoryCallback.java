package br.com.blecaute.inventory.callback;

import br.com.blecaute.inventory.event.InventoryEvent;
import br.com.blecaute.inventory.type.InventoryItem;

/**
 * The general functional interface responsible for creating the click return.
 *
 * @param <I> The type of @{@link InventoryItem}
 * @param <T> The type of @{@link InventoryEvent}
 */
@FunctionalInterface
public interface InventoryCallback<I extends InventoryItem, T extends InventoryEvent<I>> {

    /**
     * Accept click event
     *
     * @param click The type of @{@link InventoryEvent}
     */
    void accept(T click);

}