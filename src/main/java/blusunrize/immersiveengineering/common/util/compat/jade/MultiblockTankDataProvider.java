/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.compat.jade;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CokeOvenLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SheetmetalTankLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.*;

import java.util.Arrays;
import java.util.List;

public class MultiblockTankDataProvider<T extends IMultiblockState> implements IServerExtensionProvider<CompoundTag>, IClientExtensionProvider<CompoundTag, FluidView>
{
	@Override
	public @Nullable List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor)
	{
		if(accessor.getTarget() instanceof IMultiblockBE<?> multiblockBE)
		{
			final IMultiblockBEHelper<?> helper = multiblockBE.getHelper();
			if(helper.getState() instanceof ProcessContext<?> state)
			{
				IFluidTank[] tanks = state.getInternalTanks();
				if(tanks.length > 0)
					return List.of(new ViewGroup<>(
							Arrays.stream(tanks).map(this::getTagFromTank).toList()
					));
			}
			else if(helper.getState() instanceof SheetmetalTankLogic.State state)
				return getViewFromTank(state.tank);
			else if(helper.getState() instanceof CokeOvenLogic.State state)
				return getViewFromTank(state.getTank());
		}
		return null;
	}


	@Override
	public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> list)
	{
		return ClientViewGroup.map(list, FluidView::readDefault, null);
	}

	@Override
	public ResourceLocation getUid()
	{
		return ImmersiveEngineering.rl("multiblock_tank");
	}

	private CompoundTag getTagFromTank(IFluidTank tank)
	{
		FluidStack fs = tank.getFluid();
		return FluidView.writeDefault(
				JadeFluidObject.of(fs.getFluid(), fs.getAmount(), fs.getComponentsPatch()),
				tank.getCapacity()
		);
	}

	private List<ViewGroup<CompoundTag>> getViewFromTank(IFluidTank tank)
	{
		return List.of(new ViewGroup<>(List.of(getTagFromTank(tank))));
	}
}
