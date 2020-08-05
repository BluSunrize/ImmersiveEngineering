/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.links;

import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualUtils;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class Link
{
	public final static String FORMAT = TextFormatting.ITALIC.toString()+TextFormatting.UNDERLINE.toString();
	private final List<String> parts;
	@Nullable
	private final ResourceLocation target;
	private final String targetAnchor;
	private final int targetOffset;

	public Link(String text, String target, String targetInEntry, ManualInstance manual)
	{
		List<String> parts = new ArrayList<>();
		String[] resultParts = text.split("(?<= )");// Split and keep the whitespace at the end of the tokens
		for(String resultPart : resultParts)
		{
			String part = FORMAT+resultPart;
			parts.add(part);
		}
		if(ManualUtils.THIS.equals(target))
			this.target = null;
		else
			this.target = ManualUtils.getLocationForManual(target, manual);
		this.parts = ImmutableList.copyOf(parts);
		if(targetInEntry.contains("+"))
		{
			try
			{
				int plus = targetInEntry.indexOf('+');
				targetAnchor = targetInEntry.substring(0, plus);
				targetOffset = Integer.parseInt(targetInEntry.substring(plus+1));
			} catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		}
		else
		{
			targetAnchor = targetInEntry;
			targetOffset = 0;
		}
	}

	public List<String> getParts()
	{
		return parts;
	}

	public String getTargetAnchor()
	{
		return targetAnchor;
	}

	public ResourceLocation getTarget(ManualEntry fromEntry)
	{
		return target==null?fromEntry.getLocation(): target;
	}

	public int getTargetOffset()
	{
		return targetOffset;
	}
}
