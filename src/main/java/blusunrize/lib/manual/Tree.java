/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import com.google.common.collect.ImmutableSortedSet;

import javax.annotation.Nullable;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

public class Tree<NT extends Comparable<NT>, LT extends Comparable<LT>>
{
	private Node<NT, LT> root;

	public Tree(NT root)
	{
		this.root = new Node<>(root, null, 0);
	}

	public Stream<LT> leafStream()
	{
		Stream.Builder<AbstractNode<NT, LT>> b = Stream.builder();
		root.stream(b, true);
		return b.build().map(AbstractNode::getLeafData);
	}

	public Node<NT, LT> getRoot()
	{
		return root;
	}

	public Stream<AbstractNode<NT, LT>> fullStream()
	{
		Stream.Builder<AbstractNode<NT, LT>> b = Stream.builder();
		root.stream(b, false);
		return b.build();
	}

	public static abstract class AbstractNode<NT extends Comparable<NT>, LT extends Comparable<LT>>
	{
		@Nullable
		private Node<NT, LT> superNode;
		final int weight;

		AbstractNode(@Nullable Node<NT, LT> superNode, int weight)
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

		public SortedSet<AbstractNode<NT, LT>> getChildren()
		{
			return ImmutableSortedSet.of();
		}

		@Nullable
		public Node<NT, LT> getSuperNode()
		{
			return superNode;
		}

		protected abstract void stream(Stream.Builder<AbstractNode<NT, LT>> builder, boolean leafStream);
	}

	public static class Node<NT extends Comparable<NT>, LT extends Comparable<LT>> extends AbstractNode<NT, LT>
	{
		private final SortedSet<AbstractNode<NT, LT>> children = new TreeSet<>((n1, n2) -> {
			if(n1.weight!=n2.weight)
				return Integer.compare(n1.weight, n2.weight);
			else if(n1.isLeaf()&&!n2.isLeaf())
				return -1;
			else if(!n1.isLeaf()&&n2.isLeaf())
				return 1;
			else if(n1.isLeaf())
				return n1.getLeafData().compareTo(n2.getLeafData());
			else
				return n1.getNodeData().compareTo(n2.getNodeData());
		});
		private NT data;

		public Node(NT data, @Nullable Node<NT, LT> superNode, int weight)
		{
			super(superNode, weight);
			this.data = data;
		}

		@Override
		public SortedSet<AbstractNode<NT, LT>> getChildren()
		{
			return children;
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

		public Node<NT, LT> addNewSubnode(NT data, int weight)
		{
			Node<NT, LT> newNode = new Node<>(data, this, weight);
			children.add(newNode);
			return newNode;
		}

		public Node<NT, LT> getOrCreateSubnode(NT data)
		{
			return getOrCreateSubnode(data, 0);
		}

		public Node<NT, LT> getOrCreateSubnode(NT data, int weight)
		{
			for(AbstractNode<NT, LT> child : children)
			{
				if(!child.isLeaf()&&data.equals(child.getNodeData()))
				{
					return (Node<NT, LT>)child;
				}
			}
			return addNewSubnode(data, weight);
		}

		public void addNewLeaf(LT data)
		{
			addNewLeaf(data, 0);
		}

		public void addNewLeaf(LT data, int weight)
		{
			Leaf<NT, LT> newLeaf = new Leaf<>(data, this, weight);
			children.add(newLeaf);
		}

		protected void stream(Stream.Builder<AbstractNode<NT, LT>> builder, boolean leafStream)
		{
			if(!leafStream)
			{
				builder.accept(this);
			}
			for(AbstractNode<NT, LT> child : getChildren())
			{
				child.stream(builder, leafStream);
			}
		}
	}

	public static class Leaf<NT extends Comparable<NT>, LT extends Comparable<LT>> extends AbstractNode<NT, LT>
	{
		LT data;

		Leaf(LT data, @Nullable Node<NT, LT> superNode, int weight)
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