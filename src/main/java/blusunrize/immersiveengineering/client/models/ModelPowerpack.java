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
import blusunrize.immersiveengineering.client.models.obj.callback.item.PowerpackCallbacks;
import blusunrize.immersiveengineering.client.render.ConnectionRenderer;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.items.PowerpackItem;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import com.google.common.base.Suppliers;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Quaternion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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

	private final static Cache<BannerKey, List<BannerLayer>> bannerCache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.build();

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
		matrixStackIn.pushPose();
		matrixStackIn.mulPose(new Quaternion(180, 0, 0, true));
		if(model.crouching)
		{
			matrixStackIn.translate(0, -.2f, 0);
			matrixStackIn.mulPose(new Quaternion(0.5f, 0, 0, false));
		}
		matrixStackIn.translate(0, -.37, -.187);

		ItemStack banner = PowerpackItem.getBannerStatic(powerpack);
		if(!banner.isEmpty())
		{
			matrixStackIn.pushPose();
			// set up to render the cloth
			PowerpackCallbacks.THIRD_PERSON_PASS = 2;

			BakedModel bakedModel = renderer.getModel(powerpack, toRender.getLevel(), toRender, 0);
			bakedModel = ForgeHooksClient.handleCameraTransforms(matrixStackIn, bakedModel, TransformType.FIXED, false);
			matrixStackIn.translate(-0.5D, -0.5D, -0.5D);
			for(BannerLayer layer : getBannerLayers(banner, bakedModel))
			{
				VertexConsumer consumer = layer.getConsumer.apply(buffers);
				for(BakedQuad quad : layer.bakedQuads())
					consumer.putBulkData(
							matrixStackIn.last(), quad, layer.red(), layer.green(), layer.blue(), 1,
							LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, false
					);
			}
			matrixStackIn.popPose();
			PowerpackCallbacks.THIRD_PERSON_PASS = 1;
		}
		Minecraft.getInstance().getItemRenderer().render(
				powerpack, TransformType.FIXED, false,
				matrixStackIn, buffers,
				LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
				renderer.getModel(powerpack, toRender.getLevel(), toRender, 0)
		);
		PowerpackCallbacks.THIRD_PERSON_PASS = 0;
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

	private record BannerKey(DyeColor base, String patternText)
	{
	}

	private record BannerLayer(Function<MultiBufferSource, VertexConsumer> getConsumer,
							   float red, float green, float blue, List<BakedQuad> bakedQuads)
	{
	}

	private static List<BannerLayer> getBannerLayers(ItemStack banner, BakedModel bakedModel)
	{
		DyeColor baseCol = DyeColor.WHITE;
		if(banner.getItem() instanceof BlockItem && ((BlockItem)banner.getItem()).getBlock()instanceof AbstractBannerBlock bannerBlock)
			baseCol = bannerBlock.getColor();
		ListTag patternList = BannerBlockEntity.getItemPatterns(banner);
		BannerKey key = new BannerKey(baseCol, patternList!=null?patternList.toString(): "");
		List<BannerLayer> cached = bannerCache.getIfPresent(key);
		if(cached!=null)
			return cached;

		List<Pair<BannerPattern, DyeColor>> list = BannerBlockEntity.createPatterns(baseCol, patternList);
		List<BakedQuad> quads = bakedModel.getQuads(null, null, new Random(42));
		cached = new ArrayList<>(quads.size()*list.size());
		for(int i = 0; i < 17&&i < list.size(); ++i)
		{
			Pair<BannerPattern, DyeColor> pair = list.get(i);
			BannerPattern bannerpattern = pair.getFirst();
			Material material = Sheets.getShieldMaterial(bannerpattern);
			float[] colour = pair.getSecond().getTextureDiffuseColors();
			cached.add(new BannerLayer(
					mbs -> material.buffer(mbs, RenderType::entityCutoutNoCullZOffset),
					colour[0], colour[1], colour[2],
					quads
			));
		}
		bannerCache.put(key, cached);
		return cached;
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
