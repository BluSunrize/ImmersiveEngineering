package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;

public interface IServerTickableMultiblock<State extends IMultiblockState> extends IMultiblockLogic<State>
{
	void tickServer(IMultiblockContext<State> context);
}
