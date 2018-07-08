/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.datafixers;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBottlingMachine;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.walkers.Filtered;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class BottlingQueueWalker extends Filtered
{
	public BottlingQueueWalker()
	{
		super(TileEntityBottlingMachine.class);
	}

	@Nonnull
	@Override
	public NBTTagCompound filteredProcess(@Nonnull IDataFixer fixer, @Nonnull NBTTagCompound compound, int versionIn)
	{
		NBTTagList queue = compound.getTagList("bottlingQueue", Constants.NBT.TAG_COMPOUND);
		for(int i = 0; i < queue.tagCount(); i++)
		{
			NBTTagCompound process = queue.getCompoundTagAt(i);
			DataFixesManager.processItemStack(fixer, process, versionIn, "input");
			if(process.hasKey("output", Constants.NBT.TAG_COMPOUND))
				DataFixesManager.processItemStack(fixer, process, versionIn, "output");
		}
		return compound;
	}
}
