/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.utils.client.ModelDataUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.DynamicSubmodelCallbacks;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TurretBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TurretGunBlockEntity;
import blusunrize.immersiveengineering.common.register.IEBlocks.BlockEntry;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TurretRenderer extends IEBlockEntityRenderer<TurretBlockEntity<?>>
{
	public static final Map<BlockEntry<?>, String> MODEL_NAME_BY_BLOCK = ImmutableMap.of(
			MetalDevices.TURRET_CHEM, "turret_chem",
			MetalDevices.TURRET_GUN, "turret_gun"
	);
	public static final Map<BlockEntry<?>, String> MODEL_FILE_BY_BLOCK = ImmutableMap.of(
			MetalDevices.TURRET_CHEM, "block/metal_device/chem_turret.obj.ie",
			MetalDevices.TURRET_GUN, "block/metal_device/gun_turret.obj.ie"
	);
	private static final Map<ResourceLocation, DynamicModel> MODELS_BY_BLOCK = new HashMap<>();

	@Override
	public void render(TurretBlockEntity<?> tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.getLevelNonnull().hasChunkAt(tile.getBlockPos()))
			return;

		BlockState state = tile.getBlockState();
		if(state.getBlock()!=MetalDevices.TURRET_CHEM.get()&&state.getBlock()!=MetalDevices.TURRET_GUN.get())
			return;
		BakedModel model = MODELS_BY_BLOCK.get(BuiltInRegistries.BLOCK.getKey(state.getBlock())).get();

		//Outer GL Wrapping, initial translation
		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);

		float defaultYaw = 180-tile.getFacing().toYRot();
		matrixStack.mulPose(new Quaternionf()
				.rotateY((tile.rotationYaw+defaultYaw) *Mth.DEG_TO_RAD)
				.rotateX(-tile.rotationPitch * Mth.DEG_TO_RAD)
		);

		renderModelPart(bufferIn, matrixStack, tile.getLevelNonnull(), state, model, tile.getBlockPos(), true, combinedLightIn, "gun");
		if(tile instanceof TurretGunBlockEntity gunTurret)
		{
			if(gunTurret.cycleRender > 0)
			{
				float cycle = 0;
				if(gunTurret.cycleRender > 3)
					cycle = (5-gunTurret.cycleRender)/2f;
				else
					cycle = gunTurret.cycleRender/3f;

				matrixStack.translate(0, 0, cycle*.3125);
			}
			renderModelPart(bufferIn, matrixStack, tile.getLevelNonnull(), state, model, tile.getBlockPos(), false, combinedLightIn, "action");
		}

		matrixStack.popPose();
	}

	public static void renderModelPart(MultiBufferSource buffer, PoseStack matrix, Level world, BlockState state,
									   BakedModel model, BlockPos pos, boolean isFirst, int light, String... parts)
	{
		pos = pos.above();

		VertexConsumer solidBuilder = buffer.getBuffer(RenderType.solid());
		matrix.pushPose();
		matrix.translate(-.5, 0, -.5);
		List<BakedQuad> quads = model.getQuads(
				state, null, ApiUtils.RANDOM_SOURCE,
				ModelDataUtils.single(DynamicSubmodelCallbacks.getProperty(), VisibilityList.show(parts)),
				RenderType.solid()
		);
		RenderUtils.renderModelTESRFancy(quads, solidBuilder, matrix, world, pos, !isFirst, -1, light);
		matrix.popPose();
	}

	public static void fillModels()
	{
		MODEL_NAME_BY_BLOCK.forEach((key, value) -> MODELS_BY_BLOCK.put(key.getId(), new DynamicModel(value)));
	}
}