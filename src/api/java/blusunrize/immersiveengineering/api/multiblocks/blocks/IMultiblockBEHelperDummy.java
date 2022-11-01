package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IMultiblockBEHelperDummy<State extends IMultiblockState> extends IMultiblockBEHelper<State>
{
	SetRestrictedField<Factory> MAKE_HELPER = SetRestrictedField.common();

	void setPositionInMB(BlockPos pos);

	BlockPos getPositionInMB();

	interface Factory
	{
		<T extends IMultiblockState>
		IMultiblockBEHelperDummy<T> makeFor(BlockEntity be, MultiblockRegistration<T> logic);
	}
}
