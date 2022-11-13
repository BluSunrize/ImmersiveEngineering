package blusunrize.immersiveengineering.api.multiblocks.blocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic.IMultiblockState;

public interface IClientTickableMultiblock<State extends IMultiblockState> extends IMultiblockLogic<State>
{
	void tickClient(IMultiblockContext<State> context);
}
