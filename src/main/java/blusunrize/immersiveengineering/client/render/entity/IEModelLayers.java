/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.models.ModelEarmuffs;
import blusunrize.immersiveengineering.client.models.ModelGlider;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterLayerDefinitions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Lib.MODID, bus = Bus.MOD, value = Dist.CLIENT)
public class IEModelLayers
{
	public static final ModelLayerLocation BARREL_MINECART = new ModelLayerLocation(IEEntityTypes.BARREL_MINECART.getId(), "main");
	public static final ModelLayerLocation CRATE_MINECART = new ModelLayerLocation(IEEntityTypes.CRATE_MINECART.getId(), "main");
	public static final ModelLayerLocation REINFORCED_CRATE_CART = new ModelLayerLocation(IEEntityTypes.REINFORCED_CRATE_CART.getId(), "main");
	public static final ModelLayerLocation METAL_BARREL_CART = new ModelLayerLocation(IEEntityTypes.METAL_BARREL_CART.getId(), "main");
	public static final ModelLayerLocation EARMUFFS = new ModelLayerLocation(ImmersiveEngineering.rl("earmuffs"), "main");
	public static final ModelLayerLocation GLIDER = new ModelLayerLocation(ImmersiveEngineering.rl("glider"), "main");

	@SubscribeEvent
	public static void registerDefinitions(RegisterLayerDefinitions ev)
	{
		ev.registerLayerDefinition(BARREL_MINECART, MinecartModel::createBodyLayer);
		ev.registerLayerDefinition(CRATE_MINECART, MinecartModel::createBodyLayer);
		ev.registerLayerDefinition(REINFORCED_CRATE_CART, MinecartModel::createBodyLayer);
		ev.registerLayerDefinition(METAL_BARREL_CART, MinecartModel::createBodyLayer);
		ev.registerLayerDefinition(EARMUFFS, ModelEarmuffs::createLayers);
		ev.registerLayerDefinition(GLIDER, ModelGlider::createLayers);
	}
}
