package blusunrize.lib.manual;


import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

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
		this(m, (s) -> s);
	}

	public TextSplitter(ManualInstance m, Function<String, String> tokenTransform)
	{
		this(s -> m.fontRenderer().getStringWidth(s), m.pageWidth, m.pageHeight, () -> m.fontRenderer().FONT_HEIGHT, tokenTransform);
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
		for(Map<Integer, SpecialManualElement> forAnchor : specialByAnchor.values())
			for(SpecialManualElement e : forAnchor.values())
				e.recalculateCraftingRecipes();
		List<List<String>> entry = new ArrayList<>();
		Object2IntMap<String> pageByAnchor = new Object2IntOpenHashMap<>();
		pageByAnchor.put(START, 0);
		String[] wordsAndSpaces = splitWhitespace(in);
		NextPageData pageOverflow = new NextPageData();
		while(pageOverflow!=null&&pageOverflow.topLine!=null)
		{
			Page nextPage = parsePage(
					pageOverflow,
					wordsAndSpaces,
					anchor -> noCollidingElements(anchor, entry.size(), pageByAnchor),
					str -> {
						Optional<SpecialManualElement> element = findElement(pageByAnchor, entry.size(), str);
						return getLinesOnPage(element);
					}
			);
			nextPage.anchor.ifPresent(anchor -> pageByAnchor.put(anchor, entry.size()));
			entry.add(nextPage.lines);
			pageOverflow = nextPage.nextPage;
		}
		//Replace nonbreaking space (used to enforce unusual formatting, like space at the start of a line)
		//by a normal space that can be properly rendered
		for(List<String> page : entry)
			for(int i = 0; i < page.size(); ++i)
				page.set(i, page.get(i).replace('\u00A0', ' '));

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

	private boolean noCollidingElements(String newAnchor, int anchorPage, Object2IntMap<String> pageByAnchor)
	{
		IntSet newSpecials = new IntArraySet();
		for(int offset : getElements(newAnchor).keySet())
			newSpecials.add(offset+anchorPage);
		for(Entry<String> e : pageByAnchor.object2IntEntrySet())
			for(int offset : getElements(e.getKey()).keySet())
				if(newSpecials.contains(e.getIntValue()+offset))
					return false;
		return true;
	}

	private Optional<SpecialManualElement> findElement(
			Object2IntMap<String> pageByAnchor,
			int newAnchorPage,
			Optional<String> newAnchor
	)
	{
		if(newAnchor.isPresent())
		{
			Map<Integer, SpecialManualElement> forNewAnchor = getElements(newAnchor.get());
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
			IELogger.warn("Tried to access invalid key \""+anchor+"\"");
			return ImmutableMap.of();
		}
		else
			return specialByAnchor.get(anchor);
	}

	private Page parsePage(
			NextPageData overflow,
			String[] wordsAndSpaces,
			Predicate<String> canPlaceAnchor,
			Function<Optional<String>, Integer> getLines
	)
	{
		List<String> page = new ArrayList<>();
		NextLineData lineOverflow = overflow.topLine;
		Optional<String> anchorOnPage = Optional.empty();
		while(page.size() < getLines.apply(anchorOnPage)&&lineOverflow!=null)
		{
			Optional<String> finalAnchorOnPage = anchorOnPage;
			Function<String, AnchorViability> getAnchorViability = anchor -> {
				if(finalAnchorOnPage.isPresent())
					return AnchorViability.NOT_VALID;
				else if(!canPlaceAnchor.test(anchor))
					return AnchorViability.NOT_VALID;
				else if(page.size()+1 > getLines.apply(Optional.of(anchor)))
					// Allow specials larger than the page only if nothing else is placed on that page
					return AnchorViability.VALID_IF_ALONE;
				else
					return AnchorViability.VALID;
			};
			Line next = parseLine(lineOverflow, getAnchorViability, wordsAndSpaces);
			if(next.anchorBeforeLine.isPresent())
			{
				String newAnchor = next.anchorBeforeLine.get();
				AnchorViability viability = getAnchorViability.apply(newAnchor);
				Preconditions.checkState(viability!=AnchorViability.NOT_VALID);
				if(viability==AnchorViability.VALID_IF_ALONE&&!page.isEmpty())
					break;
				else
					anchorOnPage = next.anchorBeforeLine;
			}
			if(!page.isEmpty()||!next.line.isEmpty())
				page.add(next.line);
			lineOverflow = next.overflow;
			if(lineOverflow!=null&&lineOverflow.putOnNewPage)
				break;
		}
		// Remove empty lines at the end of the page
		while(!page.isEmpty()&&page.get(page.size()-1).trim().isEmpty())
			page.remove(page.size()-1);
		return new Page(page, anchorOnPage, lineOverflow);
	}

	private Line parseLine(NextLineData lastOverflow, Function<String, AnchorViability> canPlaceAnchor, String[] wordsAndSpaces)
	{
		StringBuilder lineBuilder = new StringBuilder(lastOverflow.formattingOverflow+lastOverflow.textOverflow);
		int pos = lastOverflow.firstToken;
		Optional<String> anchorBeforeLine = Optional.empty();
		while(pos < wordsAndSpaces.length&&getWidth(lineBuilder.toString()) < lineWidth)
		{
			int currWidth = getWidth(lineBuilder.toString());
			String token = tokenTransform.apply(wordsAndSpaces[pos]);
			int textWidth = getWidth(token);
			if(currWidth+textWidth <= lineWidth||currWidth==0)
			{
				if(token.equals("<np>"))
					return new Line(lineBuilder.toString(), pos+1, true, anchorBeforeLine, "");
				else if(isLinebreak(token))
					return new Line(lineBuilder.toString(), pos+1, false, anchorBeforeLine, "");
				else if(token.startsWith("<&")&&token.endsWith(">"))
				{
					String anchor = toAnchor(token);
					AnchorViability allowed = canPlaceAnchor.apply(anchor);
					if(allowed==AnchorViability.VALID_IF_ALONE&&lineBuilder.toString().isEmpty())
						return new Line("", pos+1, true, Optional.of(anchor), "");
					else if(allowed!=AnchorViability.VALID)
						return new Line(lineBuilder.toString(), pos, true, Optional.empty(), "");
					else
						anchorBeforeLine = Optional.of(anchor);
				}
				else if(!Character.isWhitespace(token.charAt(0))||currWidth!=0)
					//Don't add whitespace at the start of a line
					lineBuilder.append(token);
				pos++;
			}
			else
				break;
		}
		String line = lineBuilder.toString().trim();
		if(getWidth(line) > lineWidth)
		{
			// Split off surplus characters, this should only happen with words longer than one line
			String upToWidth = "";
			for(int i = 0; i < line.length()&&getWidth(upToWidth) < lineWidth; ++i)
				upToWidth += line.charAt(i);
			String overflow = line.substring(upToWidth.length());
			return new Line(upToWidth, pos, false, anchorBeforeLine, overflow);
		}
		else if(pos < wordsAndSpaces.length)
			return new Line(line, pos, false, anchorBeforeLine, "");
		else
			return new Line(line, null, anchorBeforeLine);
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
				if(text.startsWith("<link;"))
				{
					text = text.substring(text.indexOf(';')+1);
					text = text.substring(text.indexOf(';')+1, text.lastIndexOf(';'));
				}
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

	public static String[] splitWhitespace(String in)
	{
		List<String> parts = new ArrayList<>();
		for(int i = 0; i < in.length(); )
		{
			StringBuilder here = new StringBuilder();
			char first = in.charAt(i);
			here.append(first);
			i++;
			while(i < in.length())
			{
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
		return parts.toArray(new String[0]);
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
		private final String line;
		private final NextLineData overflow;
		private final Optional<String> anchorBeforeLine;

		private Line(String line, NextLineData overflow, @Nullable Optional<String> anchorBeforeLine)
		{
			this.line = line;
			this.overflow = overflow;
			this.anchorBeforeLine = anchorBeforeLine;
		}

		public Line(String line, int firstToken, boolean endPageAfterLine, Optional<String> anchorBeforeLine, String textOverflow)
		{
			this(line, new NextLineData(firstToken, endPageAfterLine, TextFormatting.getFormatString(line), textOverflow), anchorBeforeLine);
		}
	}

	private static class NextLineData
	{
		private final int firstToken;
		private final boolean putOnNewPage;
		private final String formattingOverflow;
		private final String textOverflow;

		private NextLineData(int firstToken, boolean putOnNewPage, String formattingOverflow, String textOverflow)
		{
			this.firstToken = firstToken;
			this.putOnNewPage = putOnNewPage;
			this.formattingOverflow = formattingOverflow;
			this.textOverflow = textOverflow;
		}
	}

	private static class Page
	{
		private final List<String> lines;
		private final Optional<String> anchor;
		private final NextPageData nextPage;

		private Page(List<String> lines, Optional<String> anchor, NextPageData nextPage)
		{
			this.lines = lines;
			this.anchor = anchor;
			this.nextPage = nextPage;
		}

		public Page(List<String> page, Optional<String> anchor, NextLineData overflow)
		{
			this(page, anchor, overflow==null?null: new NextPageData(overflow));
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
			this(new NextLineData(0, false, "", ""));
		}
	}

	private enum AnchorViability
	{
		NOT_VALID,
		VALID,
		VALID_IF_ALONE
	}
}
