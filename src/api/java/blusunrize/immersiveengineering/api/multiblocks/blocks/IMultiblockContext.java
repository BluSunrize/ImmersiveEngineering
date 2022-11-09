package blusunrize.immersiveengineering.api.multiblocks.blocks;

import net.minecraftforge.common.util.LazyOptional;

public interface IMultiblockContext<State extends IMultiblockLogic.IMultiblockState> extends MultiblockCapabilitySource
{
	State getState();

	IMultiblockLevel getLevel();

	<T> LazyOptional<T> registerCapability(T value);

	boolean isValid();

	void requestMasterBESync();
}
