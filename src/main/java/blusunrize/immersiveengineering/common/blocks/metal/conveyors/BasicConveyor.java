/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants.NBT;

import javax.annotation.Nullable;

/**
 * @author BluSunrize - 20.08.2016
 */
//TODO support covers in here, rather than having to make a covered and a non-covered version of every belt?
public class BasicConveyor implements IConveyorBelt
{
	public static final ResourceLocation NAME = new ResourceLocation(ImmersiveEngineering.MODID, "conveyor");

	ConveyorDirection direction = ConveyorDirection.HORIZONTAL;
	@Nullable
	DyeColor dyeColour = null;

	@Override
	public ConveyorDirection getConveyorDirection()
	{
		return direction;
	}

	@Override
	public boolean changeConveyorDirection()
	{
		direction = direction==ConveyorDirection.HORIZONTAL?ConveyorDirection.UP: direction==ConveyorDirection.UP?ConveyorDirection.DOWN: ConveyorDirection.HORIZONTAL;
		return true;
	}

	@Override
	public boolean setConveyorDirection(ConveyorDirection dir)
	{
		direction = dir;
		return true;
	}

	@Override
	public boolean isActive(TileEntity tile)
	{
		return tile.getWorld().getRedstonePowerFromNeighbors(tile.getPos()) <= 0;
	}

	@Override
	public boolean canBeDyed()
	{
		return true;
	}

	@Override
	public boolean setDyeColour(DyeColor colour)
	{
		if(colour==this.dyeColour)
			return false;
		this.dyeColour = colour;
		return true;
	}

	@Override
	public DyeColor getDyeColour()
	{
		return this.dyeColour;
	}

	@Override
	public CompoundNBT writeConveyorNBT()
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("direction", direction.ordinal());
		if(dyeColour!=null)
			nbt.putInt("dyeColour", dyeColour.getId());
		return nbt;
	}

	@Override
	public void readConveyorNBT(CompoundNBT nbt)
	{
		direction = ConveyorDirection.values()[nbt.getInt("direction")];
		if(nbt.contains("dyeColour", NBT.TAG_INT))
			dyeColour = DyeColor.byId(nbt.getInt("dyeColour"));
		else
			dyeColour = null;

	}

	public static ResourceLocation texture_on = new ResourceLocation("immersiveengineering:blocks/conveyor");
	public static ResourceLocation texture_off = new ResourceLocation("immersiveengineering:blocks/conveyor_off");

	@Override
	public ResourceLocation getActiveTexture()
	{
		return texture_on;
	}

	@Override
	public ResourceLocation getInactiveTexture()
	{
		return texture_off;
	}
}