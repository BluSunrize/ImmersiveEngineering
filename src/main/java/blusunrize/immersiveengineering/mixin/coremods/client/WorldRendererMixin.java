/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.mixin.coremods.client;

import blusunrize.immersiveengineering.client.utils.VertexBufferHolder;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.util.math.vector.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin
{
	@Inject(method = "updateCameraAndRender", at = @At(
			value = "INVOKE_STRING",
			target = "Lnet/minecraft/profiler/IProfiler;endStartSection(Ljava/lang/String;)V",
			args = {"ldc=destroyProgress"}
	))
	public void afterTESRRender(
			MatrixStack matrixStackIn, float partialTicks, long finishTimeNano, boolean drawBlockOutline,
			ActiveRenderInfo activeRenderInfoIn, GameRenderer gameRendererIn, LightTexture lightmapIn,
			Matrix4f projectionIn, CallbackInfo ci
	)
	{
		VertexBufferHolder.afterTERRendering();
	}
}
