/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.env;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.ApiStatus.NonExtendable;

import javax.annotation.Nonnull;

@NonExtendable
public interface IMultiblockBEHelperMaster<State extends IMultiblockState> extends IMultiblockBEHelper<State>
{
	SetRestrictedField<Factory> MAKE_HELPER = SetRestrictedField.common();

	@Nonnull
	@Override
	State getState();

	@Nonnull
	@Override
	IMultiblockContext<State> getContext();

	AABB getRenderBoundingBox();

	void tickServer();

	void tickClient();

	void onRemoved();

	void invalidateAllCaps();

	interface Factory
	{
		<T extends IMultiblockState>
		IMultiblockBEHelperMaster<T> makeFor(MultiblockBlockEntityMaster<T> be, MultiblockRegistration<T> logic);
	}
}
