package blusunrize.immersiveengineering.api.multiblocks.blocks.env;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockFace;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.RelativeBlockFace;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

@NonExtendable
public interface IMultiblockContext<State extends IMultiblockState> extends ICommonMultiblockContext
{
	default void markDirtyAndSync()
	{
		markMasterDirty();
		requestMasterBESync();
	}

	void markMasterDirty();

	State getState();

	IMultiblockLevel getLevel();

	<T> LazyOptional<T> registerCapability(T value);

	boolean isValid();

	void requestMasterBESync();

	void setComparatorOutputFor(BlockPos posInMultiblock, int newValue);

	default int getRedstoneInputValue(MultiblockFace face, int fallback)
	{
		return getRedstoneInputValue(face.posInMultiblock(), face.face(), fallback);
	}

	int getRedstoneInputValue(BlockPos posInMultiblock, RelativeBlockFace side, int fallback);

	int getRedstoneInputValue(BlockPos posInMultiblock, int fallback);
}
