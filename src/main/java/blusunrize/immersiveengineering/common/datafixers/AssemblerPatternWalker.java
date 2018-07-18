/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.datafixers;

import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.walkers.Filtered;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;

public class AssemblerPatternWalker extends Filtered
{
	public AssemblerPatternWalker()
	{
		super(TileEntityAssembler.class);
	}

	@Nonnull
	@Override
	public NBTTagCompound filteredProcess(@Nonnull IDataFixer fixer, @Nonnull NBTTagCompound compound, int versionIn)
	{
		for(int pattern = 0; pattern < 3; pattern++)
		{
			NBTTagList patternNBT = compound.getTagList("pattern"+pattern, Constants.NBT.TAG_COMPOUND);
			for(int slot = 0; slot < patternNBT.tagCount(); slot++)
			{
				NBTTagCompound in = patternNBT.getCompoundTagAt(slot);
				patternNBT.set(slot, fixer.process(FixTypes.ITEM_INSTANCE, in, versionIn));
			}
		}
		return compound;
	}
}
