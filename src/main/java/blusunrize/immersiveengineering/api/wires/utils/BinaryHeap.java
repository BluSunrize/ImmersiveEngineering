/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.wires.utils;

import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class BinaryHeap<T>
{
	private final Comparator<T> compare;
	private List<HeapEntry<T>> storage = new ArrayList<>();

	public BinaryHeap(Comparator<T> compare)
	{
		this.compare = compare;
	}

	public HeapEntry<T> insert(T newElement)
	{
		HeapEntry<T> ret = new HeapEntry<>(newElement, storage.size(), this);
		storage.add(ret);
		siftUp(ret);
		return ret;
	}

	public T extractMin()
	{
		HeapEntry<T> ret = storage.get(0);
		swap(0, -1);
		storage.remove(storage.size()-1);
		ret.invalidate();
		if(!empty())
			siftDown(storage.get(0));
		return ret.getValue();
	}

	public void decreaseKey(HeapEntry<T> entry)
	{
		entry.testValidity(this);
		siftUp(entry);
	}

	public boolean empty()
	{
		return storage.isEmpty();
	}

	private void siftDown(HeapEntry<T> entry)
	{
		while(entry!=null)
		{
			Optional<HeapEntry<T>> left = left(entry);
			Optional<HeapEntry<T>> right = right(entry);
			HeapEntry<T> min = entry;
			if(left.isPresent()&&compare.compare(left.get().getValue(), min.getValue()) < 0)
				min = left.get();
			if(right.isPresent()&&compare.compare(right.get().getValue(), min.getValue()) < 0)
				min = right.get();
			if(min!=entry)
				swap(min.location, entry.location);
			else
				entry = null;
		}
	}

	private void siftUp(HeapEntry<T> entry)
	{
		while(entry!=null)
		{
			Optional<HeapEntry<T>> up = up(entry);
			if(up.isPresent()&&compare.compare(up.get().getValue(), entry.getValue()) > 0)
				swap(entry.getLocation(), up.get().getLocation());
			else
				entry = null;
		}
	}

	private void swap(int indexA, int indexB)
	{
		if(indexA==indexB)
			return;
		if(indexA < 0)
			indexA += storage.size();
		if(indexB < 0)
			indexB += storage.size();
		HeapEntry<T> entryA = storage.get(indexA);
		HeapEntry<T> entryB = storage.get(indexB);
		entryA.setLocation(indexB);
		entryB.setLocation(indexA);
		storage.set(indexA, entryB);
		storage.set(indexB, entryA);
	}

	private Optional<HeapEntry<T>> up(HeapEntry<T> here)
	{
		if(here.getLocation()==0)
			return Optional.empty();
		else
			return Optional.of(storage.get((here.getLocation()-1)/2));
	}

	private Optional<HeapEntry<T>> left(HeapEntry<T> here)
	{
		int retIndex = here.getLocation()*2+1;
		if(retIndex < storage.size())
			return Optional.of(storage.get(retIndex));
		else
			return Optional.empty();
	}

	private Optional<HeapEntry<T>> right(HeapEntry<T> here)
	{
		int retIndex = here.getLocation()*2+2;
		if(retIndex < storage.size())
			return Optional.of(storage.get(retIndex));
		else
			return Optional.empty();
	}

	private void validate()
	{
		for(HeapEntry<T> entry : storage)
		{
			entry.testValidity(this);
			Optional<HeapEntry<T>> right = right(entry);
			Preconditions.checkState(!right.isPresent()||compare.compare(right.get().getValue(), entry.getValue()) >= 0);
			Optional<HeapEntry<T>> left = left(entry);
			Preconditions.checkState(!left.isPresent()||compare.compare(left.get().getValue(), entry.getValue()) >= 0);
		}
	}

	public static class HeapEntry<T>
	{
		private final T value;
		private int location;
		@Nullable
		private BinaryHeap<T> owner;

		public HeapEntry(T value, int location, @Nonnull BinaryHeap<T> owner)
		{
			this.value = value;
			this.location = location;
			this.owner = owner;
		}

		private int getLocation()
		{
			return location;
		}

		private void setLocation(int newValue)
		{
			location = newValue;
		}

		public T getValue()
		{
			return value;
		}

		private void invalidate()
		{
			owner = null;
		}

		private void testValidity(BinaryHeap<T> potentialOwner)
		{
			Preconditions.checkArgument(potentialOwner==owner);
		}
	}
}
