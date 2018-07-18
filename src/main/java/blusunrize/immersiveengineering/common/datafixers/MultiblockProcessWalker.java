/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.datafixers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.walkers.Filtered;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class MultiblockProcessWalker extends Filtered
{

	public static final String PROCESS_INPUT_ITEM = "process_inputItem";

	public MultiblockProcessWalker(Class<? extends TileEntity> p_i47309_1_)
	{
		super(p_i47309_1_);
	}

	@Nonnull
	@Override
	public NBTTagCompound filteredProcess(@Nonnull IDataFixer fixer, @Nonnull NBTTagCompound compound, int versionIn)
	{
		NBTTagList queue = compound.getTagList("processQueue", Constants.NBT.TAG_COMPOUND);
		for(int i = 0; i < queue.tagCount(); i++)
		{
			NBTTagCompound process = queue.getCompoundTagAt(i);
			if(process.hasKey("process_inputItem", Constants.NBT.TAG_COMPOUND))
				DataFixesManager.processItemStack(fixer, process, versionIn, PROCESS_INPUT_ITEM);

		}
		return compound;
	}
}
