package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;

// TODO client equivalent
public interface IServerTickableMultiblock<State extends IMultiblockState> extends IMultiblockLogic<State>
{
	void tickServer(IMultiblockContext<State> context);
}
