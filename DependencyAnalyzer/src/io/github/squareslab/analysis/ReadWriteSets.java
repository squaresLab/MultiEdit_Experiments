package io.github.squareslab.analysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Immutable
 * @param <T> Representation for variables
 */
class ReadWriteSets<T>
{
	private Set<T> readVariables;
	private Set<T> writtenVariables;

	/**
	 Get an empty ReadWriteSets
	 */
	public ReadWriteSets()
	{
		readVariables = new HashSet<>();
		writtenVariables = new HashSet<>();
	}

	public ReadWriteSets(Collection<T> reads, Collection<T> writes)
	{
		readVariables = new HashSet<>(reads);
		writtenVariables = new HashSet<>(writes);
	}

	/**
	 Clone constructor
	 */
	public ReadWriteSets(ReadWriteSets<T> readWriteSets)
	{
		this.readVariables = readWriteSets.readVariables;
		this.writtenVariables = readWriteSets.writtenVariables;
	}

	/**
	 * Clones itself and adds new reads and writes.
	 * @param newReads
	 * @param newWrites
	 * @return
	 */
	public ReadWriteSets<T> addReadsAndWrites(Collection<T> newReads, Collection<T> newWrites)
	{
		ReadWriteSets<T> newSet = new ReadWriteSets<>(this);
		newSet.readVariables.addAll(newReads);
		newSet.writtenVariables.addAll(newWrites);
		return newSet;
	}

	/**
	 * Clones itself and unions read & written variable sets.
	 * @param other
	 * @return
	 */
	public ReadWriteSets<T> mergeWith(ReadWriteSets<T> other)
	{
		return addReadsAndWrites(other.readVariables, other.writtenVariables);
	}

	/**
	 *
	 * @return an iterator over readVariables
	 */
	public Iterator<T> readsIterator()
	{
		return readVariables.iterator();
	}

	/**
	 *
	 * @return an iterator over writtenVariables
	 */
	public Iterator<T> writesIterator()
	{
		return writtenVariables.iterator();
	}

	/**
	 *
	 * @param candidateRead
	 * @return whether candidateRead is in the readVariables set
	 */
	public boolean hasRead(T candidateRead)
	{
		return readVariables.contains(candidateRead);
	}

	/**
	 *
	 * @param candidateWrite
	 * @return whether candidateWrite is in the writtenVariables set
	 */
	public boolean hasWrittenTo(T candidateWrite)
	{
		return writtenVariables.contains(candidateWrite);
	}
}
