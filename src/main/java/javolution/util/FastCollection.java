/*
 * Javolution - Java(TM) Solution for Real-Time and Embedded Systems
 * Copyright (C) 2012 - Javolution (http://javolution.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javolution.util;

import javolution.lang.Immutable;
import javolution.lang.Realtime;
import javolution.util.function.*;
import javolution.util.internal.collection.*;
import javolution.util.service.CollectionService;
import org.javolution.text.TextBuilder;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

import static javolution.lang.Realtime.Limit.*;

/**
 * <p> A closure-based collection supporting numerous views which can be chained.
 * <ul>
 *    <li>{@link #unmodifiable} - View which does not allow any modification.</li>
 *    <li>{@link #filtered filtered(filter)} - View exposing only the elements matching the specified filter.</li>
 *    <li>{@link #mapped mapped(function)} - View exposing elements through the specified mapping function.</li>
 *    <li>{@link #sorted sorted(comparator)} - View exposing elements sorted according to their natural order 
 *                          of using a specified comparator.</li>
 *    <li>{@link #reversed} - View exposing elements in the reverse iterative order.</li>
 *    <li>{@link #distinct} - View exposing each element only once.</li>
 * </ul></p>
 * 
 * <p> Unmodifiable collections are not always immutable. An {@link Immutable immutable}. 
 *     reference (or const reference) can only be {@link #toImmutable() obtained} when the originator  
 *     guarantees that the collection source will not be modified even by himself 
 *     (the value of the immutable reference being an {@link #unmodifiable unmodifiable} collection).
 * [code]
 * Immutable<List<String>> winners 
 *     = new FastTable<String>().addAll("John Deuff", "Otto Graf", "Sim Kamil").toImmutable();
 *     // Immutability is guaranteed, no reference left on the collection source.
 * [/code]</p>
 * 
 * [code]
 * FastTable<String> tokens = ...
 * ...
 * // Replace null with "" in tokens.
 * tokens.update(new Consumer<List<String>>() {  
 *     public void accept(List<String> view) {
 *         for (int i=0, n = view.size(); i < n; i++)
 *             if (view.get(i) == null) view.set(i, "");
 *         }
 *     }
 * });[/code]</p>
 * <p> The same code using closure (Java 8).
 * [code]
 *  tokens.update((List<String> view) -> {
 *      for (int i = 0, n = view.size(); i < n; i++) {
 *          if (view.get(i) == null) view.set(i, "");
 *      }
 *  });[/code]</p>
 * 
 * <p> Views are similar to <a href="http://lambdadoc.net/api/java/util/stream/package-summary.html">
 *     Java 8 streams</a> except that views are themselves collections (virtual collections)
 *     and actions on a view can impact the original collection. Collection views are nothing "new" 
 *     since they already existed in the original java.util collection classes (e.g. List.subList(...),
 *     Map.keySet(), Map.values()). Javolution extends to this concept and allows views to be chained 
 *     which addresses the concern of class proliferation.</p> 
 * [code]
 * FastTable<String> names = new FastTable<String>().addAll("Oscar Thon", "Eva Poret", "Paul Auchon");
 * boolean found = names.comparator(Equalities.LEXICAL_CASE_INSENSITIVE).contains("LUC SURIEUX"); 
 * names.subTable(0, n).clear(); // Removes the n first names (see java.util.List.subList).
 * names.distinct().add("Guy Liguili"); // Adds "Guy Liguili" only if not already present.
 * names.filtered(isLong).clear(); // Removes all the persons with long names.
 * ...
 * Predicate<CharSequence> isLong = new Predicate<CharSequence>() { 
 *     public boolean test(CharSequence csq) {
 *         return csq.length() > 16; 
 *     }
 * });[/code]</p>
 *    
 * <p> Views can of course be used to perform "stream" oriented filter-map-reduce operations with the same benefits:
 *     Parallelism support, excellent memory characteristics (no caching and cost nothing to create), etc.
 * [code]
 * String anyLongName = names.filtered(isLong).any(String.class); // Returns any long name.
 * int nbrChars = names.mapped(toLength).reduce(Reducers.sum()); // Returns the total number of characters.
 * int maxLength = names.mapped(toLength).parallel().max(); // Finds the longest name in parallel.
 * ...
 * Function<CharSequence, Integer> toLength = new Function<CharSequence, Integer>() {
 *    public Integer apply(CharSequence csq) {
 *        return csq.length(); 
 *    }
 * });
 *    
 * // JDK Class.getEnclosingMethod using Javolution's views and Java 8 (to be compared with the current 20 lines implementation !).
 * Method matching = new FastTable<Method>().addAll(enclosingInfo.getEnclosingClass().getDeclaredMethods())
 *     .filtered(m -> Equalities.STANDARD.areEqual(m.getName(), enclosingInfo.getName())
 *     .filtered(m -> Equalities.ARRAY.areEqual(m.getParameterTypes(), parameterClasses))
 *     .filtered(m -> Equalities.STANDARD.areEqual(m.getReturnType(), returnType))
 *     .any(Method.class);
 * if (matching == null) throw new InternalError("Enclosing method not found");
 * return matching;[/code]</p>
 *           
 * <p> With Java 8, closures are greatly simplified using lambda expressions.
 * [code]
 * names.sorted().reversed().forEach(str -> System.out.println(str)); // Prints names in reverse alphabetical order. 
 * [/code]</p>
 *     
 * @author <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 6.0, July 21, 2013
 */
