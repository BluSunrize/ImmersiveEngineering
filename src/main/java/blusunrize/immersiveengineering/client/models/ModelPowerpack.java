/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.Connection.CatenaryData;
import blusunrize.immersiveengineering.client.render.ConnectionRenderer;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author BluSunrize
 * @since 15.06.2017
 */
public class ModelPowerpack
{
	private static final Supplier<ArmorModel> MODEL = Suppliers.memoize(() -> {
		EntityModelSet models = Minecraft.getInstance().getEntityModels();
		ModelPart layer = models.bakeLayer(ModelLayers.PLAYER);
		return new ArmorModel(layer);
	});

	public static void render(
			LivingEntity toRender, ItemStack powerpack,
			PoseStack matrixStackIn, MultiBufferSource buffers,
			int packedLightIn, int packedOverlayIn,
			float red, float green, float blue, float alpha,
			float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch
	)
	{
		ArmorModel model = MODEL.get();
		model.setupAnim(toRender, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		if(powerpack!=null)
		{
			float max = EnergyHelper.getMaxEnergyStored(powerpack);
			float storage = Math.max(0, EnergyHelper.getEnergyStored(powerpack)/max);
			//model.meterNeedle.zRot = 0.5235987f-(1.047197f*storage);
		}

		ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
		BakedModel bakedModel = renderer.getModel(powerpack, toRender.getLevel(), toRender, 0);
		matrixStackIn.pushPose();
		matrixStackIn.mulPose(new Quaternion(180, 0, 0, true));
		if(model.crouching)
		{
			matrixStackIn.translate(0, -.2f, 0);
			matrixStackIn.mulPose(new Quaternion(0.5f, 0, 0, false));
		}
		matrixStackIn.translate(0, -.37, -.187);

		Minecraft.getInstance().getItemRenderer().render(
				powerpack, TransformType.FIXED, false,
				matrixStackIn, buffers,
				LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, bakedModel
		);
		matrixStackIn.popPose();

		for(InteractionHand hand : InteractionHand.values())
		{
			ItemStack stack = toRender.getItemInHand(hand);
			if(!stack.isEmpty()&&EnergyHelper.isFluxRelated(stack))
			{
				boolean right = (hand==InteractionHand.MAIN_HAND)==(toRender.getMainArm()==HumanoidArm.RIGHT);
				float angleX = (right?model.rightArm: model.leftArm).xRot;
				float angleZ = (right?model.rightArm: model.leftArm).zRot;

				matrixStackIn.pushPose();
				matrixStackIn.scale(right?-1: 1, -1, 1);
				TransformingVertexBuilder builder = new TransformingVertexBuilder(
						buffers, RenderType.entitySolid(InventoryMenu.BLOCK_ATLAS), matrixStackIn
				);
				ConnectionRenderer.renderConnection(
						builder,
						CATENARY_DATA_CACHE.getUnchecked(
								new CatenaryKey((int)(1024*angleX), (int)(1024*angleZ), model.crouching, right)
						),
						-.015625, 0xeda044,
						packedLightIn, packedOverlayIn
				);
				matrixStackIn.popPose();
			}
		}
	}

	private record CatenaryKey(int xTimes1024, int zTimes1024, boolean crouched, boolean right)
	{
	}

	public static final LoadingCache<CatenaryKey, CatenaryData> CATENARY_DATA_CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(10, TimeUnit.SECONDS)
			.build(CacheLoader.from(key -> {
				double angleX = key.xTimes1024()/1024.;
				double angleZ = key.zTimes1024()/1024.;
				double armLength = .75f;
				double x = .3125+(key.right?1: -1)*armLength*Math.sin(angleZ);
				double y = armLength*Math.cos(angleX);
				double z = armLength*Math.sin(angleX);
				double zFrom = key.crouched?.625: .25;
				double slack = key.crouched?1.25: 1.5;
				return Connection.makeCatenaryData(new Vec3(.484375, -.75, zFrom), new Vec3(x, -y, z), slack);
			}));


	private static class ArmorModel extends ModelIEArmorBase
	{
		//private final ModelPart meterNeedle;

		public ArmorModel(ModelPart part)
		{
			super(part, RenderType::entityTranslucent);
			//this.meterNeedle = part.getChild("body").getChild("meterNeedle");
		}
	}
}
