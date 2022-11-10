package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.LazyOptional;

public interface IMultiblockContext<State extends IMultiblockState> extends ICommonMultiblockContext
{
	State getState();

	IMultiblockLevel getLevel();

	<T> LazyOptional<T> registerCapability(T value);

	boolean isValid();

	void requestMasterBESync();

	void setComparatorOutputFor(BlockPos posInMultiblock, int newValue);
}
