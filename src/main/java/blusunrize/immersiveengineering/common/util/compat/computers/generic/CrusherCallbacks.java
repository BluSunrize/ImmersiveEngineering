package blusunrize.immersiveengineering.common.util.compat.computers.generic;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherTileEntity;
import net.minecraft.item.ItemStack;

import java.util.List;


public class CrusherCallbacks extends PoweredMultiblockCallbacks<CrusherTileEntity>
{
	public CrusherCallbacks()
	{
		super(CrusherTileEntity.class, "crusher");
	}

	@ComputerCallable
	public ItemStack getInputQueueElement(CallbackEnvironment<CrusherTileEntity> env, int index)
	{
		//Lua has 1-based indexing
		--index;
		List<MultiblockProcess<CrusherRecipe>> queue = env.getObject().processQueue;
		if(index < 0||index >= queue.size())
			throw new RuntimeException("Invalid index, queue contains "+queue.size()+" elements");
		MultiblockProcess<CrusherRecipe> process = queue.get(index);
		if(process instanceof PoweredMultiblockTileEntity.MultiblockProcessInWorld<?>)
			return ((MultiblockProcessInWorld<CrusherRecipe>)process).inputItems.get(0);
		else
			return null;
	}

	@ComputerCallable
	public int getQueueSize(CallbackEnvironment<CrusherTileEntity> env)
	{
		return env.getObject().processQueue.size();
	}
}
