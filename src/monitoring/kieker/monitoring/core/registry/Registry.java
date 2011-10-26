/***************************************************************************
 * Copyright 2011 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.monitoring.core.registry;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import kieker.common.record.HashRecord;
import kieker.common.record.IMonitoringRecordReceiver;
import kieker.monitoring.core.controller.MonitoringController;

/**
 * Based upon ConcurrentHashMap.
 * The basic strategy is to subdivide the table among Segments, each of which itself is a concurrently readable hash table.
 * 
 * TODO: check License
 * 
 * @author Jan Waller
 */
public class Registry<E> implements IRegistry<E> {
	private static final int INITIAL_CAPACITY = 16;
	private static final float LOAD_FACTOR = 0.75f;
	private static final int CONCURRENCY_LEVEL = 16;
	private static final int MAXIMUM_CAPACITY = 1 << 30;

	private static final IMonitoringRecordReceiver CTRL = MonitoringController.getInstance();

	/**
	 * Mask value for indexing into segments. The upper bits of a key's hash code are used to choose the segment.
	 */
	private final int segmentMask;

	/**
	 * Shift value for indexing within segments.
	 */
	private final int segmentShift;

	/**
	 * The segments, each of which is a specialized hash table
	 */
	private final Segment<E>[] segments;

	/**
	 * The next id used
	 */
	private final AtomicInteger nextId = new AtomicInteger(0);

	/**
	 * A cached copy of the current assignment of IDs
	 */
	private volatile E[] eArrayCached;

	/**
	 * Creates a new, empty registry with a default initial capacity (32), load factor (0.75) and concurrencyLevel (8).
	 */
	@SuppressWarnings("unchecked")
	public Registry(final boolean sendRecordOnNewEntry) {
		// Find power-of-two sizes best matching arguments
		int sshift = 0;
		int ssize = 1;
		while (ssize < Registry.CONCURRENCY_LEVEL) {
			++sshift;
			ssize <<= 1;
		}
		this.segmentShift = 32 - sshift;
		this.segmentMask = ssize - 1;
		this.segments = new Segment[ssize];
		int c = Registry.INITIAL_CAPACITY / ssize;
		if ((c * ssize) < Registry.INITIAL_CAPACITY) {
			++c;
		}
		int cap = 1;
		while (cap < c) {
			cap <<= 1;
		}
		for (int i = 0; i < this.segments.length; ++i) {
			this.segments[i] = new Segment<E>(cap, Registry.LOAD_FACTOR, sendRecordOnNewEntry);
		}
		this.eArrayCached = (E[]) new Object[0];
	}

	/**
	 * Applies a supplemental hash function to a given hashCode, which defends against poor quality hash functions. This is critical because ConcurrentHashMap uses
	 * power-of-two length hash tables, that otherwise encounter collisions for hashCodes that do not differ in lower or upper bits.
	 */
	private static final int hash(final Object value) {
		// Spread bits to regularize both segment and index locations, using variant of single-word Wang/Jenkins hash.
		int h = value.hashCode();
		h += (h << 15) ^ 0xffffcd7d;
		h ^= (h >>> 10);
		h += (h << 3);
		h ^= (h >>> 6);
		h += (h << 2) + (h << 14);
		return h ^ (h >>> 16);
	}

	@Override
	public int get(final E value) {
		final int hash = Registry.hash(value);
		return this.segments[(hash >>> this.segmentShift) & this.segmentMask].get(value, hash, this.nextId);
	}

	@Override
	public E get(final int id) {
		if (id > this.nextId.get()) {
			return null;
		}
		return this.getAll()[id];
	}

	/**
	 * TODO: the table may miss values at the end!
	 * 
	 * @return
	 */
	public E[] getAll() {
		final int capacity = this.nextId.get();
		if (this.eArrayCached.length != capacity) {
			@SuppressWarnings("unchecked")
			final E[] eArray = ((E[]) new Object[capacity]);
			for (final Segment<E> segment : this.segments) {
				segment.insertIntoArray(eArray);
			}
			this.eArrayCached = eArray;
		}
		return this.eArrayCached;
	}

	/* ---------------- Inner Classes -------------- */

	/**
	 * Registry entry.
	 */
	private static final class HashEntry<E> {
		private final E value;
		private final int hash;
		private final int id;
		private final HashEntry<E> next;

		private HashEntry(final E value, final int hash, final int id, final HashEntry<E> next) {
			this.value = value;
			this.hash = hash;
			this.id = id;
			this.next = next;
		}
	}

	/**
	 * Segments are specialized versions of hash tables. This subclasses from ReentrantLock opportunistically, just to simplify some locking and avoid separate
	 * construction.
	 */
	private static final class Segment<E> extends ReentrantLock {
		/*
		 * Segments maintain a table of entry lists that are ALWAYS kept in a consistent state, so can be read without locking. Next fields of nodes are immutable
		 * (final). All list additions are performed at the front of each bin. This makes it easy to check changes, and also fast to traverse. When nodes would
		 * otherwise be changed, new nodes are created to replace them. This works well for hash tables since the bin lists tend to be short. (The average length is
		 * less than two for the default load factor threshold.)
		 * 
		 * Read operations can thus proceed without locking, but rely on selected uses of volatiles to ensure that completed write operations performed by other
		 * threads are noticed. For most purposes, the "count" field, tracking the number of elements, serves as that volatile variable ensuring visibility. This is
		 * convenient because this field needs to be read in many read operations anyway:
		 * 
		 * - All (unsynchronized) read operations must first read the "count" field, and should not look at table entries if it is 0.
		 * 
		 * - All (synchronized) write operations should write to the "count" field after structurally changing any bin. The operations must not take any action that
		 * could even momentarily cause a concurrent read operation to see inconsistent data. This is made easier by the nature of the read operations in Map. For
		 * example, no operation can reveal that the table has grown but the threshold has not yet been updated, so there are no atomicity requirements for this with
		 * respect to reads.
		 * 
		 * As a guide, all critical volatile reads and writes to the count field are marked in code comments.
		 */

