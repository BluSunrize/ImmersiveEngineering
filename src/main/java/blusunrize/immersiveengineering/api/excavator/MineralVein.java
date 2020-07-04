/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.excavator;

import blusunrize.immersiveengineering.common.IESaveData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ResourceLocationException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColumnPos;

import javax.annotation.Nullable;

public class MineralVein
{
	private final ColumnPos pos;
	private final MineralMix mineral;
	private final int radius;
	private int depletion;
	private MineralMix mineralOverride;

	public MineralVein(ColumnPos pos, MineralMix mineral, int radius)
	{
		this.pos = pos;
		this.mineral = mineral;
		this.radius = radius;
	}

	public ColumnPos getPos()
	{
		return pos;
	}

	public MineralMix getMineral()
	{
		return mineral;
	}

	public int getRadius()
	{
		return radius;
	}

	// exponential growth of the fail chance depending on the distance from the center of the vein
	public double getFailChance(BlockPos pos)
	{
		double dX = pos.getX()-this.pos.x;
		double dZ = pos.getZ()-this.pos.z;
		double d = (dX*dX+dZ*dZ)/(radius*radius);
		return (-2*Math.pow(d, 3))+(3*Math.pow(d, 2));
	}

	public int getDepletion()
	{
		return depletion;
	}

	public void setDepletion(int depletion)
	{
		this.depletion = depletion;
	}

	public void deplete()
	{
		depletion++;
		IESaveData.setDirty();
	}

	public boolean isDepleted()
	{
		return ExcavatorHandler.mineralVeinYield==0||getDepletion() >= ExcavatorHandler.mineralVeinYield;
	}

	public MineralMix getMineralOverride()
	{
		return mineralOverride;
	}

	public void setMineralOverride(MineralMix mineralOverride)
	{
		this.mineralOverride = mineralOverride;
	}

	public MineralMix getActualMineral()
	{
		if(getMineralOverride()!=null)
			return getMineralOverride();
		return getMineral();
	}

	public CompoundNBT writeToNBT()
	{
		CompoundNBT tag = new CompoundNBT();
		tag.putInt("x", pos.x);
		tag.putInt("z", pos.z);
		tag.putString("mineral", mineral.getId().toString());
		tag.putInt("radius", radius);
		tag.putInt("depletion", depletion);
		if(mineralOverride!=null)
			tag.putString("mineralOverride", mineralOverride.getId().toString());
		return tag;
	}

	@Nullable
	public static MineralVein readFromNBT(CompoundNBT tag)
	{
		try
		{
			ColumnPos pos = new ColumnPos(tag.getInt("x"), tag.getInt("z"));
			ResourceLocation id = new ResourceLocation(tag.getString("mineral"));
			int radius = tag.getInt("radius");
			MineralVein info = new MineralVein(pos, MineralMix.mineralList.get(id), radius);
			info.depletion = tag.getInt("depletion");

			if(tag.contains("mineralOverride"))
			{
				id = new ResourceLocation(tag.getString("mineralOverride"));
				info.mineralOverride = MineralMix.mineralList.get(id);
			}
			return info;
		} catch(ResourceLocationException ex)
		{
			return null;
		}
	}
}
