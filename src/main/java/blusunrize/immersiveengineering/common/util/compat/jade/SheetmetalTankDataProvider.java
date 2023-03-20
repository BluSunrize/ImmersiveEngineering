/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.compat.jade;

import blusunrize.immersiveengineering.common.blocks.metal.SheetmetalTankBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.Identifiers;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

import java.util.List;

public class SheetmetalTankDataProvider implements IServerExtensionProvider<SheetmetalTankBlockEntity, CompoundTag>
{
	@Override
	public @Nullable List<ViewGroup<CompoundTag>> getGroups(ServerPlayer serverPlayer, ServerLevel serverLevel, SheetmetalTankBlockEntity tank, boolean b)
	{
		FluidTank internalTank = tank.master().tank;
		return FluidView.fromFluidHandler(internalTank);
	}

	@Override
	public ResourceLocation getUid()
	{
		return Identifiers.UNIVERSAL_FLUID_STORAGE;
	}
}
