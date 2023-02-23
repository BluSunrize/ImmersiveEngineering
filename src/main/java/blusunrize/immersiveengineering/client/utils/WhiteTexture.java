/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.function.Supplier;

import static com.mojang.blaze3d.platform.GlConst.*;

public class WhiteTexture implements AutoCloseable
{
	//TODO does this work across resource reloads?
	public static final Supplier<WhiteTexture> INSTANCE = Suppliers.memoize(WhiteTexture::new);

	private final DynamicTexture whiteTexture;
	private final ResourceLocation whiteTextureLocation;

	private WhiteTexture()
	{
		this.whiteTexture = new DynamicTexture(16, 16, false);
		this.whiteTextureLocation = Minecraft.getInstance().getTextureManager().register("ie_light_map", this.whiteTexture);
		NativeImage lightPixels = Objects.requireNonNull(this.whiteTexture.getPixels());

		for(int i = 0; i < 16; ++i)
		{
			for(int j = 0; j < 16; ++j)
			{
				lightPixels.setPixelRGBA(j, i, -1);
			}
		}

		this.whiteTexture.upload();
	}

	public void bind()
	{
		RenderSystem.setShaderTexture(2, this.whiteTextureLocation);
		Minecraft.getInstance().getTextureManager().bindForSetup(this.whiteTextureLocation);
		RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		RenderSystem.texParameter(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
	}

	public ResourceLocation getTextureLocation()
	{
		return whiteTextureLocation;
	}

	@Override
	public void close() throws Exception
	{
		whiteTexture.close();
	}
}
