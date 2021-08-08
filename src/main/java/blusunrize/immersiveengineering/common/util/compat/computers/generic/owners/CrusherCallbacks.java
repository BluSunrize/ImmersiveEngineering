/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockBlockEntity.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherBlockEntity;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.IndexArgument;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.PoweredMBCallbacks;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class CrusherCallbacks extends MultiblockCallbackOwner<CrusherBlockEntity>
{
	public CrusherCallbacks()
	{
		super(CrusherBlockEntity.class, "crusher");
		addAdditional(PoweredMBCallbacks.INSTANCE);
	}

	@ComputerCallable
	public ItemStack getInputQueueElement(CallbackEnvironment<CrusherBlockEntity> env, @IndexArgument int index)
	{
		List<MultiblockProcess<CrusherRecipe>> queue = env.getObject().processQueue;
		if(index < 0||index >= queue.size())
			throw new RuntimeException("Invalid index, queue contains "+queue.size()+" elements");
		MultiblockProcess<CrusherRecipe> process = queue.get(index);
		if(process instanceof PoweredMultiblockBlockEntity.MultiblockProcessInWorld<?>)
			return ((MultiblockProcessInWorld<CrusherRecipe>)process).inputItems.get(0);
		else
			return null;
	}

	@ComputerCallable
	public int getQueueSize(CallbackEnvironment<CrusherBlockEntity> env)
	{
		return env.getObject().processQueue.size();
	}
}
