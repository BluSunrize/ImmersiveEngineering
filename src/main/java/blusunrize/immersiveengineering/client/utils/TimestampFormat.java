/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.client.resources.I18n;

public enum TimestampFormat
{
	D,
	H,
	M,
	S,
	MS,
	HMS,
	HM,
	DHMS,
	DHM,
	DH;
	static TimestampFormat[] coreValues = {TimestampFormat.D, TimestampFormat.H, TimestampFormat.M, TimestampFormat.S};

	public boolean containsFormat(TimestampFormat format)
	{
		return this.toString().contains(format.toString());
	}

	public long getTickCut()
	{
		return this==D?1728000L: this==H?72000L: this==M?1200L: this==S?20L: 1;
	}

	public String getLocalKey()
	{
		return this==D?"day": this==H?"hour": this==M?"minute": this==S?"second": "";
	}

	public static String formatTimestamp(long timestamp, TimestampFormat format)
	{
		StringBuilder s = new StringBuilder();
		for(TimestampFormat core : TimestampFormat.coreValues)
			if(format.containsFormat(core)&&timestamp >= core.getTickCut())
			{
				s.append(I18n.format(Lib.DESC_INFO+core.getLocalKey(), Long.toString(timestamp/core.getTickCut())));
				timestamp %= core.getTickCut();
			}
		if(s.length()==0)
			for(int i = TimestampFormat.coreValues.length-1; i >= 0; i--)
				if(format.containsFormat(TimestampFormat.coreValues[i]))
				{
					s = new StringBuilder(I18n.format(Lib.DESC_INFO+TimestampFormat.coreValues[i].getLocalKey(), 0));
					break;
				}
		return s.toString();
	}
}
