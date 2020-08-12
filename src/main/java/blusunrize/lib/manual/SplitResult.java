/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.links.Link;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SplitResult
{
	public final List<List<List<Token>>> entry;
	public final Object2IntMap<String> pageByAnchor;
	public final Int2ObjectMap<SpecialManualElement> specialByPage;

	SplitResult(List<List<List<Token>>> entry, Object2IntMap<String> pageByAnchor, Int2ObjectMap<SpecialManualElement> specialByPage)
	{
		this.entry = ImmutableList.copyOf(
				entry.stream()
						.map(ImmutableList::copyOf)
						.collect(Collectors.toList())
		);
		this.pageByAnchor = Object2IntMaps.unmodifiable(pageByAnchor);
		this.specialByPage = Int2ObjectMaps.unmodifiable(specialByPage);
	}

	public static class Token
	{
		private final Either<String, LinkPart> content;

		public Token(Either<String, LinkPart> content)
		{
			this.content = content;
		}

		public Token(String content)
		{
			this(Either.left(content));
		}

		public Either<String, LinkPart> getContent()
		{
			return content;
		}

		public String getText()
		{
			return content.map(Function.identity(), LinkPart::getText);
		}

		public Token replace(char oldChar, char newChar)
		{
			return new Token(
					content.mapBoth(
							s -> s.replace(oldChar, newChar),
							l -> new LinkPart(l.getParent(), l.getText().replace(oldChar, newChar))
					)
			);
		}

		public boolean isWhitespace()
		{
			return Character.isWhitespace(getText().charAt(0));
		}

		public Token copyWithText(String text)
		{
			return new Token(getContent().mapBoth(
					s -> text,
					l -> new LinkPart(l.getParent(), text)
			));
		}
	}


	public static class LinkPart
	{
		private final Link parent;
		private final String text;

		public LinkPart(Link parent, String text)
		{
			this.parent = parent;
			this.text = text;
		}

		public Link getParent()
		{
			return parent;
		}

		public String getText()
		{
			return text;
		}
	}
}
