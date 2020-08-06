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
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class EntryWithLinks
{
	private final List<Link> links;
	private final String sanitizedText;

	public EntryWithLinks(String entryText, ManualInstance manual)
	{
		List<Link> repList = new ArrayList<>();
		int linksAdded = 0;
		int start;
		while((start = entryText.indexOf("<link")) >= 0&&linksAdded < 50)
		{
			linksAdded++;
			int end = entryText.indexOf(">", start);
			String rep = entryText.substring(start, end+1);
			String[] segment = rep.substring(0, rep.length()-1).split(";");
			if(segment.length < 3)
				break;
			String target = segment[1];
			String anchor = segment.length > 3?segment[3]: TextSplitter.START;
			String text = segment[2];
			Link link = new Link(text, target, anchor, manual);
			repList.add(link);
			String fullLinkText = String.join("", link.getParts());
			entryText = entryText.substring(0, start)+fullLinkText+TextFormatting.RESET.toString()+entryText.substring(end+1);
		}
		this.links = ImmutableList.copyOf(repList);
		this.sanitizedText = entryText;
	}

	public List<Link> getLinks()
	{
		return links;
	}

	public String getSanitizedText()
	{
		return sanitizedText;
	}
}
