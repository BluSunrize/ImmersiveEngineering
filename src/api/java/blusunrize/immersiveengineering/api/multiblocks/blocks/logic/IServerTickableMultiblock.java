package blusunrize.immersiveengineering.api.multiblocks.blocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic.IMultiblockState;

public interface IServerTickableMultiblock<State extends IMultiblockState> extends IMultiblockLogic<State>
{
	void tickServer(IMultiblockContext<State> context);
}
