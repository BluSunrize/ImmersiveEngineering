package blusunrize.immersiveengineering.api.multiblocks.blocks.logic;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;

public interface IMultiblockBE<State extends IMultiblockState>
{
	IMultiblockBEHelper<State> getHelper();
}
