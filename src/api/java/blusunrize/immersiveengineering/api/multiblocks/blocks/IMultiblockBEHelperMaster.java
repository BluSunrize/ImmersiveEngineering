package blusunrize.immersiveengineering.api.multiblocks.blocks;

import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;

import javax.annotation.Nonnull;

public interface IMultiblockBEHelperMaster<State extends IMultiblockState> extends IMultiblockBEHelper<State>
{
	SetRestrictedField<Factory> MAKE_HELPER = SetRestrictedField.common();

	void invalidateCaps();

	@Nonnull
	@Override
	State getState();

	@Nonnull
	@Override
	IMultiblockContext<State> getContext();

	interface Factory
	{
		<T extends IMultiblockState>
		IMultiblockBEHelperMaster<T> makeFor(MultiblockBlockEntityMaster<T> be, MultiblockRegistration<T> logic);
	}
}
