/*
 * MIT License
 *
 * Copyright (c) 2022. Rysefoxx
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package io.github.rysefoxx.pagination;

import io.github.rysefoxx.SlotIterator;
import io.github.rysefoxx.content.IntelligentItem;
import io.github.rysefoxx.content.IntelligentItemData;
import io.github.rysefoxx.util.StringConstants;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author Rysefoxx | Rysefoxx#6772
 * @since 2/19/2022
 */

public class Pagination implements Cloneable {

    private
    @Getter
    @Nonnegative
    int itemsPerPage;

    private @Getter SlotIterator slotIterator;

    private int page;
    private final RyseInventory inventory;

    private @Setter(AccessLevel.PROTECTED) List<IntelligentItemData> inventoryData = new ArrayList<>();

    /**
     * Pagination constructor with a default size of 1 element per page.
     */
    public Pagination(@NotNull RyseInventory inventory) {
        this.inventory = inventory;
        this.itemsPerPage = 1;
        this.page = 0;
    }

    //Copy Constructor
    public Pagination(@NotNull Pagination pagination) {
        this.inventory = pagination.inventory;
        this.itemsPerPage = pagination.itemsPerPage;
        this.page = pagination.page;
        this.inventoryData = pagination.inventoryData;
    }

    /**
     * Clones the pagination so that the original is not changed.
     *
     * @return cloned pagination
     * @deprecated use {@link #newInstance(Pagination)}} instead
     */
    @Deprecated
    public Pagination copy() {
        try {
            return (Pagination) clone();
        } catch (CloneNotSupportedException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Pagination clone failed", e);
        }
        return this;
    }

    //Copy Factory
    public Pagination newInstance(@NotNull Pagination pagination) {
        return new Pagination(pagination);
    }

    /**
     * @return the current page.
     */
    public @Nonnegative
    int page() {
        return this.page + 1;
    }

    /**
     * @return the last page.
     */
    public @Nonnegative
    int lastPage() {
        return (int) Math.ceil((double) this.inventoryData.stream().filter(data -> data.getOriginalSlot() == -1).count() / calculateValueForPage());
    }

    /**
     * @return the current inventory.
     */
    public @NotNull RyseInventory inventory() {
        return this.inventory;
    }

    /**
     * @return true if you are on the last page.
     */
    public boolean isLast() {
        int value = calculateValueForPage();
        int slide = (int) Math.ceil((double) this.inventoryData.stream().filter(data -> data.getOriginalSlot() == -1).count() / value);

        return this.page >= (slide != 0 ? slide - 1 : 0);
    }

    /**
     * @return true if you are on the first page.
     */
    public boolean isFirst() {
        return this.page <= 0;
    }

    /**
     * Increases the current page by 1
     *
     * @return the new Pagination
     */
    public @NotNull Pagination next() {
        if (isLast()) return this;
        this.page++;
        return this;
    }

    /**
     * Decreases the current page by 1
     *
     * @return the new Pagination
     */
    public @NotNull Pagination previous() {
        if (isFirst()) return this;
        this.page--;
        return this;
    }

    /**
     * Sets a complete list of smart ItemStacks
     *
     * @param items A list of intelligent ItemStacks
     */
    public void setItems(@NotNull List<IntelligentItem> items) {
        for (IntelligentItem item : items)
            this.inventoryData.add(new IntelligentItemData(item, this.page, -1));
    }

    /**
     * Sets a complete list of smart ItemStacks
     *
     * @param items An array of smart ItemStacks
     */
    public void setItems(@NotNull IntelligentItem[] items) {
        for (IntelligentItem item : items)
            this.inventoryData.add(new IntelligentItemData(item, this.page, -1));
    }

    /**
     * Adds a single intelligent ItemStack.
     *
     * @param item the intelligent ItemStack
     */
    public void addItem(@NotNull IntelligentItem item) {
        this.inventoryData.add(new IntelligentItemData(item, this.page, -1));
    }

    /**
     * Sets the SlotIterator for the pagination
     *
     * @param slotIterator the SlotIterator
     */
    public void iterator(@NotNull SlotIterator slotIterator) {
        this.slotIterator = slotIterator;
    }

    /**
     * Sets a new item at a slot.
     *
     * @param slot    The slot
     * @param newItem The Item
     * @throws IllegalArgumentException if slot > 53
     */
    protected void setItem(@Nonnegative int slot, @NotNull IntelligentItem newItem) throws IllegalArgumentException {
        if (slot > 53)
            throw new IllegalArgumentException(StringConstants.INVALID_SLOT);

        remove(slot);

        this.inventoryData.add(new IntelligentItemData(newItem, this.page, slot));
    }

    /**
     * Sets a new item at a slot with defined a page.
     *
     * @param slot    The slot
     * @param page    The page
     * @param newItem The Item
     * @throws IllegalArgumentException if slot > 53
     * @apiNote First page is 0
     */
    protected void setItem(@Nonnegative int slot, @Nonnegative int page, @NotNull IntelligentItem newItem) throws IllegalArgumentException {
        if (slot > 53)
            throw new IllegalArgumentException(StringConstants.INVALID_SLOT);

        remove(slot, page);

        this.inventoryData.add(new IntelligentItemData(newItem, page, slot));
    }

    /**
     * @param itemsPerPage How many items may be per page.
     * @apiNote If you have set the endPosition at the SlotIterator, it will be preferred.
     */
    public void setItemsPerPage(@Nonnegative int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return this;
        }
    }

    protected void remove(@Nonnegative int slot) {
        this.inventoryData.removeIf(data -> data.getPage() == this.page && data.getModifiedSlot() == slot);
    }

    protected void remove(@Nonnegative int slot, @Nonnegative int page) {
        this.inventoryData.removeIf(data -> data.getPage() == page && data.getModifiedSlot() == slot);
    }

    protected @Nullable IntelligentItem get(@Nonnegative int slot) {
        for (IntelligentItemData data : this.inventoryData) {
            if (data.getPage() == this.page && data.getModifiedSlot() == slot)
                return data.getItem();
        }
        return null;
    }

    protected @Nullable IntelligentItem get(@Nonnegative int slot, @Nonnegative int page) {
        for (IntelligentItemData data : this.inventoryData) {
            if (data.getPage() == page && data.getModifiedSlot() == slot)
                return data.getItem();
        }
        return null;
    }

    protected @NotNull List<IntelligentItemData> getInventoryData() {
        return this.inventoryData;
    }

    protected @NotNull List<IntelligentItemData> getDataByPage(@Nonnegative int page) {
        return this.inventoryData.stream().filter(item -> item.getPage() == page).collect(Collectors.toList());
    }

    protected void setPage(@Nonnegative int page) {
        this.page = page;
    }

    private int calculateValueForPage() {
        int value;

        if (this.slotIterator == null || this.slotIterator.getEndPosition() == -1) {
            value = this.itemsPerPage;
        } else {
            value = this.slotIterator.getEndPosition() - (this.slotIterator.getSlot() == -1 ? 9 * this.slotIterator.getRow() + this.slotIterator.getColumn() : this.slotIterator.getSlot());
        }
        return value;
    }

}
