package blusunrize.lib.manual;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TextSplitter {
	private final Function<String, Integer> width;
	private final int lineWidth;
	private final Map<Integer, Map<Integer, SpecialManualElement>> specialByAnchor = new HashMap<>();
	private final Map<Integer, SpecialManualElement> specialByPage = new HashMap<>();
	private final List<List<String>> entry = new ArrayList<>();
	private final Function<String, String> tokenTransform;
	private final int linesPerPage;

	public TextSplitter(Function<String, Integer> w, int lW, int lP,
						Function<String, String> tokenTransform) {
		width = w;
		lineWidth = lW;
		linesPerPage = lP;
		this.tokenTransform = tokenTransform;
	}

	public TextSplitter(ManualInstance m)
	{
		this(m.fontRenderer::getStringWidth, 120, 16, (s)->s);
	}

	public TextSplitter(ManualInstance m, Function<String, String> tokenTransform)
	{
		this(m.fontRenderer::getStringWidth, 120, 16, tokenTransform);
	}

	public void clearSpecial() {
		specialByPage.clear();
	}

	public void addSpecialPage(int ref, int offset, SpecialManualElement element) {
		if (offset<0||(ref!=-1&&ref<0)) {
			throw new IllegalArgumentException();
		}
		if (!specialByAnchor.containsKey(ref)) {
			specialByAnchor.put(ref, new HashMap<>());
		}
		specialByAnchor.get(ref).put(offset, element);
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
								Map<Integer, SpecialManualElement> specialForId = specialByAnchor.get(id);
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
				if (!line.isEmpty())
					page.add(line);//TODO
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
		if (specialByPage.containsKey(id)) {
			return linesPerPage-specialByPage.get(id).getLinesTaken();
		}
		return linesPerPage;
	}

	private boolean updateSpecials(int ref, int page) {
		if (specialByAnchor.containsKey(ref)) {
			for (Map.Entry<Integer, SpecialManualElement> entry : specialByAnchor.get(ref).entrySet()) {
				int specialPage = page+entry.getKey();
				if (specialByPage.containsKey(specialPage)) {
					return true;
				}
				specialByPage.put(specialPage, entry.getValue());
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

	public List<List<String>> getEntryText() {
		return entry;
	}

	public Map<Integer, SpecialManualElement> getSpecials() {
		return specialByPage;
	}
}