/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.common.blocks.metal.MixerTileEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.IndexArgument;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.InventoryCallbacks;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.PoweredMBCallbacks;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class MixerCallbacks extends MultiblockCallbackOwner<MixerTileEntity>
{
	public MixerCallbacks()
	{
		super(MixerTileEntity.class, "mixer");
		addAdditional(PoweredMBCallbacks.INSTANCE);
		addAdditional(new InventoryCallbacks<>(te -> te.inventory, 0, 12, "input"));
	}

	@ComputerCallable
	public FluidStack getFluid(CallbackEnvironment<MixerTileEntity> env, @IndexArgument int index)
	{
		List<FluidStack> fluids = env.getObject().tank.fluids;
		if(index >= 0&&index < fluids.size())
			return fluids.get(index);
		else
			throw new RuntimeException("Tank currently contains "+fluids.size()+" fluids!");
	}

	@ComputerCallable
	public int getCapacity(CallbackEnvironment<MixerTileEntity> env)
	{
		return env.getObject().tank.getCapacity();
	}

	@ComputerCallable
	public int getNumFluids(CallbackEnvironment<MixerTileEntity> env)
	{
		return env.getObject().tank.fluids.size();
	}
}