@Realtime
public abstract class FastCollection<E> implements Collection<E>, Serializable {

    private static final long serialVersionUID = 0x600L; // Version.

    /**
     * Default constructor.
     */
    protected FastCollection() {}

    ////////////////////////////////////////////////////////////////////////////
    // Views.
    //

    /**
     * Returns an unmodifiable view over this collection. Any attempt to 
     * modify the collection through this view will result into 
     * a {@link UnsupportedOperationException} being raised.
     */
    public FastCollection<E> unmodifiable() {
        return new UnmodifiableCollectionImpl<E>(service());
    }

    /** 
     * Returns a view exposing only the elements matching the specified 
     * filter.  Adding elements not matching the specified filter has 
     * no effect. If this collection is initially empty, using a filtered
     * view to add new elements ensure that this collection has only elements
     * satisfying the filter predicate.
     */
    public FastCollection<E> filtered(Predicate<? super E> filter) {
        return new FilteredCollectionImpl<E>(service(), filter);
    }

    /** 
     * Returns a view exposing elements through the specified mapping function.
     * The returned view does not allow new elements to be added.
     */
    public <R> FastCollection<R> mapped(
            Function<? super E, ? extends R> function) {
        return new MappedCollectionImpl<E, R>(service(), function);
    }

    /** 
     * Returns a view exposing elements sorted according to the 
     * collection {@link #comparator() order}. 
     */
    public FastCollection<E> sorted() {
        return new SortedCollectionImpl<E>(service(), comparator());
    }

    /** 
     * Returns a view exposing elements sorted according to the specified 
     * comparator.
     */
    public FastCollection<E> sorted(Comparator<? super E> cmp) {
        return new SortedCollectionImpl<E>(service(), cmp);
    }

    /** 
     * Returns a view exposing elements in reverse iterative order.
     */
    public FastCollection<E> reversed() {
        return new ReversedCollectionImpl<E>(service());
    }

    /** 
     * Returns a view exposing only distinct elements (it does not iterate twice 
     * over the {@link #comparator() same} elements). Adding elements already 
     * in the collection through this view has no effect. If this collection is 
     * initially empty, using a distinct view to add new elements ensures that
     * this collection has no duplicate.  
     */
    public FastCollection<E> distinct() {
        return new DistinctCollectionImpl<E>(service());
    }

    ////////////////////////////////////////////////////////////////////////////
    // Closure operations.
    //

    /** 
     * Executes the specified read-only action on this collection.
     *    
     * @param action the read-only action.
     * @throws UnsupportedOperationException if the action tries to update 
     *         this collection and this collection is thread-safe.
     * @throws ClassCastException if the action type is not compatible with 
     *         this collection (e.g. action on set and this is a list). 
     * @see #update(Consumer)
     */
    @SuppressWarnings("unchecked")
    @Realtime(limit = LINEAR)
    public void perform(Consumer<? extends Collection<E>> action) {
        service().perform((Consumer<CollectionService<E>>) action, service());
    }

    /** 
     * Executes the specified update action on this collection.
     *    
     * @param action the update action.
     * @throws ClassCastException if the action type is not compatible with 
     *         this collection (e.g. action on set and this is a list). 
     * @see #perform(Consumer)
     */
    @SuppressWarnings("unchecked")
    @Realtime(limit = LINEAR)
    public void update(Consumer<? extends Collection<E>> action) {
        service().update((Consumer<CollectionService<E>>) action, service());
    }

