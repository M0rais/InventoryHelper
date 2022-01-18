package br.com.blecaute.inventory;

import br.com.blecaute.inventory.enums.ButtonType;
import br.com.blecaute.inventory.event.InventoryClick;
import br.com.blecaute.inventory.exception.InventoryBuilderException;
import br.com.blecaute.inventory.format.InventoryFormat;
import br.com.blecaute.inventory.format.PaginatedFormat;
import br.com.blecaute.inventory.format.impl.PaginatedItemFormat;
import br.com.blecaute.inventory.format.impl.PaginatedObjectFormat;
import br.com.blecaute.inventory.format.impl.SimpleObjectFormat;
import br.com.blecaute.inventory.format.impl.SimpleItemFormat;
import br.com.blecaute.inventory.property.InventoryProperty;
import br.com.blecaute.inventory.type.InventoryItem;
import br.com.blecaute.inventory.type.InventorySlot;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A simple class for building of @{@link Inventory}.
 * @param <T> The type of @{@link InventoryBuilder}
 */
@Getter
public class InventoryBuilder<T extends InventoryItem> implements Cloneable {

    @Getter(AccessLevel.NONE) private final String inventoryName;
    @Getter(AccessLevel.NONE) private Inventory inventory;

    @Getter(AccessLevel.NONE) private Function<Integer, Boolean> skipFunction;

    private int startSlot = 0;
    private int exitSlot;

    private int pageSize = 0;
    private int currentPage = 1;

    private InventoryProperty properties = new InventoryProperty();
    private Map<ButtonType, Pair<Integer, ItemStack>> buttons = new EnumMap<>(ButtonType.class);
    private List<InventoryFormat<T>> formats = new LinkedList<>();

    /**
     * Create instance of @{@link InventoryBuilder}
     *
     * @param name  The name of @{@link Inventory}
     * @param lines The lines of @{@link Inventory}
     */
    public InventoryBuilder(String name, int lines) {
        if (!InventoryHelper.isEnabled()) {
            throw new InventoryBuilderException("The InventoryHelper must be enabled");
        }

        int size = Math.min(6, Math.max(1, lines)) * 9;
        this.inventoryName = name.replace("&", "§");
        this.exitSlot = size - 1;
        this.inventory = createInventory(size);
    }

    /**
     * Set number of objects on each page.
     *
     * @param size  The size
     * @return This @{@link InventoryBuilder}
     */
    public InventoryBuilder<T> withPageSize(int size)  {
        this.pageSize = size;
        return this;
    }

    /**
     * Set slot to start the place of items.
     *
     * @param start The slot
     * @return This @{@link InventoryBuilder}
     */
    public InventoryBuilder<T> withStart(int start) {
        this.startSlot = start;
        return this;
    }

    /**
     * Set slot to stop place of items.
     *
     * @param exit  The slot.
     * @return This @{@link InventoryBuilder}
     */
    public InventoryBuilder<T> withExit(int exit) {
        this.exitSlot = exit;
        return this;
    }

    /**
     * Skip placing items in these slots.
     *
     * @param skip The slots
     * @return This @{@link InventoryBuilder}
     */
    public InventoryBuilder<T> withSkip(int... skip) {
        this.skipFunction = integer -> Arrays.stream(skip).anyMatch(slot -> slot == integer);
        return this;
    }

    /**
     * Skip placing items in these slots.
     *
     * @param skip The @{@link Function} to check slot.
     * @return This @{@link InventoryBuilder}
     */
    public InventoryBuilder<T> withSkip(Function<Integer, Boolean> skip) {
        this.skipFunction = skip;
        return this;
    }

    /**
     * Set item in @{@link Inventory}
     *
     * @param slot      The slot
     * @param itemStack The @{@link ItemStack}
     * @param consumer  The @{@link InventoryClick} callback.
     *
     * @return This @{@link InventoryBuilder}
     */
    public InventoryBuilder<T> withItem(int slot, ItemStack itemStack, Consumer<InventoryClick<T>> consumer) {

        if (slot > 0) {
            formats.add(new SimpleItemFormat<>(slot, itemStack, consumer));
        }

        return this;
    }

    /**
     * Set items in @{@link Inventory} with pagination
     *
     * @param items     The list of @{@link ItemStack}
     * @param consumer  The @{@link InventoryClick} callback.
     *
     * @return This @{@link InventoryBuilder}
     */
    public InventoryBuilder<T> withItems(List<ItemStack> items, Consumer<InventoryClick<T>> consumer) {
        formats.add(new PaginatedItemFormat<>(items, consumer));
        return this;
    }

    /**
     * Set item in @{@link Inventory} with @{@link InventoryItem}
     *
     * @param slot      The slot
     * @param value     The @{@link InventoryItem}
     * @param consumer  The @{@link InventoryClick} callback.
     *
     * @return This @{@link InventoryBuilder}
     */
    public InventoryBuilder<T> withObject(int slot, T value, Consumer<InventoryClick<T>> consumer) {

        if (slot > 0) {
            formats.add(new SimpleObjectFormat<>(slot, value, consumer));
        }

        return this;
    }

