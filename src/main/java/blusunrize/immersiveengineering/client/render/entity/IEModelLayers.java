package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.models.ModelEarmuffs;
import blusunrize.immersiveengineering.client.models.ModelPowerpack;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraftforge.client.ForgeHooksClient;

public class IEModelLayers
{
	public static final ModelLayerLocation BARREL_MINECART = new ModelLayerLocation(IEEntityTypes.BARREL_MINECART.getId(), "main");
	public static final ModelLayerLocation CRATE_MINECART = new ModelLayerLocation(IEEntityTypes.CRATE_MINECART.getId(), "main");
	public static final ModelLayerLocation REINFORCED_CRATE_CART = new ModelLayerLocation(IEEntityTypes.REINFORCED_CRATE_CART.getId(), "main");
	public static final ModelLayerLocation METAL_BARREL_CART = new ModelLayerLocation(IEEntityTypes.METAL_BARREL_CART.getId(), "main");
	public static final ModelLayerLocation POWERPACK = new ModelLayerLocation(ImmersiveEngineering.rl("powerpack"), "main");
	public static final ModelLayerLocation EARMUFFS = new ModelLayerLocation(ImmersiveEngineering.rl("earmuffs"), "main");

	public static void registerDefinitions()
	{
		ForgeHooksClient.registerLayerDefinition(BARREL_MINECART, MinecartModel::createBodyLayer);
		ForgeHooksClient.registerLayerDefinition(CRATE_MINECART, MinecartModel::createBodyLayer);
		ForgeHooksClient.registerLayerDefinition(REINFORCED_CRATE_CART, MinecartModel::createBodyLayer);
		ForgeHooksClient.registerLayerDefinition(METAL_BARREL_CART, MinecartModel::createBodyLayer);
		ForgeHooksClient.registerLayerDefinition(POWERPACK, ModelPowerpack::createLayers);
		ForgeHooksClient.registerLayerDefinition(EARMUFFS, ModelEarmuffs::createLayers);
	}
}
