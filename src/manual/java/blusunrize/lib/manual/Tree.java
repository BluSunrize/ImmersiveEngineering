/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.DoubleSupplier;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

public class Tree<NT extends Comparable<NT>, LT extends Comparable<LT>>
{
	private final InnerNode<NT, LT> root;

	public Tree(NT root)
	{
		this.root = new InnerNode<>(root, null, () -> 0);
	}

	public Stream<LT> leafStream()
	{
		return root.leafStream().map(Leaf::getLeafData);
	}

	public InnerNode<NT, LT> getRoot()
	{
		return root;
	}

	public Stream<AbstractNode<NT, LT>> fullStream()
	{
		Stream.Builder<AbstractNode<NT, LT>> b = Stream.builder();
		root.stream(b, false);
		return b.build();
	}

	public void sortAll()
	{
		root.resetWeights();
		root.sortChildren();
	}

	public static abstract class AbstractNode<NT extends Comparable<NT>, LT extends Comparable<LT>>
	{
		@Nullable
		private final InnerNode<NT, LT> superNode;
		private final DoubleSupplier weight;
		private OptionalDouble cachedWeight;

		AbstractNode(@Nullable InnerNode<NT, LT> superNode, DoubleSupplier weight)
		{
			this.superNode = superNode;
			this.weight = weight;
		}

		public abstract boolean isLeaf();

		public NT getNodeData()
		{
			return null;
		}

		public LT getLeafData()
		{
			return null;
		}

		public List<AbstractNode<NT, LT>> getChildren()
		{
			return ImmutableList.of();
		}

		@Nullable
		public InnerNode<NT, LT> getSuperNode()
		{
			return superNode;
		}

		protected abstract void stream(Stream.Builder<AbstractNode<NT, LT>> builder, boolean leafStream);

		public double getWeight()
		{
			if(!cachedWeight.isPresent())
				cachedWeight = OptionalDouble.of(weight.getAsDouble());
			return cachedWeight.getAsDouble();
		}

		void resetWeights()
		{
			cachedWeight = OptionalDouble.empty();
		}

		public final Stream<Leaf<NT, LT>> leafStream()
		{
			Stream.Builder<AbstractNode<NT, LT>> b = Stream.builder();
			stream(b, true);
			return b.build().map(node -> (Leaf<NT, LT>)node);
		}
	}

	public static class InnerNode<NT extends Comparable<NT>, LT extends Comparable<LT>> extends AbstractNode<NT, LT>
	{
		private final List<AbstractNode<NT, LT>> children = new ArrayList<>();

		private final Comparator<AbstractNode<NT, LT>> compare =
				Comparator.<AbstractNode<NT, LT>, Boolean>comparing(AbstractNode::isLeaf)
						.thenComparingDouble(AbstractNode::getWeight)
						//Nulls should be irrelevant (either both or neither are null)
						.thenComparing(AbstractNode::getNodeData, Comparator.nullsFirst(Comparator.naturalOrder()))
						.thenComparing(AbstractNode::getLeafData, Comparator.nullsFirst(Comparator.naturalOrder()));
		private final NT data;

		InnerNode(NT data, @Nullable InnerNode<NT, LT> superNode, DoubleSupplier weight)
		{
			super(superNode, weight);
			this.data = data;
		}

		@Override
		public List<AbstractNode<NT, LT>> getChildren()
		{
			synchronized(children)
			{
				// Do not allow external modification, otherwise we'll get CMEs we can't trace
				return ImmutableList.copyOf(children);
			}
		}

		@Override
		public boolean isLeaf()
		{
			return false;
		}

		@Override
		public NT getNodeData()
		{
			return data;
		}

		public InnerNode<NT, LT> addNewSubnode(NT data, int weight)
		{
			return addNewSubnode(data, () -> weight);
		}

		public InnerNode<NT, LT> addNewSubnode(NT data, DoubleSupplier weight)
		{
			synchronized(children)
			{
				InnerNode<NT, LT> newNode = new InnerNode<>(data, this, weight);
				children.add(newNode);
				return newNode;
			}
		}

		public InnerNode<NT, LT> getOrCreateSubnode(NT data)
		{
			return getOrCreateSubnode(data, 0);
		}

		public InnerNode<NT, LT> getOrCreateSubnode(NT data, int weight)
		{
			return getOrCreateSubnode(data, () -> weight);
		}

		public InnerNode<NT, LT> getOrCreateSubnode(NT data, DoubleSupplier weight)
		{
			return getSubnode(data)
					.orElseGet(() -> addNewSubnode(data, weight));
		}

		public Optional<InnerNode<NT, LT>> getSubnode(NT data)
		{
			synchronized(children)
			{
				for(AbstractNode<NT, LT> child : children)
					if(!child.isLeaf()&&data.equals(child.getNodeData()))
						return Optional.of((InnerNode<NT, LT>)child);
			}
			return Optional.empty();
		}

		public void removeLeaf(LT data)
		{
			synchronized(children)
			{
				children.removeIf(child -> child.isLeaf()&&((Leaf<NT, LT>)child).data.equals(data));
			}
		}

		public void removeSubnode(NT data)
		{
			synchronized(children)
			{
				children.removeIf(child -> !child.isLeaf()&&((InnerNode<NT, LT>)child).data.equals(data));
			}
		}

		public void addNewLeaf(LT data)
		{
			addNewLeaf(data, 0);
		}

		public void addNewLeaf(LT data, int weight)
		{
			addNewLeaf(data, () -> weight);
		}

		public void addNewLeaf(LT data, DoubleSupplier weight)
		{
			synchronized(children)
			{
				Leaf<NT, LT> newLeaf = new Leaf<>(data, this, weight);
				children.add(newLeaf);
			}
		}

		public void sortChildren()
		{
			synchronized(children)
			{
				children.sort(compare);
			}
			for(AbstractNode<NT, LT> c : getChildren())
				if(c instanceof InnerNode)
					((InnerNode<NT, LT>)c).sortChildren();
		}

		protected void stream(Builder<AbstractNode<NT, LT>> builder, boolean leafStream)
		{
			if(!leafStream)
				builder.accept(this);
			for(AbstractNode<NT, LT> child : getChildren())
				child.stream(builder, leafStream);
		}

		@Override
		void resetWeights()
		{
			super.resetWeights();
			getChildren().forEach(AbstractNode::resetWeights);
		}
	}

	public static class Leaf<NT extends Comparable<NT>, LT extends Comparable<LT>> extends AbstractNode<NT, LT>
	{
		private final LT data;

		Leaf(LT data, @Nullable InnerNode<NT, LT> superNode, DoubleSupplier weight)
		{
			super(superNode, weight);
			this.data = data;
		}

		@Override
		public boolean isLeaf()
		{
			return true;
		}

		@Override
		public LT getLeafData()
		{
			return data;
		}

		@Override
		protected void stream(Stream.Builder<AbstractNode<NT, LT>> builder, boolean leafStream)
		{
			builder.accept(this);
		}
	}
}