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
import blusunrize.immersiveengineering.api.IEApi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class DynamicModel
{
	private static final List<ModelResourceLocation> MODELS = new ArrayList<>();

	@SubscribeEvent
	public static void registerModels(ModelEvent.RegisterAdditional ev)
	{
		for(ModelResourceLocation model : MODELS)
			// TODO check if this works
			ev.register(model);
	}

	private final ModelResourceLocation name;

	public DynamicModel(String desc)
	{
		// TODO does this work?
		this.name = new ModelResourceLocation(IEApi.ieLoc("dynamic/"+desc), "");
		MODELS.add(this.name);
	}

	public BakedModel get()
	{
		final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		return blockRenderer.getBlockModelShaper().getModelManager().getModel(name);
	}

	public List<BakedQuad> getNullQuads()
	{
		return getNullQuads(ModelData.EMPTY);
	}

	public List<BakedQuad> getNullQuads(ModelData data)
	{
		return get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, data, null);
	}

	public ResourceLocation getName()
	{
		return name.id();
	}
}
