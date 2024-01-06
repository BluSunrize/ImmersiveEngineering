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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Accessor;
import snownee.jade.api.view.*;
import snownee.jade.util.JadeForgeUtils;

import java.util.ArrayList;
import java.util.List;

public class MultiblockTankDataProvider<T extends IMultiblockState> implements IServerExtensionProvider<IMultiblockBE<T>, CompoundTag>, IClientExtensionProvider<CompoundTag, FluidView>
{
	@Override
	public @Nullable List<ViewGroup<CompoundTag>> getGroups(ServerPlayer serverPlayer, ServerLevel serverLevel, IMultiblockBE<T> multiblockBE, boolean b)
	{
		final IMultiblockBEHelper<T> helper = multiblockBE.getHelper();
		if(helper.getState() instanceof ProcessContext<?> state)
		{
			if(state.getInternalTanks().length > 0)
			{
				List<CompoundTag> list = new ArrayList<>();
				for(int i = 0; i < state.getInternalTanks().length; i++)
				{
					FluidStack fluid = state.getInternalTanks()[i].getFluid();
					long capacity = state.getInternalTanks()[i].getCapacity();
					list.add(JadeForgeUtils.fromFluidStack(fluid, capacity));
				}
				return List.of(new ViewGroup<>(list));
			}
		}
		else if(helper.getState() instanceof SheetmetalTankLogic.State state)
			return JadeForgeUtils.fromFluidHandler(state.tank);
		else if(helper.getState() instanceof CokeOvenLogic.State state)
			return JadeForgeUtils.fromFluidHandler(state.getTank());
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
}
