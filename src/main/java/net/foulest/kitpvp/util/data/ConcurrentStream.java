/*
 * KitPvP - a fully-featured core plugin for the KitPvP gamemode.
 * Copyright (C) 2024 Foulest (https://github.com/Foulest)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package net.foulest.kitpvp.util.data;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A utility class for handling concurrent streams.
 *
 * @param <T> The type of the stream.
 * @see Stream
 * @author Foulest
 */
@Getter
@Setter
@SuppressWarnings("unused")
public final class ConcurrentStream<T> {

    /**
     * The supplier of the stream.
     */
    private final Supplier<Stream<T>> supplier;

    /**
     * The collection of the stream.
     */
    private final Collection<T> collection;

    /**
     * Whether the stream is parallel.
     */
    private final boolean parallel;

    /**
     * Constructs a new concurrent stream.
     *
     * @param list The list to create the stream from.
     * @param parallel Whether the stream is parallel.
     */
    @Contract(pure = true)
    public ConcurrentStream(@NotNull List<T> list, boolean parallel) {
        supplier = list::stream;
        collection = list;
        this.parallel = parallel;
    }

    /**
     * Finds any element in the stream that matches the predicate.
     *
     * @param t The predicate to match.
     * @return Whether any element in the stream matches the predicate.
     */
    public boolean any(Predicate<T> t) {
        return parallel ? supplier.get().parallel().anyMatch(t) : supplier.get().anyMatch(t);
    }

    /**
     * Finds all elements in the stream that match the predicate.
     *
     * @param t The predicate to match.
     * @return Whether all elements in the stream match the predicate.
     */
    public boolean all(Predicate<T> t) {
        return parallel ? supplier.get().parallel().allMatch(t) : supplier.get().allMatch(t);
    }
}
