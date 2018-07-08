/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.datafixers;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.wooden.BlockTypes_WoodenDevice0;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;

import javax.annotation.Nonnull;

public class IEItemFixWalker implements IDataWalker
{
	private final String CRATE_ID, TOOLBOX_ID;

	public IEItemFixWalker()
	{
		CRATE_ID = IEContent.blockWoodenDevice0.getRegistryName().toString();
		TOOLBOX_ID = IEContent.itemToolbox.getRegistryName().toString();
	}

	@Nonnull
	@Override
	public NBTTagCompound process(@Nonnull IDataFixer fixer, @Nonnull NBTTagCompound compound, int versionIn)
	{
		String type = compound.getString("id");
		if(type.equals(CRATE_ID))
		{
			int meta = compound.getInteger("Damage");
			if(meta==BlockTypes_WoodenDevice0.CRATE.getMeta()||meta==BlockTypes_WoodenDevice0.REINFORCED_CRATE.getMeta())
			{
				NBTTagCompound stackTag = compound.getCompoundTag("tag");
				DataFixesManager.processInventory(fixer, stackTag, versionIn, "inventory");
			}
		}
		else if(type.equals(TOOLBOX_ID))
		{
			NBTTagCompound stackTag = compound.getCompoundTag("ForgeCaps").getCompoundTag("Parent");
			DataFixesManager.processInventory(fixer, stackTag, versionIn, "Items");
		}
		return compound;
	}
}
