/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.coremods;

import blusunrize.immersiveengineering.common.entities.RailgunShotEntity;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.neoforged.neoforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Breeze.class)
public class BreezeDeflectionMixin
{
	@Inject(method = "deflection", at = @At("HEAD"), cancellable = true)
	protected void deflection(Projectile projectile, CallbackInfoReturnable<ProjectileDeflection> callback)
	{
		if(projectile instanceof RailgunShotEntity railgunShot&&railgunShot.getAmmo().is(Tags.Items.RODS_BREEZE))
			callback.setReturnValue(ProjectileDeflection.NONE);
	}
}