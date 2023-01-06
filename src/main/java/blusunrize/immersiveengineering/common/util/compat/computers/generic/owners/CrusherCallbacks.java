package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CrusherLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.IndexArgument;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.impl.MBEnergyCallbacks;
import net.minecraft.world.item.ItemStack;

public class CrusherCallbacks extends Callback<State>
{
	public CrusherCallbacks()
	{
		addAdditional(MBEnergyCallbacks.INSTANCE, State::getEnergy);
	}

	@ComputerCallable
	public boolean isRunning(CallbackEnvironment<State> env)
	{
		return env.object().shouldRenderActive();
	}

	@ComputerCallable
	public ItemStack getInputQueueElement(CallbackEnvironment<State> env, @IndexArgument int index)
	{
		var queue = env.object().getProcessQueue();
		if(index < 0||index >= queue.size())
			throw new RuntimeException("Invalid index, queue contains "+queue.size()+" elements");
		var process = queue.get(index);
		if(process instanceof MultiblockProcessInWorld<CrusherRecipe> inWorld)
			return inWorld.inputItems.get(0);
		else
			return null;
	}

	@ComputerCallable
	public int getQueueSize(CallbackEnvironment<State> env)
	{
		return env.object().getProcessQueue().size();
	}
}
