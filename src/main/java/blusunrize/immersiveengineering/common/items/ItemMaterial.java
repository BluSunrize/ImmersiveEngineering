/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;


public class ItemMaterial extends ItemIEBase
{
	private String subtype;

	public ItemMaterial(String subtype)
	{
		super(subtype, 64);
		this.subtype = subtype;
	}

	@Nonnull
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return super.getUnlocalizedName(stack)+"."+subtype;
	}
}