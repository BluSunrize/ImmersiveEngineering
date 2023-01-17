/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.computers.generic.owners;

import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration.ExtraComponent;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.RedstoneControl;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;
import net.minecraft.core.BlockPos;

import java.util.Set;
import java.util.function.Function;

public class MultiblockCallbackWrapper<S extends IMultiblockState> extends CallbackOwner<IMultiblockBE<S>>
{
	private final MultiblockRegistration<S> multiblock;
	private final Set<BlockPos> validPositions;

	public MultiblockCallbackWrapper(
			Callback<S> innerCallback, MultiblockRegistration<S> multiblock, String name, BlockPos... validPositions
	)
	{
		super((Class<IMultiblockBE<S>>)(Class<?>)IMultiblockBE.class, name);
		this.multiblock = multiblock;
		this.validPositions = Set.of(validPositions);
		addAdditional(innerCallback, stateGetter());
		for(final ExtraComponent<S, ?> component : multiblock.extraComponents())
		{
			if(!(component.makeWrapper() instanceof RedstoneControl<?>))
				continue;
			final RedstoneControl<S> rsControl = (RedstoneControl<S>)component.makeWrapper();
			if(rsControl.allowComputerControl())
				addAdditional(new RedstoneControlCallbacks(), stateGetter().andThen(rsControl::wrapState));
		}
	}

	private Function<IMultiblockBE<S>, S> stateGetter()
	{
		return imbe -> imbe.getHelper().getState();
	}

	@Override
	public boolean canAttachTo(IMultiblockBE<S> candidate)
	{
		final IMultiblockBEHelper<S> helper = candidate.getHelper();
		if(helper.getMultiblock()!=multiblock)
			return false;
		else
			return validPositions.contains(helper.getPositionInMB());
	}
}
