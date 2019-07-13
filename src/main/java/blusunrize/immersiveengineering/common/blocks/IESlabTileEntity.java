/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.nbt.CompoundNBT;

public class IESlabTileEntity extends IEBaseTileEntity
{
	public int slabType = 0;

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		slabType = nbt.getInt("slabType");
		if(descPacket&&world!=null)
			this.markContainingBlockForUpdate(null);
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putInt("slabType", slabType);
	}
}