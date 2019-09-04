/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

public enum EnumMetals
{
	COPPER,
	ALUMINUM,
	LEAD,
	SILVER,
	NICKEL,
	URANIUM,
	CONSTANTAN(Type.IE_ALLOY),
	ELECTRUM(Type.IE_ALLOY),
	STEEL(Type.IE_ALLOY),
	IRON(Type.VANILLA),
	GOLD(Type.VANILLA);

	private final Type type;

	EnumMetals(Type t)
	{
		this.type = t;
	}

	EnumMetals()
	{
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
		return name().toLowerCase();
	}

	private enum Type
	{
		VANILLA,
		IE_PURE,
		IE_ALLOY
	}
}