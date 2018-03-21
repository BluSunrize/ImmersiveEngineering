package blusunrize.lib.manual;

import blusunrize.lib.manual.IManualPage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TextSplitter {
	private final Function<String, Integer> width;
	private final int lineWidth;
	private Map<Integer, Map<Integer, Page>> linesOnSpecialPages = new HashMap<>();
	private Map<Integer, Page> pageToSpecial = new HashMap<>();
	private List<List<String>> entry = new ArrayList<>();
	private Page defPage;
	private Function<String, String> tokenTransform;

	public TextSplitter(Function<String, Integer> w, int lW, Function<String, IManualPage> defaultPage,
						Function<String, String> tokenTransform) {
		width = w;
		lineWidth = lW;
		defPage = new Page(defaultPage);
		this.tokenTransform = tokenTransform;
	}

	public TextSplitter(ManualInstance m)
	{
		this(m.fontRenderer::getStringWidth, 120,
				(s) -> new ManualPages.Text(m, s), (s)->s);
	}

	public TextSplitter(ManualInstance m, Function<String, String> tokenTransform)
	{
		this(m.fontRenderer::getStringWidth, 120,
				(s) -> new ManualPages.Text(m, s), tokenTransform);
	}

	public void clearSpecial() {
		linesOnSpecialPages.clear();
	}

	public void addSpecialPage(int ref, int offset, Function<String, IManualPage> factory) {
		if (offset<0||(ref!=-1&&ref<0)) {
			throw new IllegalArgumentException();
		}
		if (!linesOnSpecialPages.containsKey(ref)) {
			linesOnSpecialPages.put(ref, new HashMap<>());
		}
		linesOnSpecialPages.get(ref).put(offset, new Page(factory));
	}

	// I added labels to all break statements to make it more readable
	@SuppressWarnings({"UnnecessaryLabelOnBreakStatement", "UnusedLabel"})
	public void split(String in) {
		clearSpecial();
		entry.clear();
		String[] wordsAndSpaces = splitWhitespace(in);
		int pos = 0;
		List<String> overflow = new ArrayList<>();
		updateSpecials(-1, 0);
		entry:while (pos<wordsAndSpaces.length) {
			List<String> page = new ArrayList<>();
			page.addAll(overflow);
			overflow.clear();
			page:while (page.size()<getLinesOnPage(entry.size())&&pos<wordsAndSpaces.length) {
				String line = "";
				int currWidth = 0;
				line:while (pos<wordsAndSpaces.length&&currWidth<lineWidth) {
					String text = tokenTransform.apply(wordsAndSpaces[pos]);
					if (pos<wordsAndSpaces.length) {
						int textWidth = getWidth(text);
						if (currWidth + textWidth < lineWidth||line.length()==0) {
							pos++;
							if (text.equals("<np>")) {
								page.add(line);
								break page;
							} else if (text.equals("\n")) {
								break line;
							} else if (text.startsWith("<&")&&text.endsWith(">")) {
								int id = Integer.parseInt(text.substring(2, text.length()-1));
								int pageForId = entry.size();
								Map<Integer, Page> specialForId = linesOnSpecialPages.get(id);
								if (specialForId!=null&&specialForId.containsKey(0)) {
									if (page.size()>getLinesOnPage(pageForId)) {
										pageForId++;
									}
								}
								if (updateSpecials(id, pageForId)) {
									page.add(line);
									break page;
								}
							} else if (!Character.isWhitespace(text.charAt(0))||line.length()!=0) {//Don't add whitespace at the start of a line
								line += text;
								currWidth += textWidth;
							}
						} else {
							break line;
						}
					}
				}
				page.add(line);
			}
			if (!page.stream().allMatch(String::isEmpty)) {
				int linesMax = getLinesOnPage(entry.size());
				if (page.size()>linesMax) {
					overflow.addAll(page.subList(linesMax, page.size()));
					page = page.subList(0, linesMax-1);
				}
				entry.add(page);
			}
		}
	}

	public IManualPage[] toManualEntry() {
		IManualPage[] ret = new IManualPage[entry.size()];
		for (int i = 0; i < entry.size(); i++) {
			String s = entry.get(i).stream().collect(Collectors.joining("\n"));
			ret[i] = pageToSpecial.getOrDefault(i, defPage).factory.apply(s);
		}
		return ret;
	}

	private int getWidth(String text) {
		switch (text) {
			case "<br>":
			case "<np>":
				return 0;
			default:
				if (text.startsWith("<link;")) {
					text = text.substring(text.indexOf(';') + 1);
					text = text.substring(text.indexOf(';') + 1, text.lastIndexOf(';'));
				}
				return width.apply(text);
		}
	}

	private int getLinesOnPage(int id) {
		if (pageToSpecial.containsKey(id)) {
			return pageToSpecial.get(id).getMaxLines();
		}
		return defPage.getMaxLines();
	}

	private boolean updateSpecials(int ref, int page) {
		if (linesOnSpecialPages.containsKey(ref)) {
			for (Map.Entry<Integer, Page> entry :linesOnSpecialPages.get(ref).entrySet()) {
				int specialPage = page+entry.getKey();
				if (pageToSpecial.containsKey(specialPage)) {
					return true;
				}
				pageToSpecial.put(specialPage, entry.getValue());
			}
		} else if (ref!=-1) {//Default reference for page 0
			System.out.println("WARNING: Reference "+ref+" was found, but no special pages were registered for it");
		}
		return false;
	}

	private String[] splitWhitespace(String in) {
		List<String> parts = new ArrayList<>();
		for (int i = 0;i<in.length();) {
			StringBuilder here = new StringBuilder();
			char first = in.charAt(i);
			here.append(first);
			i++;
			for (;i<in.length();) {
				char hereC = in.charAt(i);
				byte action = shouldSplit(first, hereC);
				if ((action&1)!=0) {
					here.append(in.charAt(i));
					i++;
				}
				if ((action&2)!=0||(action&1)==0) {
					break;
				}
			}
			parts.add(here.toString());
		}
		return parts.toArray(new String[parts.size()]);
	}

	/**
	 * @return
	 * &1: add
	 * &2: end here
	 */
	private byte shouldSplit(char start, char here) {
		byte ret = 0b01;
		if (Character.isWhitespace(start)^Character.isWhitespace(here)) {
			ret = 0b10;
		}
		if (here=='<') {
			ret = 0b10;
		}
		if (start=='<') {
			ret = 0b01;
			if (here=='>') {
				ret |= 0b10;
			}
		}
		return ret;
	}

	public List<List<String>> getEntry() {
		return entry;
	}

	private class Page {
		private final Function<String, IManualPage> factory;
		private IManualPage emptyInstance;
		public Page(Function<String, IManualPage> f) {
			factory = f;
			emptyInstance = f.apply("");
		}
		
		public int getMaxLines()
		{
			return emptyInstance.getMaxLines();
		}
	}
}