package blusunrize.immersiveengineering.api.multiblocks.blocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;

public interface IClientTickableComponent<State> extends IMultiblockComponent<State>
{
	void tickClient(IMultiblockContext<State> context);
}
