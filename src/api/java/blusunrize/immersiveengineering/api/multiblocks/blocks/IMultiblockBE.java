package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;

public interface IMultiblockBE<State extends IMultiblockState>
{
	IMultiblockBEHelper<State> getHelper();
}