    /** 
     * Iterates over all this collection elements applying the specified 
     * consumer (convenience method).
     * 
     * @param consumer the functional consumer applied to the collection elements.
     */
    @Realtime(limit = LINEAR)
    public void forEach(final Consumer<? super E> consumer) {
        perform(new Consumer<Collection<E>>() {
            public void accept(Collection<E> view) {
                Iterator<E> it = view.iterator();
                while (it.hasNext()) {
                    consumer.accept(it.next());
                }
            }
        });
    }

    /**
     * Removes from this collection all the elements matching the specified
     * functional predicate (convenience method).
     * 
     * @param filter a predicate returning {@code true} for elements to be removed.
     * @return {@code true} if at least one element has been removed;
     *         {@code false} otherwise.
     */
    @Realtime(limit = LINEAR)
    public boolean removeIf(final Predicate<? super E> filter) {
        final boolean[] removed = new boolean[1];
        update(new Consumer<Collection<E>>() {
            public void accept(Collection<E> view) {
                Iterator<E> it = view.iterator();
                while (it.hasNext()) {
                    if (filter.test(it.next())) {
                        it.remove(); // Ok mutable iteration.
                        removed[0] = true;
                    }
                }
            }
        });
        return removed[0];
    }

    /** 
     * Performs a reduction of the elements of this collection using the 
     * specified reducer. This may not involve iterating  over all the 
     * collection elements, for example the reducers: {@link Reducers#any},
     * {@link Reducers#and} and {@link Reducers#or} may stop iterating 
     * early.
     * 
     * @param reducer the collection reducer.
     * @return the reduction result.
     */
    @Realtime(limit = LINEAR)
    public E reduce(Reducer<E> reducer) {
        perform(reducer);
        return reducer.get();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Collection operations.
    //

    /** Adds the specified element to this collection */
    @Override
    @Realtime(limit = LINEAR, comment = "May have to search the whole collection (e.g. distinct view).")
    public boolean add(E element) {
        return service().add(element);
    }

    /** Indicates if this collection is empty. */
    @Override
    @Realtime(limit = LINEAR, comment = "May actually iterates the whole collection (e.g. filtered view).")
    public boolean isEmpty() {
        return iterator().hasNext();
    }

    /** Returns the size of this collection. */
    @Override
    @Realtime(limit = LINEAR, comment = "Potentially counts the elements.")
    public int size() {
        return service().size();
    }

    /** Removes all elements from this collection. */
    @Override
    @Realtime(limit = LINEAR, comment = "Potentially removes elements one at a time.")
    public void clear() {
        service().clear();
    }

    /** Indicates if this collection contains the specified element. */
    @Override
    @Realtime(limit = LINEAR, comment = "Potentially searches the whole collection.")
    public boolean contains(Object searched) {
        return service().contains(searched);
    }

    /** Removes the specified element from this collection. */
    @Override
    @Realtime(limit = LINEAR, comment = "Potentially searches the whole collection.")
    public boolean remove(Object searched) {
        return service().remove(searched);
    }

    /**
     * Returns an iterator over this collection elements. 
     * In other words the elements iterated over
     * may or may not reflect the current state of the collection.
     */
    @Override
    @Realtime(limit = N_SQUARE, comment = "Construction of the iterator may require sorting the elements (e.g. sorted view)")
    public Iterator<E> iterator() {
        return service().iterator();
    }

    /** Adds all the specified elements to this collection. */
    @Override
    @Realtime(limit = LINEAR)
    public boolean addAll(final Collection<? extends E> that) {
        return service().addAll(that);
    }

    /** Indicates if this collection contains all the specified elements. */
    @Override
    @Realtime(limit = N_SQUARE)
    public boolean containsAll(Collection<?> that) {
        return service().containsAll(that);
    }

    /** Removes all the specified element from this collection. */
    @Override
    @Realtime(limit = N_SQUARE)
    public boolean removeAll(final Collection<?> that) {
        return service().removeAll(that);
    }

    /** Removes all the elements except those in the specified collection. */
    @Override
    @Realtime(limit = N_SQUARE)
    public boolean retainAll(final Collection<?> that) {
        return service().retainAll(that);
    }

    /** Returns an array holding this collection elements. */
    @Override
    @Realtime(limit = LINEAR)
    public Object[] toArray() {
        return service().toArray();
    }

    /** Returns the specified array holding this collection elements if 
     *  enough capacity. */
    @Override
    @Realtime(limit = LINEAR)
    public <T> T[] toArray(final T[] array) {
        return service().toArray(array);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Misc.
    //

    /**
     * Returns any non-null element of the specified type (convenience method).
     * 
     * @param type the element type searched for.
     * @return {@code reduce(Reducers.any(type))}
     * @see Reducers#any
     */
    @SuppressWarnings("unchecked")
    @Realtime(limit = LINEAR)
    public <T extends E> T any(Class<T> type) {
        return (T) reduce((Reducer<E>) Reducers.any(type));
    }

    /**
     * Returns the smallest element of this collection using this 
     * collection {@link #comparator() comparator} (convenience method).
     * Returns {@code null} if this collection is empty. 
     * 
     * @return {@code reduce(Reducers.min(comparator()))}
     * @see Reducers#min
     */
    @Realtime(limit = LINEAR)
    public E min() {
        return reduce(Reducers.min(comparator()));
    }

    /**
     * Returns the largest element of this collection using this 
     * collection {@link #comparator() comparator} (convenience method). 
     * Returns {@code null} if this collection is empty. 
     * 
     * @return {@code reduce(Reducers.max(comparator()))}
     * @see Reducers#max
     */
    @Realtime(limit = LINEAR)
    public E max() {
        return reduce(Reducers.max(comparator()));
    }

    /**
     * Returns this collection with the specified element added. 
     * 
     * @param elements the elements to be added.
     * @return {@code this}
     */
    @Realtime(limit = LINEAR)
    public FastCollection<E> addAll(E... elements) {
        for (E e : elements) {
            add(e);
        }
        return this;
    }

    /**
     * Returns this collection with the specified collection's elements added
     * in sequence. 
     */
    @Realtime(limit = LINEAR)
    public FastCollection<E> addAll(FastCollection<? extends E> that) {
        addAll((Collection<? extends E>) that);
        return this;
    }

    /** 
     * Returns the comparator uses by this collection for equality and/or 
     * ordering if this collection is sorted.
     */
    @Realtime(limit = CONSTANT)
    public Equality<? super E> comparator() {
        return service().comparator();
    }

    /** 
     * Returns an immutable reference over this collection. The immutable 
     * value is an {@link #unmodifiable() unmodifiable} view of this collection.
     * The caller must guarantees that the original collection is never going 
     * to be updated (e.g. there is no reference left of the original collection).
     */
    @Realtime(limit = CONSTANT)
    public <T extends Collection<E>> Immutable<T> toImmutable() {
        return new Immutable<T>() {
            @SuppressWarnings("unchecked")
            final T value = (T) unmodifiable();

            @Override
            public T value() {
                return value;
            }

        };
    }

    /**
     * Compares the specified object with this collection for equality.
     * This method follows the {@link Collection#equals(Object)} specification 
     * if this collection {@link #comparator comparator} is 
     * {@link Equalities#STANDARD} (default). Otherwise, only collections
     * using the same comparator can be considered equals.  
     * 
     * @param obj the object to be compared for equality with this collection
     * @return <code>true</code> if both collections are considered equals;
     *        <code>false</code> otherwise. 
     */
    @Override
    @Realtime(limit = LINEAR)
    public boolean equals(Object obj) {
        return service().equals(obj);
    }

    /**
     * Returns the hash code of this collection.
     * This method follows the {@link Collection#hashCode()} specification 
     * if this collection {@link #comparator comparator} is 
     * {@link Equalities#STANDARD}.
     *    
     * @return this collection hash code. 
     */
    @Override
    @Realtime(limit = LINEAR)
    public int hashCode() {
        return service().hashCode();
    }

    /** Returns the string representation of this collection. */
    @Override
    @org.javolution.annotations.Realtime(limit = org.javolution.annotations.Realtime.Limit.LINEAR)
    public String toString() {
        Iterator<?> i = iterator();
        TextBuilder dest = new TextBuilder();
        dest.append('[');
        while (i.hasNext()) {
            dest.append(i.next());
            if (i.hasNext()) {
                dest.append(',').append(' ');
            }
        }
        return dest.append(']').toString();
    }

    /**
     * Returns the service implementation of this collection (for sub-classes).
     */
    protected abstract CollectionService<E> service();

    /**
     * Returns the service implementation of any fast collection 
     * (for sub-classes).
     */
    protected static <E> CollectionService<E> serviceOf(
            FastCollection<E> collection) {
        return collection.service();
    }
}