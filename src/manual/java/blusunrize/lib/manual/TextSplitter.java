/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.lib.manual;


import blusunrize.lib.manual.SplitResult.LinkPart;
import blusunrize.lib.manual.SplitResult.Token;
import blusunrize.lib.manual.links.EntryWithLinks;
import blusunrize.lib.manual.links.Link;
import blusunrize.lib.manual.utils.ManualLogger;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import java.util.*;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public class TextSplitter
{
	public static final String START = "start";

	private final Function<String, Integer> width;
	private final int lineWidth;
	private final IntSupplier pixelsPerLine;
	private final Map<String, Map<Integer, SpecialManualElement>> specialByAnchor = new HashMap<>();
	private final Function<String, String> tokenTransform;
	private final int pixelsPerPage;

	public TextSplitter(Function<String, Integer> w, int lineWidthPixel, int pageHeightPixel,
						IntSupplier pixelsPerLine, Function<String, String> tokenTransform)
	{
		this.width = w;
		this.lineWidth = lineWidthPixel;
		this.pixelsPerPage = pageHeightPixel;
		this.pixelsPerLine = pixelsPerLine;
		this.tokenTransform = tokenTransform;
		clearSpecialByAnchor();
	}

	public TextSplitter(ManualInstance m)
	{
		this(m, Function.identity());
	}

	public TextSplitter(ManualInstance m, Function<String, String> tokenTransform)
	{
		this(m.fontRenderer(), m.pageWidth, m.pageHeight, tokenTransform.andThen(s -> {
			final String extraFormat = TextFormatting.BOLD.toString();
			if(m.improveReadability()&&s.charAt(0)!='<'&&!s.trim().isEmpty())
			{
				for(TextFormatting f : TextFormatting.values())
					if(!f.isFancyStyling())
						s = s.replace(f.toString(), f+extraFormat);
				s = extraFormat+s;
			}
			return s;
		}));
	}

	public TextSplitter(FontRenderer fontRenderer, int width, int height, Function<String, String> tokenTransform)
	{
		this(fontRenderer::getStringWidth, width, height, () -> fontRenderer.FONT_HEIGHT, tokenTransform);
	}

	public void clearSpecialByAnchor()
	{
		specialByAnchor.clear();
		specialByAnchor.put(START, new HashMap<>());
	}

	public void addSpecialPage(String ref, int offset, SpecialManualElement element)
	{
		if(offset < 0||ref==null||ref.isEmpty())
			throw new IllegalArgumentException();
		if(!specialByAnchor.containsKey(ref))
			specialByAnchor.put(ref, new HashMap<>());
		specialByAnchor.get(ref).put(offset, element);
	}

	public SplitResult split(String in)
	{
		return split(
				EntryWithLinks.splitWhitespace(in).stream()
						.<Either<String, Link>>map(Either::left)
						.collect(Collectors.toList())
		);
	}

	public SplitResult split(List<Either<String, Link>> unsizedTokens)
	{
		for(Map<Integer, SpecialManualElement> forAnchor : specialByAnchor.values())
			for(SpecialManualElement e : forAnchor.values())
				e.recalculateCraftingRecipes();
		List<List<List<Token>>> entry = new ArrayList<>();
		Object2IntMap<String> pageByAnchor = new Object2IntOpenHashMap<>();
		pageByAnchor.put(START, 0);
		List<TokenWithWidth> wordsAndSpaces = convertToSplitterTokens(unsizedTokens);
		NextPageData pageOverflow = new NextPageData();
		while(pageOverflow!=null&&pageOverflow.topLine!=null)
		{
			Page nextPage = parsePage(
					pageOverflow,
					wordsAndSpaces,
					anchors -> noCollidingElements(anchors, entry.size(), pageByAnchor),
					str -> {
						Optional<SpecialManualElement> element = findElement(pageByAnchor, entry.size(), str);
						return getLinesOnPage(element);
					}
			);
			nextPage.anchorsOnPage.forEach(anchor -> pageByAnchor.put(anchor, entry.size()));
			entry.add(nextPage.lines);
			pageOverflow = nextPage.nextPage;
		}
		//Replace nonbreaking space (used to enforce unusual formatting, like space at the start of a line)
		//by a normal space that can be properly rendered
		for(List<List<Token>> page : entry)
			for(List<Token> line : page)
				for(int i = 0; i < line.size(); i++)
				{
					Token t = line.get(i);
					line.set(i, line.get(i).replace('\u00A0', ' '));
				}

		// Create page to special element map
		Int2ObjectMap<SpecialManualElement> specialByPage = new Int2ObjectOpenHashMap<>();
		int maxPageWithSpecial = 0;
		for(Entry<String> forAnchor : pageByAnchor.object2IntEntrySet())
			for(Map.Entry<Integer, SpecialManualElement> element : getElements(forAnchor.getKey()).entrySet())
			{
				int page = forAnchor.getIntValue()+element.getKey();
				Preconditions.checkState(!specialByPage.containsKey(page));
				specialByPage.put(page, element.getValue());
				if(page > maxPageWithSpecial)
					maxPageWithSpecial = page;
			}
		// Add pages without text if there are special elements after the last text page
		while(entry.size() <= maxPageWithSpecial)
			entry.add(new ArrayList<>());
		return new SplitResult(entry, pageByAnchor, specialByPage);
	}

	private boolean noCollidingElements(List<String> newAnchors, int anchorPage, Object2IntMap<String> pageByAnchor)
	{
		IntSet pagesWithSpecials = new IntArraySet();
		for(String newAnchor : newAnchors)
			for(int offset : getElements(newAnchor).keySet())
				if(!pagesWithSpecials.add(offset+anchorPage))
					return false;
		for(Entry<String> e : pageByAnchor.object2IntEntrySet())
			for(int offset : getElements(e.getKey()).keySet())
				if(!pagesWithSpecials.add(offset+e.getIntValue()))
					return false;
		return true;
	}

	private Optional<SpecialManualElement> findElement(
			Object2IntMap<String> pageByAnchor,
			int newAnchorPage,
			List<String> newAnchors
	)
	{
		for(String newAnchor : newAnchors)
		{
			Map<Integer, SpecialManualElement> forNewAnchor = getElements(newAnchor);
			if(forNewAnchor.containsKey(0))
				return Optional.of(forNewAnchor.get(0));
		}
		for(Entry<String> e : pageByAnchor.object2IntEntrySet())
		{
			Map<Integer, SpecialManualElement> forAnchor = getElements(e.getKey());
			int offset = newAnchorPage-e.getIntValue();
			if(forAnchor.containsKey(offset))
				return Optional.of(forAnchor.get(offset));
		}
		return Optional.empty();
	}

	private Map<Integer, SpecialManualElement> getElements(String anchor)
	{
		if(!specialByAnchor.containsKey(anchor))
		{
			ManualLogger.LOGGER.warn("Tried to access invalid anchor \"{}\"", anchor);
			return ImmutableMap.of();
		}
		else
			return specialByAnchor.get(anchor);
	}

	private Page parsePage(
			NextPageData overflow,
			List<TokenWithWidth> wordsAndSpaces,
			Predicate<List<String>> canPlaceAnchors,
			Function<List<String>, Integer> getLines
	)
	{
		List<List<Token>> page = new ArrayList<>();
		NextLineData lineOverflow = overflow.topLine;
		List<String> anchorsOnPage = new ArrayList<>();
		while(page.size() < getLines.apply(anchorsOnPage)&&lineOverflow!=null)
		{
			Function<List<String>, AnchorViability> getAnchorViability = anchors -> {
				List<String> withNewAnchor = new ArrayList<>(anchorsOnPage);
				withNewAnchor.addAll(anchors);
				if(!canPlaceAnchors.test(withNewAnchor))
					return AnchorViability.NOT_VALID;
				else if(page.size()+1 > getLines.apply(withNewAnchor))
					// Allow specials larger than the page only if nothing else is placed on that page
					return AnchorViability.VALID_IF_ALONE;
				else
					return AnchorViability.VALID;
			};
			Line next = parseLine(lineOverflow, getAnchorViability, wordsAndSpaces);
			AnchorViability viability = getAnchorViability.apply(next.anchorsBeforeLine);
			Preconditions.checkState(viability!=AnchorViability.NOT_VALID);
			if(viability==AnchorViability.VALID_IF_ALONE&&!page.isEmpty())
				break;
			else
				anchorsOnPage.addAll(next.anchorsBeforeLine);
			if(!page.isEmpty()||!next.line.isEmpty())
				page.add(next.line);
			lineOverflow = next.overflow;
			if(lineOverflow!=null&&lineOverflow.putOnNewPage)
				break;
		}
		// Remove empty lines at the end of the page
		while(!page.isEmpty()&&page.get(page.size()-1).stream().allMatch(t -> t.getText().trim().isEmpty()))
			page.remove(page.size()-1);
		return new Page(page, anchorsOnPage, lineOverflow);
	}

	private Line parseLine(
			NextLineData lastOverflow,
			Function<List<String>, AnchorViability> canPlaceAnchors,
			List<TokenWithWidth> wordsAndSpaces
	)
	{
		int pos = lastOverflow.firstToken;
		List<String> anchorsBeforeLine = new ArrayList<>();
		List<TokenWithWidth> lineTokens = new ArrayList<>();
		int currentWidth;
		if(!lastOverflow.overflow.getText().isEmpty())
		{
			int overflowLength = getWidth(lastOverflow.overflow.getText());
			lineTokens.add(new TokenWithWidth(lastOverflow.overflow, overflowLength));
			currentWidth = overflowLength;
		}
		else
			currentWidth = 0;
		while(pos < wordsAndSpaces.size()&&currentWidth < lineWidth)
		{
			final TokenWithWidth token = wordsAndSpaces.get(pos);
			if(currentWidth+token.width <= lineWidth||currentWidth==0)
			{
				if(token.getText().equals("<np>"))
					return new Line(lineTokens, pos+1, true, anchorsBeforeLine);
				else if(isLinebreak(token.getText()))
					return new Line(lineTokens, pos+1, false, anchorsBeforeLine);
				else if(token.getText().startsWith("<&")&&token.getText().endsWith(">"))
				{
					String anchor = toAnchor(token.getText());
					List<String> withNewAdded = new ArrayList<>(anchorsBeforeLine);
					withNewAdded.add(anchor);
					AnchorViability allowed = canPlaceAnchors.apply(withNewAdded);

					if(allowed==AnchorViability.VALID_IF_ALONE&&currentWidth==0&&anchorsBeforeLine.isEmpty())
						return new Line(ImmutableList.of(), pos+1, true, ImmutableList.of(anchor));
					else if(allowed!=AnchorViability.VALID)
						return new Line(lineTokens, pos, true, anchorsBeforeLine);
					else
						anchorsBeforeLine.add(anchor);
				}
				else if(!token.baseToken.isWhitespace()||currentWidth!=0)
				{
					//Don't add whitespace at the start of a line
					lineTokens.add(token);
					currentWidth += token.width;
				}
				pos++;
			}
			else
				break;
		}
		currentWidth = removeEndWhitespace(lineTokens, currentWidth);
		if(currentWidth > lineWidth)
		{
			// Split off surplus characters, this should only happen with words longer than one line
			TokenWithWidth lastToken = lineTokens.get(lineTokens.size()-1);
			String lastTokenText = lastToken.getText();
			final int trimTo = lineWidth-(currentWidth-lastToken.width);
			String upToWidth = "";
			for(int i = 0; i < lastTokenText.length()&&getWidth(upToWidth) < trimTo; ++i)
				upToWidth += lastTokenText.charAt(i);
			String overflowText = lastTokenText.substring(upToWidth.length());
			Token overflow = lastToken.baseToken.copyWithText(overflowText);
			TokenWithWidth trimmedLastToken = lastToken.copyWithText(upToWidth, trimTo);
			lineTokens.set(lineTokens.size()-1, trimmedLastToken);
			return new Line(lineTokens, pos, false, anchorsBeforeLine, overflow);
		}
		else if(pos < wordsAndSpaces.size())
			return new Line(lineTokens, pos, false, anchorsBeforeLine);
		else
			return new Line(lineTokens, null, anchorsBeforeLine);
	}

	private int removeEndWhitespace(List<TokenWithWidth> tokens, int totalLength)
	{
		int lastIndex = tokens.size()-1;
		while(!tokens.isEmpty()&&tokens.get(lastIndex).baseToken.isWhitespace())
			totalLength -= tokens.remove(lastIndex--).width;
		if(lastIndex >= 0)
		{
			TokenWithWidth lastToken = tokens.get(lastIndex);
			String newText = lastToken.getText().trim();
			StringBuilder postFormat = new StringBuilder();
			while(newText.length() >= 2&&newText.charAt(newText.length()-2)=='\u00a7')
			{
				postFormat.insert(0, newText.substring(newText.length()-2));
				newText = newText.substring(0, newText.length()-2).trim();
			}
			newText += postFormat;
			if(!newText.equals(lastToken.getText()))
				tokens.set(lastIndex, lastToken.copyWithText(newText, getWidth(newText)));
		}
		return totalLength;
	}

	private String toAnchor(String token)
	{
		return token.substring(2, token.length()-1);
	}

	private int getWidth(String text)
	{
		if(isLinebreak(text))
			return 0;
		switch(text)
		{
			case "<br>":
			case "<np>":
				return 0;
			default:
				return width.apply(text);
		}
	}

	private int getLinesOnPage(Optional<SpecialManualElement> elementOnPage)
	{
		int pixels = pixelsPerPage;
		if(elementOnPage.isPresent())
			pixels = pixelsPerPage-elementOnPage.get().getPixelsTaken();
		return MathHelper.floor(pixels/(double)pixelsPerLine.getAsInt());
	}

	private List<TokenWithWidth> convertToSplitterTokens(List<Either<String, Link>> rawTokens)
	{
		List<TokenWithWidth> ret = new ArrayList<>(rawTokens.size());
		for(Either<String, Link> e : rawTokens)
		{
			e.ifLeft(s -> {
				String transformed = tokenTransform.apply(s);
				ret.add(new TokenWithWidth(Either.left(transformed), getWidth(transformed)));
			});
			e.ifRight(
					l -> l.getParts().stream()
							.map(tokenTransform)
							.map(s -> new TokenWithWidth(Either.right(new LinkPart(l, s)), getWidth(s)))
							.forEach(ret::add)
			);
		}
		return ret;
	}

	private boolean isLinebreak(String s)
	{
		if(s.isEmpty())
			return false;
		for(int i = 0; i < s.length(); ++i)
		{
			char c = s.charAt(i);
			if(c!='\n'&&c!='\r')
				return false;
		}
		return true;
	}

	private static class Line
	{
		private final List<Token> line;
		private final NextLineData overflow;
		private final List<String> anchorsBeforeLine;

		private Line(List<TokenWithWidth> line, NextLineData overflow, List<String> anchorsBeforeLine)
		{
			this.line = line.stream()
					.map(t -> t.baseToken)
					.collect(Collectors.toList());
			this.overflow = overflow;
			this.anchorsBeforeLine = anchorsBeforeLine;
		}

		public Line(List<TokenWithWidth> line, int firstToken, boolean endPageAfterLine, List<String> anchorBeforeLine, Token textOverflow)
		{
			this(line, new NextLineData(
					firstToken,
					endPageAfterLine,
					textOverflow.copyWithText(getFormattingAtEnd(line)+textOverflow.getText())
			), anchorBeforeLine);
		}

		public Line(List<TokenWithWidth> line, int firstToken, boolean endPageAfterLine, List<String> anchorBeforeLine)
		{
			this(line, firstToken, endPageAfterLine, anchorBeforeLine, new Token(""));
		}

		private static String getFormattingAtEnd(List<TokenWithWidth> tokens)
		{
			List<TextFormatting> ret = new ArrayList<>();
			for(TokenWithWidth token : tokens)
			{
				String tokenText = token.getText();
				int start = -1;

				while((start = tokenText.indexOf('\u00a7', start+1))!=-1)
					if(start < tokenText.length()-1)
					{
						TextFormatting textformatting = TextFormatting.fromFormattingCode(tokenText.charAt(start+1));
						if(textformatting!=null)
						{
							if(!textformatting.isFancyStyling())
								ret.clear();
							if(textformatting!=TextFormatting.RESET)
							{
								ret.remove(textformatting);
								ret.add(textformatting);
							}
						}
					}
			}

			return ret.stream()
					.map(TextFormatting::toString)
					.collect(Collectors.joining());
		}
	}

	private static class NextLineData
	{
		private final int firstToken;
		private final boolean putOnNewPage;
		private final Token overflow;

		private NextLineData(int firstToken, boolean putOnNewPage, Token overflow)
		{
			this.firstToken = firstToken;
			this.putOnNewPage = putOnNewPage;
			this.overflow = overflow;
		}
	}

	private static class Page
	{
		private final List<List<Token>> lines;
		private final List<String> anchorsOnPage;
		private final NextPageData nextPage;

		private Page(List<List<Token>> lines, List<String> anchorsOnPage, NextPageData nextPage)
		{
			this.lines = lines;
			this.anchorsOnPage = anchorsOnPage;
			this.nextPage = nextPage;
		}

		public Page(List<List<Token>> page, List<String> anchorsOnPage, NextLineData overflow)
		{
			this(page, anchorsOnPage, overflow==null?null: new NextPageData(overflow));
		}
	}

	private static class NextPageData
	{
		private final NextLineData topLine;

		private NextPageData(NextLineData topLine)
		{
			this.topLine = topLine;
		}

		public NextPageData()
		{
			this(new NextLineData(0, false, new Token(Either.left(""))));
		}
	}

	private enum AnchorViability
	{
		NOT_VALID,
		VALID,
		VALID_IF_ALONE
	}

	private static class TokenWithWidth
	{
		private final Token baseToken;
		private final int width;

		private TokenWithWidth(Either<String, LinkPart> text, int width)
		{
			this(new Token(text), width);
		}

		private TokenWithWidth(Token text, int width)
		{
			this.baseToken = text;
			this.width = width;
		}

		public String getText()
		{
			return baseToken.getText();
		}

		public TokenWithWidth copyWithText(String text, int newWidth)
		{
			return new TokenWithWidth(
					baseToken.copyWithText(text),
					newWidth
			);
		}
	}
}
