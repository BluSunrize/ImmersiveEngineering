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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = ImmersiveEngineering.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class DynamicModel
{
	private static final List<ResourceLocation> MODELS = new ArrayList<>();

	@SubscribeEvent
	public static void registerModels(ModelEvent.RegisterAdditional ev)
	{
		for(ResourceLocation model : MODELS)
			ev.register(model);
	}

	private final ResourceLocation name;

	public DynamicModel(String desc)
	{
		this.name = new ResourceLocation(ImmersiveEngineering.MODID, "dynamic/"+desc);
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
		return name;
	}
}
