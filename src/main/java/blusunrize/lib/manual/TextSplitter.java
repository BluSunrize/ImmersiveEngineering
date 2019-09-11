package blusunrize.lib.manual;


import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@SuppressWarnings("WeakerAccess")
public class TextSplitter
{
	public static final String START = "start";

	private final Function<String, Integer> width;
	private final int lineWidth;
	private final int pixelsPerLine;
	private final Map<String, Map<Integer, SpecialManualElement>> specialByAnchor = new HashMap<>();
	private final Int2ObjectMap<SpecialManualElement> specialByPage = new Int2ObjectOpenHashMap<>();
	private final List<List<String>> entry = new ArrayList<>();
	private final Function<String, String> tokenTransform;
	private final int pixelsPerPage;
	private Object2IntMap<String> pageByAnchor = new Object2IntOpenHashMap<>();

	public TextSplitter(Function<String, Integer> w, int lineWidthPixel, int pageHeightPixel,
						int pixelsPerLine, Function<String, String> tokenTransform)
	{
		width = w;
		this.lineWidth = lineWidthPixel;
		pixelsPerPage = pageHeightPixel;
		this.pixelsPerLine = pixelsPerLine;
		this.tokenTransform = tokenTransform;
	}

	public TextSplitter(ManualInstance m)
	{
		this(m.fontRenderer::getStringWidth, m.pageWidth, m.pageHeight, m.fontRenderer.FONT_HEIGHT, (s) -> s);
	}

	public TextSplitter(ManualInstance m, Function<String, String> tokenTransform)
	{
		this(m.fontRenderer::getStringWidth, m.pageWidth, m.pageHeight, m.fontRenderer.FONT_HEIGHT, tokenTransform);
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
		{
			throw new IllegalArgumentException();
		}
		if(!specialByAnchor.containsKey(ref))
		{
			specialByAnchor.put(ref, new HashMap<>());
		}
		specialByAnchor.get(ref).put(offset, element);
	}

	// I added labels to all break statements to make it more readable
	@SuppressWarnings({"UnnecessaryLabelOnBreakStatement", "UnusedLabel"})
	public void split(String in)
	{
		clearSpecialByPage();
		entry.clear();
		String[] wordsAndSpaces = splitWhitespace(in);
		int pos = 0;
		List<String> overflow = new ArrayList<>();
		updateSpecials(START, 0);
		entry:
		while(pos < wordsAndSpaces.length)
		{
			List<String> page = new ArrayList<>(overflow);
			overflow.clear();
			page:
			while(page.size() < getLinesOnPage(entry.size())&&pos < wordsAndSpaces.length)
			{
				String line = "";
				int currWidth = 0;
				line:
				while(pos < wordsAndSpaces.length&&currWidth < lineWidth)
				{
					String token = tokenTransform.apply(wordsAndSpaces[pos]);
					int textWidth = getWidth(token);
					if(currWidth+textWidth < lineWidth||line.length()==0)
					{
						pos++;
						if(token.equals("<np>"))
						{
							page.add(line);
							break page;
						}
						else if(token.equals("\n"))
						{
							break line;
						}
						else if(token.startsWith("<&")&&token.endsWith(">"))
						{
							String id = token.substring(2, token.length()-1);
							int pageForId = entry.size();
							Map<Integer, SpecialManualElement> specialForId = specialByAnchor.get(id);
							if(specialForId!=null&&specialForId.containsKey(0))
							{
								if(page.size() > getLinesOnPage(pageForId))
								{
									pageForId++;
								}
							}
							//New page if there is already a special element on this page
							if(updateSpecials(id, pageForId))
							{
								page.add(line);
								pos--;
								break page;
							}
						}
						else if(!Character.isWhitespace(token.charAt(0))||line.length()!=0)
						{//Don't add whitespace at the start of a line
							line += token;
							currWidth += textWidth;
						}
					}
					else
					{
						break line;
					}
				}
				line = line.trim();
				if(!line.isEmpty())
					page.add(line);
			}
			if(!page.stream().allMatch(String::isEmpty))
			{
				int linesMax = getLinesOnPage(entry.size());
				if(page.size() > linesMax)
				{
					overflow.addAll(page.subList(linesMax, page.size()));
					if(linesMax > 0)
						page = page.subList(0, linesMax-1);
					else
						page = new ArrayList<>();
				}
				entry.add(page);
			}
		}
	}

	private int getWidth(String text)
	{
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
		int pixels = pixelsPerPage;
		if(specialByPage.containsKey(id))
		{
			pixels = pixelsPerPage-specialByPage.get(id).getPixelsTaken();
		}
		return Math.max(0, MathHelper.floor(pixels/(double)pixelsPerLine));
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
				{
					return true;
				}
				specialByPageTmp.put(specialPage, entry.getValue());
			}
			specialByPage.putAll(specialByPageTmp);
		}
		else if(!ref.equals(START))
		{//Default reference for page 0
			System.out.println("WARNING: Reference "+ref+" was found, but no special pages were registered for it");
		}
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
			for(; i < in.length(); )
			{
				char hereC = in.charAt(i);
				byte action = shouldSplit(first, hereC);
				if((action&1)!=0)
				{
					here.append(in.charAt(i));
					i++;
				}
				if((action&2)!=0||(action&1)==0)
				{
					break;
				}
			}
			parts.add(here.toString());
		}
		return parts.toArray(new String[0]);
	}

	/**
	 * @return &1: add
	 * &2: end here
	 */
	private static byte shouldSplit(char start, char here)
	{
		byte ret = 0b01;
		if(Character.isWhitespace(start)^Character.isWhitespace(here))
		{
			ret = 0b10;
		}
		if(here=='<')
		{
			ret = 0b10;
		}
		if(start=='<')
		{
			ret = 0b01;
			if(here=='>')
			{
				ret |= 0b10;
			}
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