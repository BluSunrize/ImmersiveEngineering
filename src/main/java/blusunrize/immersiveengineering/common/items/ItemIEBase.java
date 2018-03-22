/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;

public class ItemIEBase extends Item implements IColouredItem
{
	public String itemName;
	private int burnTime = -1;
	private boolean hidden = false;

	public ItemIEBase(String name, int stackSize)
	{
		this.setUnlocalizedName(ImmersiveEngineering.MODID+"."+name);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		this.setMaxStackSize(stackSize);
		this.itemName = name;
		IEContent.registeredIEItems.add(this);
	}

	public ItemIEBase(String name, int stackSize, String orePrefix)
	{
		this(name, stackSize);
		String oreName = Utils.createOreDictName(name);
		if(orePrefix!=null&&!orePrefix.isEmpty())
			oreName = oreName.substring(0,1).toUpperCase()+oreName.substring(1);
		OreDictionary.registerOre(orePrefix+oreName, new ItemStack(this));
	}

	@Override
	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items)
	{
		if (!hidden)
			super.getSubItems(tab, items);
	}

	public ItemIEBase setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
	}
	@Override
	public int getItemBurnTime(ItemStack itemStack)
	{
		return this.burnTime;
	}

	public boolean isHidden()
	{
		return hidden;
	}

	public ItemIEBase setHidden(boolean hidden)
	{
		this.hidden = hidden;
		return this;
	}
}
