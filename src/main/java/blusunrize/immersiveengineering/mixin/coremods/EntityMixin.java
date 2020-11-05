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
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class EntityMixin
{
	@Redirect(
			method = "doBlockCollisions",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/BlockState;onEntityCollision(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/entity/Entity;)V"
			)
	)
	public void onBlockCollision(BlockState abstractBlockState, World worldIn, BlockPos pos, Entity entityIn)
	{
		abstractBlockState.onEntityCollision(worldIn, pos, entityIn);
		WireCollisions.handleEntityCollision(pos, entityIn);
	}
}
