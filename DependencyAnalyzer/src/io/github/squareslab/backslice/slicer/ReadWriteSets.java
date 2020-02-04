package io.github.squareslab.backslice.slicer;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

//Immutable
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
	public ReadWriteSets<T> addReadsAndWrites(Set<T> newReads, Set<T> newWrites)
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
	 * @param candidateWrite
	 * @return whether candidateWrite is in the writtenVariables set
	 */
	public boolean hasWrittenTo(T candidateWrite)
	{
		return writtenVariables.contains(candidateWrite);
	}
}
