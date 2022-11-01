package blusunrize.immersiveengineering.api.multiblocks.blocks;

import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nullable;

public interface IMultiblockContext<State extends IMultiblockLogic.IMultiblockState> extends MultiblockCapabilitySource
{
	State getState();

	void markMasterDirty();

	IMultiblockLevel getLevel();

	<T> LazyOptional<T> registerCapability(T value);

	default <T> LazyOptional<T> orRegisterCapability(@Nullable LazyOptional<T> existingCap, T value)
	{
		if(existingCap!=null&&existingCap.isPresent())
			return existingCap;
		else
			return registerCapability(value);
	}
}
