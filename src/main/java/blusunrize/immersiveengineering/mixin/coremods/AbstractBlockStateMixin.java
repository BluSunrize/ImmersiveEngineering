/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.coremods;

import blusunrize.immersiveengineering.common.wires.WireCollisions;
import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin
{
	@Inject(method = "onEntityCollision", at = @At(value = "HEAD"))
	public void onBlockCollision(World worldIn, BlockPos pos, Entity entityIn, CallbackInfo ci)
	{
		WireCollisions.handleEntityCollision(pos, entityIn);
	}
}
