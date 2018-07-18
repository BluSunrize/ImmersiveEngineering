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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Tree<NT, LT>
{
	private Node<NT, LT> root;

	public Tree(NT root)
	{
		this.root = new Node<>(root, null);
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

	public static abstract class AbstractNode<NT, LT>
	{
		@Nullable
		private Node<NT, LT> superNode;

		AbstractNode(@Nullable Node<NT, LT> superNode)
		{
			this.superNode = superNode;
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
		public Node<NT, LT> getSuperNode()
		{
			return superNode;
		}

		protected abstract void stream(Stream.Builder<AbstractNode<NT, LT>> builder, boolean leafStream);
	}

	public static class Node<NT, LT> extends AbstractNode<NT, LT>
	{
		private final List<AbstractNode<NT, LT>> children = new ArrayList<>();
		private NT data;

		public Node(NT data, @Nullable Node<NT, LT> superNode)
		{
			super(superNode);
			this.data = data;
		}

		@Override
		public List<AbstractNode<NT, LT>> getChildren()
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

		public Node<NT, LT> addNewSubnode(NT data)
		{
			Node<NT, LT> newNode = new Node<>(data, this);
			children.add(newNode);
			return newNode;
		}

		public Node<NT, LT> getOrCreateSubnode(NT data)
		{
			for(AbstractNode<NT, LT> child : children)
			{
				if(!child.isLeaf()&&data.equals(child.getNodeData()))
				{
					return (Node<NT, LT>)child;
				}
			}
			return addNewSubnode(data);
		}

		public void addNewLeaf(LT data)
		{
			Leaf<NT, LT> newLeaf = new Leaf<>(data, this);
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

	public static class Leaf<NT, LT> extends AbstractNode<NT, LT>
	{
		LT data;

		Leaf(LT data, @Nullable Node<NT, LT> superNode)
		{
			super(superNode);
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