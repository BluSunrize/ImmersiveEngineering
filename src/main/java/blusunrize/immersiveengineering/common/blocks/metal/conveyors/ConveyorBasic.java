/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

/**
 * @author BluSunrize - 20.08.2016
 */
public class ConveyorBasic implements IConveyorBelt
{
	ConveyorDirection direction = ConveyorDirection.HORIZONTAL;
	int dyeColour = -1;

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
	public boolean setDyeColour(int colour)
	{
		if(colour==this.dyeColour)
			return false;
		this.dyeColour = colour;
		return true;
	}

	@Override
	public int getDyeColour()
	{
		return this.dyeColour;
	}

	@Override
	public NBTTagCompound writeConveyorNBT()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("direction", direction.ordinal());
		nbt.setInteger("dyeColour", dyeColour);
		return nbt;
	}

	@Override
	public void readConveyorNBT(NBTTagCompound nbt)
	{
		direction = ConveyorDirection.values()[nbt.getInteger("direction")];
		dyeColour = nbt.hasKey("dyeColour")?nbt.getInteger("dyeColour"): -1;
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