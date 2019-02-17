/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat;

import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.event.FMLInterModComms;

public class ChiselHelper extends IECompatModule
{
	@Override
	public void preInit()
	{
	}

	@Override
	public void registerRecipes()
	{
	}

	@Override
	public void init()
	{
		addVariation("treated_wood", IEContent.blockTreatedWood, 0);
		addVariation("treated_wood", IEContent.blockTreatedWood, 1);
		addVariation("treated_wood", IEContent.blockTreatedWood, 2);
		addVariation("steel_scaffold", IEContent.blockMetalDecoration1, 1);
		addVariation("steel_scaffold", IEContent.blockMetalDecoration1, 2);
		addVariation("steel_scaffold", IEContent.blockMetalDecoration1, 3);
		addVariation("aluminum_scaffold", IEContent.blockMetalDecoration1, 5);
		addVariation("aluminum_scaffold", IEContent.blockMetalDecoration1, 6);
		addVariation("aluminum_scaffold", IEContent.blockMetalDecoration1, 7);
	}

	@Override
	public void postInit()
	{
	}

	private void addVariation(String group, Block block, int meta)
	{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("group", group);
		tag.setString("block", block.getRegistryName().toString());
		tag.setInteger("meta", meta);
		FMLInterModComms.sendMessage("chisel", "add_variation", tag);
	}
}