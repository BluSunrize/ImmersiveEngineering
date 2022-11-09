package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;

public interface IClientTickableMultiblock<State extends IMultiblockState> extends IMultiblockLogic<State>
{
	void tickClient(IMultiblockContext<State> context);
}
