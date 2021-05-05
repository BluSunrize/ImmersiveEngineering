/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.coremods.client;

import blusunrize.immersiveengineering.client.render.IEBipedRotations;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedModel.class)
public class BipedModelMixin<T extends LivingEntity>
{
	@Inject(method = "setRotationAngles", at = @At("RETURN"))
	public void rotationAngleCallback(
			T entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch,
			CallbackInfo info
	)
	{
		//noinspection ConstantConditions (IntelliJ warns about the (admittedly crazy-looking) cast)
		IEBipedRotations.handleBipedRotations((BipedModel<?>)(Object)this, entityIn);
	}
}
