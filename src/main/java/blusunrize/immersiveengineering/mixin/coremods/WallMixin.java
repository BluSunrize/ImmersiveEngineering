/*
 *  BluSunrize
 *  Copyright (c) 2025
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.coremods;

import blusunrize.immersiveengineering.common.blocks.generic.WindowBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WallBlock.class)
public abstract class WallMixin
{
	@Inject(method = "connectsTo", at = @At("HEAD"), cancellable = true)
	protected void deflection(BlockState state, boolean sideSolid, Direction direction, CallbackInfoReturnable<Boolean> cir)
	{
		if(state.getBlock() instanceof WindowBlock)
			cir.setReturnValue(true);
	}
}
