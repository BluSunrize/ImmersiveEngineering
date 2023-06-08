/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.Connection.CatenaryData;
import blusunrize.immersiveengineering.client.models.obj.callback.item.PowerpackCallbacks;
import blusunrize.immersiveengineering.client.render.ConnectionRenderer;
import blusunrize.immersiveengineering.client.render.tile.ShaderBannerRenderer;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;

import java.util.*;
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

	public static Map<UUID, Connection> PLAYER_ATTACHED_TO = new HashMap<>();

	private final static Cache<BannerKey, List<BannerLayer>> bannerCache = CacheBuilder.newBuilder()
			.maximumSize(100)
			.build();

	public static void render(
			LivingEntity toRender, ItemStack powerpack,
			EntityModel<?> baseModel, PoseStack matrixStackIn, MultiBufferSource buffers,
			int packedLightIn, int packedOverlayIn,
			float limbSwing, float limbSwingAmount, float ageInTicks, float partialTicks, float netHeadYaw, float headPitch
	)
	{
		matrixStackIn.pushPose();
//		ArmorModel model = MODEL.get();
//		model.setupAnim(toRender, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		boolean isCrouching = false;
		ModelPart leftArm = null;
		ModelPart rightArm = null;
		if(baseModel instanceof HumanoidModel humanoidModel)
		{
			isCrouching = humanoidModel.crouching;
			leftArm = humanoidModel.leftArm;
			rightArm = humanoidModel.rightArm;
		}
		else if(baseModel instanceof HierarchicalModel hierarchicalModel)
		{
			// Attempt to find limbs for illagers or similar
			try
			{
				leftArm = hierarchicalModel.root().getChild("left_arm");
				rightArm = hierarchicalModel.root().getChild("right_arm");
			} catch(NoSuchElementException ignored)
			{
			}
		}
		if(baseModel instanceof AgeableListModel ageable&&baseModel.young)
		{
			matrixStackIn.scale(.5f, .5f, .5f);
			matrixStackIn.translate(0, 1.5f, 0);
		}

		if(powerpack!=null)
		{
			float max = EnergyHelper.getMaxEnergyStored(powerpack);
			float storage = Math.max(0, EnergyHelper.getEnergyStored(powerpack)/max);
			//model.meterNeedle.zRot = 0.5235987f-(1.047197f*storage);
		}

		ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
		matrixStackIn.pushPose();
		matrixStackIn.mulPose(new Quaternionf().rotateXYZ((float)Math.PI, 0, 0));
		if(isCrouching)
		{
			matrixStackIn.translate(0, -.2f, 0);
			matrixStackIn.mulPose(new Quaternionf().rotateXYZ(0.5f, 0, 0));
		}
		matrixStackIn.translate(0, -.37, -.187);

		ItemStack banner = PowerpackItem.getBannerStatic(powerpack);
		if(!banner.isEmpty())
		{
			matrixStackIn.pushPose();

			ResourceLocation shaderTexture = ShaderBannerRenderer.getShaderResourceLocation(
					banner, new ResourceLocation(ImmersiveEngineering.MODID, "banner")
			);
			if(shaderTexture!=null)
			{
				// set up to render the large-texture banner
				PowerpackCallbacks.THIRD_PERSON_PASS = 3;
				BakedModel bakedModel = renderer.getModel(powerpack, toRender.level(), toRender, 0);
				bakedModel = ForgeHooksClient.handleCameraTransforms(matrixStackIn, bakedModel, ItemDisplayContext.FIXED, false);
				matrixStackIn.translate(-0.5D, -0.5D, -0.5D);
				VertexConsumer consumer = buffers.getBuffer(RenderType.entitySolid(shaderTexture));
				Minecraft.getInstance().getItemRenderer().renderModelLists(
						bakedModel, powerpack, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, matrixStackIn, consumer
				);
			}
			else
			{
				// set up to render the small-texture banner
				PowerpackCallbacks.THIRD_PERSON_PASS = 2;
				BakedModel bakedModel = renderer.getModel(powerpack, toRender.level(), toRender, 0);
				bakedModel = ForgeHooksClient.handleCameraTransforms(matrixStackIn, bakedModel, ItemDisplayContext.FIXED, false);
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
			}
			matrixStackIn.popPose();
			PowerpackCallbacks.THIRD_PERSON_PASS = 1;
		}
		Minecraft.getInstance().getItemRenderer().render(
				powerpack, ItemDisplayContext.FIXED, false,
				matrixStackIn, buffers,
				LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY,
				renderer.getModel(powerpack, toRender.level(), toRender, 0)
		);
		PowerpackCallbacks.THIRD_PERSON_PASS = 0;
		matrixStackIn.popPose();

		for(InteractionHand hand : InteractionHand.values())
		{
			ItemStack stack = toRender.getItemInHand(hand);
			boolean right = (hand==InteractionHand.MAIN_HAND)==(toRender.getMainArm()==HumanoidArm.RIGHT);
			ModelPart arm = right?rightArm: leftArm;
			if(!stack.isEmpty()&&EnergyHelper.isFluxRelated(stack)&&arm!=null)
			{
				int armHash = (int)(1024*arm.xRot)*31
						+(int)(1024*arm.yRot)*31
						+(int)(1024*arm.zRot)*31
						+(int)(arm.x*31)+(int)(arm.y*31)+(int)(arm.z*31)
						+(int)(arm.xScale*31)+(int)(arm.yScale*31)+(int)(arm.zScale*31)
						+(right?17*31: 0)+(isCrouching?11*31: 0);

				matrixStackIn.pushPose();
				matrixStackIn.scale(1, -1, 1);
				TransformingVertexBuilder builder = new TransformingVertexBuilder(
						buffers, RenderType.entitySolid(InventoryMenu.BLOCK_ATLAS), matrixStackIn
				);
				ConnectionRenderer.renderConnection(
						builder,
						CATENARY_DATA_CACHE.getUnchecked(new CatenaryKey(arm, isCrouching, right, armHash)),
						-.015625, 0xeda044,
						packedLightIn, packedOverlayIn
				);
				matrixStackIn.popPose();
			}
		}

		CompoundTag upgrades = PowerpackItem.getUpgradesStatic(powerpack);
		if(upgrades.getBoolean("antenna"))
		{
			Vec3 antennaBase = new Vec3(-.09375, -.3125, .21875).yRot((float)Math.toRadians(-toRender.yBodyRotO));
			Vec3 antennaTip = antennaBase.add(0, -2.5, 0);
			double distFromWire = 0;
			if(PLAYER_ATTACHED_TO.containsKey(toRender.getUUID()))
			{
				Connection connection = PLAYER_ATTACHED_TO.get(toRender.getUUID());
				CatenaryData catenary = connection.getCatenaryData();
				Vec3 a = Vec3.atLowerCornerOf(connection.getEndA().position());
				Vec3 ap = antennaTip.add(toRender.position()).subtract(a);
				Vec3 connectionDir = catenary.delta().normalize();
				distFromWire = ap.cross(connectionDir).length();
				double orthLength = ap.dot(catenary.delta())/catenary.delta().dot(catenary.delta());
				Vec3 catPoint = connection.getPoint(orthLength, connection.getEndA());
				antennaTip = toRender.position().add(0, 1.25, 0).subtract(a.add(catPoint)).scale(1.02);
			}
			matrixStackIn.pushPose();
			// undo player rotation to allow absolute positioning
			matrixStackIn.mulPose(new Quaternionf().rotationY(-toRender.yBodyRotO*Mth.DEG_TO_RAD));
			matrixStackIn.scale(-1.0F, 1.0F, 1.0F);
			CatenaryData renderCat = Connection.makeCatenaryData(antennaBase, antennaTip, 1.0+distFromWire*0.005);
			ConnectionRenderer.renderConnection(
					new TransformingVertexBuilder(buffers, RenderType.entitySolid(InventoryMenu.BLOCK_ATLAS), matrixStackIn),
					renderCat, -.03125, 0xa4afb0, packedLightIn, packedOverlayIn
			);
			matrixStackIn.popPose();
		}
		matrixStackIn.popPose();
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
		if(banner.getItem() instanceof BlockItem&&((BlockItem)banner.getItem()).getBlock() instanceof AbstractBannerBlock bannerBlock)
			baseCol = bannerBlock.getColor();
		ListTag patternList = BannerBlockEntity.getItemPatterns(banner);
		BannerKey key = new BannerKey(baseCol, patternList!=null?patternList.toString(): "");
		List<BannerLayer> cached = bannerCache.getIfPresent(key);
		if(cached!=null)
			return cached;

		List<Pair<Holder<BannerPattern>, DyeColor>> list = BannerBlockEntity.createPatterns(baseCol, patternList);
		List<BakedQuad> quads = bakedModel.getQuads(null, null, ApiUtils.RANDOM_SOURCE);
		cached = new ArrayList<>(quads.size()*list.size());
		for(int i = 0; i < 17&&i < list.size(); ++i)
		{
			Pair<Holder<BannerPattern>, DyeColor> pair = list.get(i);
			Holder<BannerPattern> bannerpattern = pair.getFirst();
			Material material = Sheets.getShieldMaterial(bannerpattern.unwrapKey().orElseThrow());
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

	private record CatenaryKey(ModelPart arm, boolean crouched, boolean right, int hash)
	{
		@Override
		public int hashCode()
		{
			return hash;
		}
	}

	public static final LoadingCache<CatenaryKey, CatenaryData> CATENARY_DATA_CACHE = CacheBuilder.newBuilder()
			.expireAfterAccess(10, TimeUnit.SECONDS)
			.build(CacheLoader.from(key -> {
				Vector4f vec4f = new Vector4f(key.right?-.0625f: .0265f, -.625f, 0, 1);
				Matrix4f mat4f = new Matrix4f();
				mat4f.setTranslation(key.arm.x/16f, -key.arm.y/16f, key.arm.z/16f);
				mat4f.rotate(new Quaternionf().rotationZ(-key.arm.zRot));
				mat4f.rotate(new Quaternionf().rotationY(key.arm.yRot));
				mat4f.rotate(new Quaternionf().rotationX(-key.arm.xRot));
				vec4f.mul(mat4f);
				vec4f.mul(key.arm.xScale, key.arm.yScale, key.arm.zScale, 1);
				double zFrom = key.crouched?.625: .25;
				double slack = key.crouched?1.25: 1.5;
				return Connection.makeCatenaryData(
						new Vec3(key.right?-.484375: .484375, -.75, zFrom),
						new Vec3(vec4f.x(), vec4f.y(), vec4f.z()),
						slack
				);
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