		private static final long serialVersionUID = 1L;

		/**
		 * The number of elements in this segment's region.
		 */
		private transient volatile int count;

		/**
		 * The table is rehashed when its size exceeds this threshold. (The value of this field is always <tt>(int)(capacity * loadFactor)</tt>.)
		 */
		private transient int threshold;

		/**
		 * The per-segment table.
		 */
		private transient volatile HashEntry<E>[] table;

		private transient final boolean sendRecordOnNewEntry;

		@SuppressWarnings("unchecked")
		private Segment(final int initialCapacity, final float lf, final boolean sendRecordOnNewEntry) {
			this.table = new HashEntry[initialCapacity];
			this.threshold = (int) (initialCapacity * lf);
			this.count = 0;
			this.sendRecordOnNewEntry = sendRecordOnNewEntry;
		}

		private void insertIntoArray(final E[] eArray) {
			if (this.count != 0) { // volatile read!
				final int capacity = eArray.length;
				final HashEntry<E>[] tab = this.table;
				for (final HashEntry<E> hashEntry : tab) {
					HashEntry<E> e = hashEntry;
					while (e != null) {
						if (e.id < capacity) {
							eArray[e.id] = e.value;
						}
						e = e.next;
					}
				}
			}
		}

		private int get(final E value, final int hash, final AtomicInteger nextId) {
			HashEntry<E> e = null;
			if (this.count != 0) { // volatile read! search for entry without locking
				final HashEntry<E>[] tab = this.table;
				final int index = hash & (tab.length - 1);
				final HashEntry<E> first = tab[index];
				e = first;
				while ((e != null) && ((e.hash != hash) || !value.equals(e.value))) {
					e = e.next;
				}
			}
			if (e == null) { // not yet present, but have to repeat search
				this.lock();
				try {
					int c = this.count;
					if (c++ > this.threshold) {
						this.rehash();
					}
					final HashEntry<E>[] tab = this.table; // volatile read
					final int index = hash & (tab.length - 1);
					final HashEntry<E> first = tab[index]; // the bin the value may be inside
					e = first;
					while ((e != null) && ((e.hash != hash) || !value.equals(e.value))) {
						e = e.next;
					}
					if (e == null) { // still not found
						final int id = nextId.getAndIncrement();
						tab[index] = new HashEntry<E>(value, hash, id, first);
						this.count = c; // write-volatile
						if (this.sendRecordOnNewEntry) {
							Registry.CTRL.newMonitoringRecord(new HashRecord(id, value));
						}
						return id; // return new id
					}
				} finally {
					this.unlock();
				}
			}
			return e.id; // return id if found
		}

		/**
		 * Reclassify nodes in each list to new Map. Because we are using power-of-two expansion, the elements from each bin must either stay at same index, or
		 * move with a power of two offset. We eliminate unnecessary node creation by catching cases where old nodes can be reused because their next fields
		 * won't change. Statistically, at the default threshold, only about one-sixth of them need cloning when a table doubles. The nodes they replace will be
		 * garbage collectable as soon as they are no longer referenced by any reader thread that may be in the midst of traversing table right now.
		 */
		private void rehash() {
			final HashEntry<E>[] oldTable = this.table;
			final int oldCapacity = oldTable.length;
			if (oldCapacity >= Registry.MAXIMUM_CAPACITY) {
				return;
			}
			@SuppressWarnings("unchecked")
			final HashEntry<E>[] newTable = new HashEntry[oldCapacity << 1];
			this.threshold = (int) (newTable.length * Registry.LOAD_FACTOR);
			final int sizeMask = newTable.length - 1;
			for (int i = 0; i < oldCapacity; i++) {
				// We need to guarantee that any existing reads of old Map can proceed. So we cannot yet null out each bin.
				final HashEntry<E> e = oldTable[i];

				if (e != null) {
					final HashEntry<E> next = e.next;
					final int idx = e.hash & sizeMask;

					// Single node on list
					if (next == null) {
						newTable[idx] = e;
					} else {
						// Reuse trailing consecutive sequence at same slot
						HashEntry<E> lastRun = e;
						int lastIdx = idx;
						for (HashEntry<E> last = next; last != null; last = last.next) { // find end of bin
							final int k = last.hash & sizeMask;
							if (k != lastIdx) {
								lastIdx = k;
								lastRun = last;
							}
						}
						newTable[lastIdx] = lastRun;

						// Clone all remaining nodes
						for (HashEntry<E> p = e; p != lastRun; p = p.next) {
							final int k = p.hash & sizeMask;
							final HashEntry<E> n = newTable[k];
							newTable[k] = new HashEntry<E>(p.value, p.hash, p.id, n);
						}
					}
				}
			}
			this.table = newTable; // volatile write
		}
	}
}
