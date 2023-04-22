/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.compat.jade;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelper;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockBE;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SheetmetalTankLogic.State;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Identifiers;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

import java.util.List;

public class SheetmetalTankDataProvider<T extends IMultiblockState> implements IServerExtensionProvider<IMultiblockBE<T>, CompoundTag>
{
	@Override
	public @Nullable List<ViewGroup<CompoundTag>> getGroups(ServerPlayer serverPlayer, ServerLevel serverLevel, IMultiblockBE<T> tank, boolean b)
	{
		final IMultiblockBEHelper<State> tankHelper = tank.getHelper().asType(IEMultiblockLogic.TANK);
		if(tankHelper==null)
			return null;
		return FluidView.fromFluidHandler(tankHelper.getState().tank);
	}

	@Override
	public ResourceLocation getUid()
	{
		return Identifiers.UNIVERSAL_FLUID_STORAGE;
	}
}