    /**
     * Set items in @{@link Inventory} with @{@link InventoryItem} and pagination
     *
     * @param objects   The list of @{@link InventoryItem}
     * @param consumer  The @{@link InventoryClick} callback.
     *
     * @return This @{@link InventoryBuilder}
     */
    public InventoryBuilder<T> withObjects(List<T> objects, Consumer<InventoryClick<T>> consumer) {
        formats.add(new PaginatedObjectFormat<>(objects, consumer));
        return this;
    }

    /**
     * Set @{@link ButtonType}
     *
     * @param type      The @{@link ButtonType}
     * @param slot      The slot
     * @param itemStack The @{@link ItemStack}
     *
     * @return This @{@link InventoryBuilder}
     */
    public InventoryBuilder<T> withButton(ButtonType type, int slot, ItemStack itemStack) {
        buttons.put(type, Pair.of(slot, itemStack));
        return this;
    }

    /**
     * Add property to @{@link InventoryBuilder}
     *
     * @param key       The key.
     * @param object    The object.
     *
     * @return This @{@link InventoryBuilder}
     */
    public InventoryBuilder<T> withProperty(String key, Object object) {
        this.properties.set(key, object);
        return this;
    }

    /**
     * Set properties of @{@link InventoryBuilder}
     *
     * @param properties The properties.
     *
     * @return This @{@link InventoryBuilder}
     */
    public InventoryBuilder<T> withProperties(InventoryProperty properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Clone @{@link InventoryBuilder}
     * @return The clone of this @{@link InventoryBuilder}
     */
    @Override @SuppressWarnings("unchecked")
    public InventoryBuilder<T> clone() {
        try {
            InventoryBuilder<T> clone = (InventoryBuilder<T>) super.clone();

            clone.inventory = clone.createInventory(this.inventory.getSize());
            clone.properties = this.properties.clone();
            clone.buttons = new EnumMap<>(this.buttons);
            clone.formats = new LinkedList<>(this.formats);

            return clone;

        } catch (Exception exception) {
            throw new InventoryBuilderException(exception);
        }

    }

    /**
     * Format @{@link Inventory}
     * @return This @{@link InventoryBuilder}
     */
    public InventoryBuilder<T> format() {
        inventory.clear();

        for (InventoryFormat<T> format : formats) {

            if (format instanceof PaginatedFormat) {
                PaginatedFormat<T> paginated = (PaginatedFormat<T>) format;
                paginated.format(inventory, this, skipFunction);
                createPages(paginated.getSize());

                continue;
            }

            format.format(inventory, this);
        }

        return this;
    }

    /**
     * Open @{@link Inventory} to player
     *
     * @param player The @{@link Player}
     *
     * @return This @{@link InventoryBuilder}
     */
    public InventoryBuilder<T> open(Player player) {
        updateInventory();
        player.openInventory(inventory);

        return this;
    }

    /**
     * Build inventory and open it to players.
     *
     * @param players he @{@link Player}
     *
     * @return The @{@link Inventory}
     */
    public Inventory build(Player... players) {
        updateInventory();

        for (Player player : players) {
            player.openInventory(inventory);
        }

        return this.inventory;
    }

    private void updateInventory() {
        format();

        for (HumanEntity human : inventory.getViewers()) {
            if (human instanceof Player) {
                ((Player) human).updateInventory();
            }
        }
    }

    private void createPages(int size) {
        if(this.currentPage > 1 && buttons.containsKey(ButtonType.PREVIOUS_PAGE)) {
            Pair<Integer, ItemStack> pair = buttons.get(ButtonType.PREVIOUS_PAGE);
            inventory.setItem(pair.getKey(), pair.getValue());
        }

        if(this.currentPage > 0 && buttons.containsKey(ButtonType.NEXT_PAGE) && size > this.currentPage * this.pageSize) {
            Pair<Integer, ItemStack> pair = buttons.get(ButtonType.NEXT_PAGE);
            inventory.setItem(pair.getKey(), pair.getValue());
        }
    }

    private Inventory createInventory(int size) {
        return Bukkit.createInventory(new CustomHolder(event -> {
            if (event instanceof InventoryClickEvent) {
                InventoryClickEvent click = (InventoryClickEvent) event;

                int slot = click.getRawSlot();

                for (Map.Entry<ButtonType, Pair<Integer, ItemStack>> entry : buttons.entrySet()) {
                    if (entry.getValue().getKey() == slot) {
                        this.currentPage = this.currentPage + entry.getKey().getValue();
                        format();
                        return;
                    }
                }

                for (InventoryFormat<T> format : formats) {
                    if (format.isValid(slot)) {
                        format.accept(click, this);
                        break;
                    }
                }

            }

        }), size, inventoryName);
    }

    @Data
    public static class CustomHolder implements InventoryHolder {

        private final Consumer<InventoryEvent> consumer;

        @Override
        public Inventory getInventory() {
            return null;
        }
    }

}