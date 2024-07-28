/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.coremods.client;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class TempMixin
{
	@Redirect(
			method = "loadBlurEffect",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getHeight()I")
	)
	private int redirectGetHeight(Window w)
	{
		return 400;
	}

	@Redirect(
			method = "loadBlurEffect",
			at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;getWidth()I")
	)
	private int redirectGetWidth(Window w)
	{
		return 600;
	}
}
