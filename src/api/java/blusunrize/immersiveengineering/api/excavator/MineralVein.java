/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.excavator;

import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ColumnPos;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nullable;

public class MineralVein
{
	private final ColumnPos pos;
	private final ResourceLocation mineralName;
	private final Lazy<MineralMix> mineral;
	private final int radius;
	private int depletion;

	public MineralVein(ColumnPos pos, ResourceLocation mineral, int radius)
	{
		this.pos = pos;
		this.mineralName = mineral;
		this.mineral = Lazy.of(() -> MineralMix.mineralList.get(mineralName));
		this.radius = radius;
	}

	public ColumnPos getPos()
	{
		return pos;
	}

	@Nullable
	public MineralMix getMineral()
	{
		return mineral.get();
	}

	public int getRadius()
	{
		return radius;
	}

	// fail chance grows with distance from the center of the vein
	public double getFailChance(BlockPos pos)
	{
		double dX = pos.getX()-this.pos.x;
		double dZ = pos.getZ()-this.pos.z;
		double d = (dX*dX+dZ*dZ)/(radius*radius);
		return d*d*(-2*d+3);
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
		if(!isDepleted())
		{
			depletion++;
			ExcavatorHandler.MARK_SAVE_DATA_DIRTY.getValue().run();
		}
	}

	public boolean isDepleted()
	{
		return ExcavatorHandler.mineralVeinYield > 0&&getDepletion() >= ExcavatorHandler.mineralVeinYield;
	}

	public CompoundTag writeToNBT()
	{
		CompoundTag tag = new CompoundTag();
		tag.putInt("x", pos.x);
		tag.putInt("z", pos.z);
		tag.putString("mineral", mineralName.toString());
		tag.putInt("radius", radius);
		tag.putInt("depletion", depletion);
		return tag;
	}

	@Nullable
	public static MineralVein readFromNBT(CompoundTag tag)
	{
		try
		{
			ColumnPos pos = new ColumnPos(tag.getInt("x"), tag.getInt("z"));
			ResourceLocation id = new ResourceLocation(tag.getString("mineral"));
			int radius = tag.getInt("radius");
			MineralVein info = new MineralVein(pos, id, radius);
			info.depletion = tag.getInt("depletion");
			return info;
		} catch(ResourceLocationException ex)
		{
			return null;
		}
	}
}
