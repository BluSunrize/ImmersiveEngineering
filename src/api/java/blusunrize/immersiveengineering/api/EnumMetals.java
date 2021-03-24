/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import java.util.Locale;

public enum EnumMetals
{
	COPPER(0.3f),
	ALUMINUM(0.3F),
	LEAD(0.7F),
	SILVER(1.0F),
	NICKEL(1.0F),
	URANIUM(1.0F),
	CONSTANTAN(Type.IE_ALLOY, Float.NaN),
	ELECTRUM(Type.IE_ALLOY, Float.NaN),
	STEEL(Type.IE_ALLOY, Float.NaN),
	IRON(Type.VANILLA, 0.7F),
	GOLD(Type.VANILLA, 1);

	private final Type type;
	public final float smeltingXP;

	EnumMetals(Type t, float xp)
	{
		this.type = t;
		this.smeltingXP = xp;
	}

	EnumMetals(float xp)
	{
		smeltingXP = xp;
		this.type = Type.IE_PURE;
	}

	public boolean isVanillaMetal()
	{
		return type==Type.VANILLA;
	}

	public boolean isAlloy()
	{
		return type==Type.IE_ALLOY;
	}

	public boolean shouldAddOre()
	{
		return !isVanillaMetal()&&!isAlloy();
	}

	public String tagName()
	{
		return name().toLowerCase(Locale.US);
	}

	private enum Type
	{
		VANILLA,
		IE_PURE,
		IE_ALLOY
	}
}