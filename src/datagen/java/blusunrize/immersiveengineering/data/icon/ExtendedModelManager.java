/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.icon;


import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.InactiveProfiler;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.Collection;
import java.util.Map;

public class ExtendedModelManager extends ModelManager
{
	private AtlasSet spriteMap = null;

	ExtendedModelManager(
			final TextureManager textureManagerIn,
			final BlockColors blockColorsIn,
			final int maxMipmapLevelIn)
	{
		super(textureManagerIn, blockColorsIn, maxMipmapLevelIn);
	}

	void loadModels()
	{
		final ModelBakery modelBakery = this.prepare(
				Minecraft.getInstance().getResourceManager(), InactiveProfiler.INSTANCE
		);

		this.apply(modelBakery, Minecraft.getInstance().getResourceManager(), InactiveProfiler.INSTANCE);

		this.spriteMap = modelBakery.getSpriteMap();

		Minecraft.getInstance().getItemRenderer().getItemModelShaper().rebuildCache();
	}

	@SuppressWarnings("unchecked")
	public Collection<ResourceLocation> getTextureMap()
	{
		if(spriteMap==null)
			throw new IllegalStateException("SpriteMap not initialized.");

		final Map<ResourceLocation, TextureAtlas> textureMap = ObfuscationReflectionHelper.getPrivateValue(
				AtlasSet.class, spriteMap, "atlases"
		);
		return textureMap.keySet();
	}
}
