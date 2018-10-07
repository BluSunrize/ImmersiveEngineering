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
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemIEBase extends Item implements IColouredItem
{
	public String itemName;
	protected String[] subNames;
	boolean[] isMetaHidden;
	public boolean registerSubModels = true;
	private int[] burnTime;

	public ItemIEBase(String name, int stackSize, String... subNames)
	{
		this.setTranslationKey(ImmersiveEngineering.MODID+"."+name);
		this.setHasSubtypes(subNames!=null&&subNames.length > 0);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		this.setMaxStackSize(stackSize);
		this.itemName = name;
		this.subNames = subNames!=null&&subNames.length > 0?subNames: null;
		this.isMetaHidden = new boolean[this.subNames!=null?this.subNames.length: 1];
		this.burnTime = new int[this.subNames!=null?this.subNames.length: 1];
//		ImmersiveEngineering.registerItem(this, name);
		IEContent.registeredIEItems.add(this);
	}

	public String[] getSubNames()
	{
		return subNames;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)
	{
		if(this.isInCreativeTab(tab))
			if(getSubNames()!=null)
			{
				for(int i = 0; i < getSubNames().length; i++)
					if(!isMetaHidden(i))
						list.add(new ItemStack(this, 1, i));
			}
			else
				list.add(new ItemStack(this));

	}

	@Override
	public String getTranslationKey(ItemStack stack)
	{
		if(getSubNames()!=null)
		{
			String subName = stack.getMetadata() < getSubNames().length?getSubNames()[stack.getMetadata()]: "";
			return this.getTranslationKey()+"."+subName;
		}
		return this.getTranslationKey();
	}

	public ItemIEBase setMetaHidden(int... meta)
	{
		for(int i : meta)
			if(i >= 0&&i < this.isMetaHidden.length)
				this.isMetaHidden[i] = true;
		return this;
	}

	public ItemIEBase setMetaUnhidden(int... meta)
	{
		for(int i : meta)
			if(i >= 0&&i < this.isMetaHidden.length)
				this.isMetaHidden[i] = false;
		return this;
	}

	public boolean isMetaHidden(int meta)
	{
		return this.isMetaHidden[Math.max(0, Math.min(meta, this.isMetaHidden.length-1))];
	}

	public ItemIEBase setRegisterSubModels(boolean register)
	{
		this.registerSubModels = register;
		return this;
	}

	public ItemIEBase setBurnTime(int meta, int burnTime)
	{
		if(meta >= 0&&meta < this.burnTime.length)
			this.burnTime[meta] = burnTime;
		return this;
	}

	@Override
	public int getItemBurnTime(ItemStack itemStack)
	{
		return this.burnTime[Math.max(0, Math.min(itemStack.getMetadata(), this.burnTime.length-1))];
	}
}
