package blusunrize.lib.manual;


import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public class TextSplitter
{
	public static final String START = "start";
	public static final Pattern LINEBREAK = Pattern.compile("[\\n\\r]+");

	private final Function<String, Integer> width;
	private final int lineWidth;
	private final IntSupplier pixelsPerLine;
	private final Map<String, Map<Integer, SpecialManualElement>> specialByAnchor = new HashMap<>();
	private final Int2ObjectMap<SpecialManualElement> specialByPage = new Int2ObjectOpenHashMap<>();
	private final List<List<String>> entry = new ArrayList<>();
	private final Function<String, String> tokenTransform;
	private final int pixelsPerPage;
	private final Object2IntMap<String> pageByAnchor = new Object2IntOpenHashMap<>();

	public TextSplitter(Function<String, Integer> w, int lineWidthPixel, int pageHeightPixel,
						IntSupplier pixelsPerLine, Function<String, String> tokenTransform)
	{
		width = w;
		this.lineWidth = lineWidthPixel;
		pixelsPerPage = pageHeightPixel;
		this.pixelsPerLine = pixelsPerLine;
		this.tokenTransform = tokenTransform;
	}

	public TextSplitter(ManualInstance m)
	{
		this(m, (s) -> s);
	}

	public TextSplitter(ManualInstance m, Function<String, String> tokenTransform)
	{
		this(s -> m.fontRenderer().getStringWidth(s), m.pageWidth, m.pageHeight, () -> m.fontRenderer().FONT_HEIGHT, tokenTransform);
	}

	public void clearSpecialByPage()
	{
		specialByPage.clear();
	}

	public void clearSpecialByAnchor()
	{
		specialByAnchor.clear();
	}

	public void addSpecialPage(String ref, int offset, SpecialManualElement element)
	{
		if(offset < 0||ref==null||ref.isEmpty())
			throw new IllegalArgumentException();
		if(!specialByAnchor.containsKey(ref))
			specialByAnchor.put(ref, new HashMap<>());
		specialByAnchor.get(ref).put(offset, element);
	}

	// I added labels to all break statements to make it more readable
	@SuppressWarnings({"UnnecessaryLabelOnBreakStatement", "UnusedLabel"})
	public void split(String in)
	{
		for(Map<Integer, SpecialManualElement> forAnchor : specialByAnchor.values())
			for(SpecialManualElement e : forAnchor.values())
				e.recalculateCraftingRecipes();
		clearSpecialByPage();
		entry.clear();
		String[] wordsAndSpaces = splitWhitespace(in);
		int pos = 0;
		List<String> overflow = new ArrayList<>();
		updateSpecials(START, 0);
		String formattingFromPreviousLine = "";
		entry:
		while(pos < wordsAndSpaces.length||!overflow.isEmpty())
		{
			List<String> page = new ArrayList<>(overflow);
			overflow.clear();
			boolean forceNewPage = getLinesOnPage(entry.size()) <= 0;
			page:
			while(page.size() < getLinesOnPage(entry.size())&&pos < wordsAndSpaces.length)
			{
				String line = formattingFromPreviousLine;
				formattingFromPreviousLine = "";
				int currWidth = 0;
				line:
				while(pos < wordsAndSpaces.length&&currWidth < lineWidth)
				{
					String token = tokenTransform.apply(wordsAndSpaces[pos]);
					int textWidth = getWidth(token);
					if(currWidth+textWidth <= lineWidth||line.length()==0)
					{
						pos++;
						if(token.equals("<np>"))
						{
							if(!page.isEmpty()||!line.isEmpty())
								page.add(line);
							break page;
						}
						else if(LINEBREAK.matcher(token).matches())
						{
							break line;
						}
						else if(token.startsWith("<&")&&token.endsWith(">"))
						{
							String id = token.substring(2, token.length()-1);
							int pageForId = entry.size();
							Map<Integer, SpecialManualElement> specialForId = specialByAnchor.get(id);
							boolean specialOnNextPage = false;
							boolean pageFull = false;
							if(specialForId!=null&&specialForId.containsKey(0))
							{
								SpecialManualElement specialHere = specialForId.get(0);
								//+1: Current line
								if(page.size()+1 > getLinesOnPage(specialHere)&&
										//Add long special elements on an empty page
										!(page.isEmpty()&&getLinesOnPage(specialHere) <= 0))
								{
									pageForId++;
									specialOnNextPage = true;
								}
								else
									pageFull = page.size()+1 > getLinesOnPage(specialHere);
							}
							//New page if there is already a special element on this page
							if(!specialOnNextPage&&updateSpecials(id, pageForId))
								specialOnNextPage = true;
							if(specialOnNextPage||pageFull)
							{
								if(!line.isEmpty())
									page.add(line);
								if(specialOnNextPage)
									pos--;
								forceNewPage = true;
								break page;
							}
						}
						else if(!Character.isWhitespace(token.charAt(0))||currWidth!=0)
						{//Don't add whitespace at the start of a line
							line += token;
							currWidth += textWidth;
						}
					}
					else
					{
						formattingFromPreviousLine = TextFormatting.getFormatString(line);
						break line;
					}
				}
				line = line.trim();
				if(!page.isEmpty()||!line.isEmpty())
					page.add(line);
			}
			int linesMax = getLinesOnPage(entry.size());
			forceNewPage |= linesMax <= 0;
			if(forceNewPage||!page.stream().allMatch(String::isEmpty))
			{
				if(page.size() > linesMax)
				{
					if(linesMax > 0)
					{
						overflow.addAll(page.subList(linesMax, page.size()));
						page = page.subList(0, linesMax-1);
					}
					else
						page = new ArrayList<>();
				}
				entry.add(page);
			}
		}
		for(List<String> page : entry)
			for(int i = 0; i < page.size(); ++i)
				//Replace nonbreaking space (used to enforce unusual formatting, like space at the start of a line)
				//by a normal space that can be properly rendered
				page.set(i, page.get(i).replace('\u00A0', ' '));
		specialByPage.keySet().stream().max(Comparator.naturalOrder()).ifPresent(maxPageWithSpecial -> {
			while(entry.size() <= maxPageWithSpecial)
				entry.add(new ArrayList<>());
		});
	}

	private int getWidth(String text)
	{
		if(LINEBREAK.matcher(text).matches())
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

	private int getLinesOnPage(int id)
	{
		return getLinesOnPage(specialByPage.get(id));
	}

	private int getLinesOnPage(@Nullable SpecialManualElement elementOnPage)
	{
		int pixels = pixelsPerPage;
		if(elementOnPage!=null)
			pixels = pixelsPerPage-elementOnPage.getPixelsTaken();
		return MathHelper.floor(pixels/(double)pixelsPerLine.getAsInt());
	}

	private boolean updateSpecials(String ref, int page)
	{
		if(specialByAnchor.containsKey(ref))
		{
			Int2ObjectMap<SpecialManualElement> specialByPageTmp = new Int2ObjectOpenHashMap<>();
			for(Map.Entry<Integer, SpecialManualElement> entry : specialByAnchor.get(ref).entrySet())
			{
				int specialPage = page+entry.getKey();
				if(specialByPage.containsKey(specialPage))
					return true;
				specialByPageTmp.put(specialPage, entry.getValue());
			}
			specialByPage.putAll(specialByPageTmp);
		}
		else if(!ref.equals(START)) //Default reference for page 0
			System.out.println("WARNING: Reference "+ref+" was found, but no special pages were registered for it");
		pageByAnchor.put(ref, page);
		return false;
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

	public List<List<String>> getEntryText()
	{
		return entry;
	}

	public Int2ObjectMap<SpecialManualElement> getSpecials()
	{
		return specialByPage;
	}

	public int getPageForAnchor(String anchor)
	{
		return pageByAnchor.getInt(anchor);
	}

	public boolean hasAnchor(String anchor)
	{
		return pageByAnchor.containsKey(anchor);
	}
}