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
	CONSTANTAN,
	ELECTRUM,
	STEEL,
	IRON(true),
	GOLD(true);

	private final boolean isVanilla;

	EnumMetals(boolean isVanilla)
	{
		this.isVanilla = isVanilla;
	}

	EnumMetals()
	{
		this.isVanilla = false;
	}

	public boolean isVanillaMetal()
	{
		return isVanilla;
	}
}