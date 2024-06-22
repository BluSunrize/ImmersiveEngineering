/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.coremods.client;

import blusunrize.immersiveengineering.client.utils.VertexBufferHolder;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin
{
	@Inject(method = "renderLevel", at = @At(
			value = "INVOKE_STRING",
			target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V",
			args = {"ldc=destroyProgress"}
	))
	public void afterTESRRender(
			DeltaTracker p_348530_, boolean p_109603_, Camera p_109604_, GameRenderer p_109605_, LightTexture p_109606_,
			Matrix4f p_254120_, Matrix4f p_323920_, CallbackInfo ci)
	{
		VertexBufferHolder.afterTERRendering();
	}
}
