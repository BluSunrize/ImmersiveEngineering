/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.accessors;

import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LiquidBlock.class)
public abstract class LiquidBlockFix
{
	@Redirect(
			method = {
					"getCollisionShape",
					"isPathfindable",
					"skipRendering",
					"onPlace",
					"updateShape",
					"neighborChanged",
					"shouldSpreadLiquid",
					"pickupBlock",
					"getPickupSound"
			},
			at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/LiquidBlock;fluid:Lnet/minecraft/world/level/material/FlowingFluid;")
	)
	private FlowingFluid useGetter(LiquidBlock this2)
	{
		return this2.getFluid();
	}
}
