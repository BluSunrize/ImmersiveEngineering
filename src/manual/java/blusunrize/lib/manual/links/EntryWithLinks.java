/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.links;

import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.TextSplitter;
import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EntryWithLinks
{
	private final List<Either<String, Link>> tokens;

	public EntryWithLinks(final String entryText, ManualInstance manual)
	{
		List<String> rawTokens = splitWhitespace(entryText);
		List<Either<String, Link>> tokens = new ArrayList<>(rawTokens.size());
		for(String token : rawTokens)
			if(token.startsWith("<link")&&token.charAt(token.length()-1)=='>')
			{
				String[] segment = token.substring(0, token.length()-1).split(";");
				if(segment.length < 3)
					break;
				String target = segment[1];
				String anchor = segment.length > 3?segment[3]: TextSplitter.START;
				String text = segment[2];
				Link link = new Link(text, target, anchor, manual);
				tokens.add(Either.right(link));
			}
			else
				tokens.add(Either.left(token));
		this.tokens = ImmutableList.copyOf(tokens);
	}

	@Deprecated
	public List<Link> getLinks()
	{
		return tokens.stream()
				.map(e -> e.right())
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(Collectors.toList());
	}

	@Deprecated
	public String getSanitizedText()
	{
		return tokens.stream()
				.map(e -> e.map(s -> s, l -> String.join("", l.getParts())))
				.collect(Collectors.joining());
	}

	public List<Either<String, Link>> getUnsplitTokens()
	{
		return tokens;
	}

	public static List<String> splitWhitespace(String in)
	{
		List<String> parts = new ArrayList<>();
		for(int i = 0; i < in.length(); )
		{
			// CJK codepoints are always tokenized individually
			int cp = in.codePointAt(i);
			int cpLen = Character.charCount(cp);
			if(isCJK(cp))
			{
				parts.add(in.substring(i, i+cpLen));
				i += cpLen;
				continue;
			}

			StringBuilder here = new StringBuilder();
			char first = in.charAt(i);
			here.append(first);
			i++;
			while(i < in.length())
			{
				if(!(first== '<')&&isCJK(in.codePointAt(i)))
					break;
				char hereC = in.charAt(i);
				byte action = shouldSplit(first, hereC);
				if((action&1)!=0)
				{
					here.append(in.charAt(i));
					i++;
				}
				if((action&2)!=0||(action&1)==0)
					break;
			}
			parts.add(here.toString());
		}
		return parts;
	}

	/**
	 * @return &1: add character to token
	 * &2: end here
	 */
	private static byte shouldSplit(char start, char here)
	{
		byte ret = 0b01;
		if(Character.isWhitespace(start)^Character.isWhitespace(here))
			ret = 0b10;
		else if(Character.isWhitespace(here))
		{
			if((start=='\n'&&here=='\r')||(start=='\r'&&here=='\n'))
				ret = 0b11;
			else if((here=='\r'||here=='\n')||(start=='\r'||start=='\n'))
				ret = 0b10;
		}

		if(here=='<')
			ret = 0b10;
		if(start=='<')
		{
			ret = 0b01;
			if(here=='>')
				ret |= 0b10;
		}
		return ret;
	}

	private static boolean isCJK(int codePoint)
	{
		return (codePoint >= 0x2E80 && codePoint <= 0xA4FF)
				|| (codePoint >= 0xAC00 && codePoint <= 0xD7FF)
				|| (codePoint >= 0xF900 && codePoint <= 0xFAFF)
				|| (codePoint >= 0xFF00 && codePoint <= 0xFFEF)
				|| (codePoint >= 0x20000 && codePoint <= 0x2A6DF)
				|| (codePoint >= 0x2A700 && codePoint <= 0x2B81F)
				|| (codePoint >= 0x2B820 && codePoint <= 0x2EBEF)
				|| (codePoint >= 0x2EBF0 && codePoint <= 0x2EE5F)
				|| (codePoint >= 0x2F800 && codePoint <= 0x2FA1F)
				|| (codePoint >= 0x30000 && codePoint <= 0x323AF);
	}
}
