package blusunrize.immersiveengineering.api.multiblocks.blocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;

public interface IServerTickableComponent<State> extends IMultiblockComponent<State>
{
	void tickServer(IMultiblockContext<State> context);
}
