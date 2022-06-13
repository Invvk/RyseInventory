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

package io.github.rysefoxx;

import io.github.rysefoxx.pagination.Pagination;
import io.github.rysefoxx.util.SlotUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import java.util.ArrayList;
import java.util.List;

public class SlotIterator {

    private int slot = -1;
    private int endPosition = -1;
    private int row = -1;
    private int column = -1;
    private SlotIteratorType type;
    private boolean override;
    private List<Integer> blackList = new ArrayList<>();

    @Contract(" -> new")
    public static @NotNull Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int slot = -1;
        private int endPosition = -1;
        private int row = -1;
        private int column = -1;
        private SlotIteratorType type;
        private boolean override;
        private List<Integer> blackList = new ArrayList<>();

        /**
         * Adds a slot to the blacklist.
         *
         * @param slot The slot to add to the blacklist.
         * @return The builder object itself.
         * @deprecated Use {@link #blackList(int)} instead.
         */
        @Deprecated
        public @NotNull Builder addBlackList(@Nonnegative int slot) {
            this.blackList.add(slot);
            return this;
        }

        /**
         * Adds a slot to the blacklist.
         *
         * @param slot The slot to add to the blacklist.
         * @return The builder object itself.
         */
        public @NotNull Builder blackList(@Nonnegative int slot) {
            this.blackList.add(slot);
            return this;
        }

        /**
         * This can be used to block multiple slots.
         *
         * @param slots The slots to be used for the inventory.
         * @return The builder object itself.
         */
        public @NotNull Builder blackList(@NotNull List<Integer> slots) {
            this.blackList = new ArrayList<>(slots);
            return this;
        }

        /**
         * This can be used to block multiple slots.
         *
         * @param slots The slots to be used for the inventory.
         * @return The builder object itself.
         */
        public @NotNull Builder blackList(int @NotNull ... slots) {
            for (int slot : slots)
                blackList(slot);

            return this;
        }

        /**
         * This tells us where the item should stop.
         *
         * @param slot The slot to stop at.
         * @return The Builder object itself.
         * @apiNote If this method is used, {@link Pagination#setItemsPerPage(int)} is ignored.
         */
        public @NotNull Builder endPosition(@Nonnegative int slot) {
            this.endPosition = slot;
            return this;
        }

        /**
         * This tells us where the item should stop.
         *
         * @param row    The row to stop at.
         * @param column The column to stop at.
         * @return The Builder object itself.
         * @apiNote If this method is used, {@link Pagination#setItemsPerPage(int)} is ignored.
         */
        public @NotNull Builder endPosition(@Nonnegative int row, @Nonnegative int column) {
            this.endPosition = SlotUtils.toSlot(row, column);
            return this;
        }

        /**
         * Sets the slot to start at.
         *
         * @param startSlot The slot to start at.
         * @return The Builder object itself.
         */
        public @NotNull Builder slot(@Nonnegative int startSlot) {
            this.slot = startSlot;
            return this;
        }

        /**
         * Sets the slot to start at.
         *
         * @param row    The row to start at.
         * @param column The column to start at.
         * @return The Builder object itself.
         */
        public @NotNull Builder slot(@Nonnegative int row, @Nonnegative int column) {
            this.row = row;
            this.column = column;
            return this;
        }

        /**
         * This tells us whether the items should be placed vertically or horizontally.
         *
         * @param type The type of the iterator.
         * @return The builder object itself.
         */
        public @NotNull Builder type(@NotNull SlotIteratorType type) {
            this.type = type;
            return this;
        }

        /**
         * This is used to overwrite items.
         *
         * @return The Builder object itself.
         */
        public @NotNull Builder override() {
            this.override = true;
            return this;
        }

        public SlotIterator build() {
            SlotIterator slotIterator = new SlotIterator();
            slotIterator.slot = this.slot;
            slotIterator.row = this.row;
            slotIterator.column = this.column;
            slotIterator.type = this.type;
            slotIterator.override = this.override;
            slotIterator.blackList = this.blackList;
            slotIterator.endPosition = this.endPosition;

            if ((this.slot >= this.endPosition) && this.endPosition != -1)
                throw new IllegalArgumentException("The start slot must be smaller than the end slot");

            return slotIterator;
        }
    }

    /**
     * @return the start slot
     */
    public int getSlot() {
        return this.slot;
    }

    /**
     * @return the start column
     */
    public int getColumn() {
        return this.column;
    }

    /**
     * @return the start row
     */
    public int getRow() {
        return this.row;
    }

    /**
     * @return the SlotIteratorType
     */
    public @NotNull SlotIteratorType getType() {
        return this.type;
    }

    /**
     * @return true if items can be overwritten.
     */
    public boolean isOverride() {
        return this.override;
    }

    /**
     * @return where the last item should be placed.
     * @apiNote If the endPosition was set, the {@link Pagination#setItemsPerPage(int)} specification is ignored.
     */
    public int getEndPosition() {
        return this.endPosition;
    }

    /**
     * @return all slots where no items should be placed.
     */
    public @NotNull List<Integer> getBlackList() {
        return this.blackList;
    }

    public enum SlotIteratorType {
        HORIZONTAL,
        VERTICAL
    }

}
