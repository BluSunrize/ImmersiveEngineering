/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.datafixers;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

import javax.annotation.Nonnull;

import static blusunrize.immersiveengineering.common.items.ItemIETool.CUTTER_META;
import static blusunrize.immersiveengineering.common.items.ItemIETool.HAMMER_META;

public class DataFixerHammerCutterDamage implements IFixableData
{

	@Override
	public int getFixVersion()
	{
		return 1;
	}

	@Nonnull
	@Override
	public NBTTagCompound fixTagCompound(@Nonnull NBTTagCompound compound)
	{
		if(IEContent.itemTool.getRegistryName().toString()
				.equals(compound.getString("id")))
		{
			int meta = compound.getInteger("Damage");
			if(meta==CUTTER_META||meta==HAMMER_META)
			{
				int damage;
				NBTTagCompound stackTag = compound.getCompoundTag("tag");
				if(meta==CUTTER_META)
				{
					damage = stackTag.getInteger("cutterDmg");
					stackTag.removeTag("cutterDmg");
				}
				else
				{
					damage = stackTag.getInteger("hammerDmg");
					stackTag.removeTag("hammerDmg");
				}
				stackTag.setInteger(Lib.NBT_DAMAGE, damage);
			}
		}
		return compound;
	}
}
