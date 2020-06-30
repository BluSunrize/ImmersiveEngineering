/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.excavator;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;

import javax.annotation.Nullable;

public class MineralWorldInfo
{
	public MineralMix mineral;
	public MineralMix mineralOverride;
	public int depletion;

	public CompoundNBT writeToNBT()
	{
		CompoundNBT tag = new CompoundNBT();
		if(mineral!=null)
			tag.putString("mineral", mineral.getId().toString());
		if(mineralOverride!=null)
			tag.putString("mineralOverride", mineralOverride.getId().toString());
		tag.putInt("depletion", depletion);
		return tag;
	}

	@Nullable
	public static MineralWorldInfo readFromNBT(CompoundNBT tag)
	{
		try
		{
			MineralWorldInfo info = new MineralWorldInfo();
			if(tag.contains("mineral"))
			{
				ResourceLocation id = new ResourceLocation(tag.getString("mineral"));
				info.mineral = MineralMix.mineralList.get(id);
			}
			if(tag.contains("mineralOverride"))
			{
				ResourceLocation id = new ResourceLocation(tag.getString("mineralOverride"));
				info.mineralOverride = MineralMix.mineralList.get(id);
			}
			info.depletion = tag.getInt("depletion");
			return info;
		} catch(ResourceLocationException ex)
		{
			return null;
		}
	}
}
