/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.coremods;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

@Mixin(FlowingFluid.class)
public class WaterwheelBoundsMixin {
	@Inject(
			method = "canPassThrough(" +
					"Lnet/minecraft/world/level/BlockGetter;" +
					"Lnet/minecraft/world/level/material/Fluid;" +
					"Lnet/minecraft/core/BlockPos;" +
					"Lnet/minecraft/world/level/block/state/BlockState;" +
					"Lnet/minecraft/core/Direction;" +
					"Lnet/minecraft/core/BlockPos;" +
					"Lnet/minecraft/world/level/block/state/BlockState;" +
					"Lnet/minecraft/world/level/material/FluidState;)Z",
			at = @At("HEAD"),
			cancellable = true)
	protected void canPassThrough(BlockGetter pLevel, Fluid pFluid, BlockPos pFromPos, BlockState p_75967_,
								  Direction pDirection, BlockPos p_75969_, BlockState p_75970_, FluidState p_75971_,
								  CallbackInfoReturnable<Boolean> info) {

		if (pDirection.getAxis() == Axis.Y)
			return;

		BlockPos pos = pFromPos.below();
		BlockState state = pLevel.getBlockState(pos);

		if (state.getBlock().equals(WoodenDevices.WATERMILL.get())) {
			if (state.getValue(IEProperties.FACING_HORIZONTAL).getAxis().equals(pDirection.getAxis()))
				info.setReturnValue(false);
		}
	}
}