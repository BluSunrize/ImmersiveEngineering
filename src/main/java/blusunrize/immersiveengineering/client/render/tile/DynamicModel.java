/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.ForgeModelBakery;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import java.util.List;

public class DynamicModel
{
	private final ResourceLocation name;

	public DynamicModel(String desc)
	{
		this.name = new ResourceLocation(ImmersiveEngineering.MODID, "dynamic/"+desc);
		ForgeModelBakery.addSpecialModel(this.name);
	}

	public BakedModel get()
	{
		final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		return blockRenderer.getBlockModelShaper().getModelManager().getModel(name);
	}

	public List<BakedQuad> getNullQuads()
	{
		return getNullQuads(EmptyModelData.INSTANCE);
	}

	public List<BakedQuad> getNullQuads(IModelData data)
	{
		return get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, data);
	}

	public ResourceLocation getName()
	{
		return name;
	}
}
