/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import blusunrize.immersiveengineering.client.render.entity.IEModelLayers;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import com.google.common.base.Function;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
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
		ModelPart layer = models.bakeLayer(IEModelLayers.POWERPACK);
		return new ArmorModel(layer);
	});
	private static final ResourceLocation POWERPACK_TEXTURE = ImmersiveEngineering.rl("textures/models/powerpack.png");

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
			model.meterNeedle.zRot = 0.5235987f-(1.047197f*storage);
		}

		RenderType type = model.renderType(POWERPACK_TEXTURE);
		model.renderToBuffer(
				matrixStackIn, buffers.getBuffer(type), packedLightIn, packedOverlayIn, red, green, blue, alpha
		);

		TextureAtlasSprite wireTexture = Minecraft.getInstance()
				.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
				.apply(ImmersiveEngineering.rl("block/wire"));
		for(InteractionHand hand : InteractionHand.values())
		{
			ItemStack stack = toRender.getItemInHand(hand);
			if(!stack.isEmpty()&&EnergyHelper.isFluxRelated(stack))
			{
				boolean right = (hand==InteractionHand.MAIN_HAND)==(toRender.getMainArm()==HumanoidArm.RIGHT);
				float angleX = (right?model.rightArm: model.leftArm).xRot;
				float angleZ = (right?model.rightArm: model.leftArm).zRot;
				Vec3[] vex = (right?catenaryCacheRight: catenaryCacheLeft).getUnchecked(
						new CatenaryKey((int)(angleX*1024), (int)(angleZ*1024))
				);

				float vStep = 1f/vex.length;

				TransformingVertexBuilder builder = new TransformingVertexBuilder(
						buffers, RenderType.entitySolid(InventoryMenu.BLOCK_ATLAS), matrixStackIn
				);
				double scaleX = right?-1: 1;
				builder.defaultColor(.93f, .63f, .27f, 1);
				builder.setLight(packedLightIn);
				builder.setOverlay(packedOverlayIn);
				final float v0 = wireTexture.getV0();
				final float v1 = wireTexture.getV1();
				for(int i = 1; i < vex.length; i++)
					//TODO UVs or indices are probably twisted somewhere
					for(int offset = 0; offset < 2; ++offset)
					{
						int iHere = i-offset;
						int iThere = i-1+offset;
						Vec3 vecHere = vex[iHere];
						Vec3 vecThere = vex[iThere];
						builder.setNormal((float)(vecThere.z-vecHere.z), 0, (float)(vecHere.x-vecThere.x));
						for(int index : new int[]{iHere, iThere})
						{
							Vec3 vec = vex[index];
							double xA = scaleX*vec.x-.015625;
							double xB = scaleX*vec.x+.015625;
							if(index==iHere)
							{
								double tmp = xA;
								xA = xB;
								xB = tmp;
							}
							builder.vertex(xA, -vec.y, vec.z)
									.uv(wireTexture.getU(vStep*index), v0)
									.endVertex();
							builder.vertex(xB, -vec.y, vec.z)
									.uv(wireTexture.getU(vStep*index), v1)
									.endVertex();
						}
						builder.setNormal((float)(vecThere.y-vecHere.y), (float)(vecHere.x-vecThere.x), 0);
						for(int index : new int[]{iHere, iThere})
						{
							Vec3 vec = vex[index];
							double yA = -vec.y-.015625;
							double yB = -vec.y;
							if(index==iThere)
							{
								double tmp = yA;
								yA = yB;
								yB = tmp;
							}
							builder.vertex(scaleX*vec.x, yA, vec.z)
									.uv(wireTexture.getU(vStep*index), v0)
									.endVertex();
							builder.vertex(scaleX*vec.x, yB, vec.z)
									.uv(wireTexture.getU(vStep*index), v1)
									.endVertex();
						}
					}
			}
		}
	}

	private static record CatenaryKey(int xTimes1024, int zTimes1024)
	{
	}

	public static final LoadingCache<CatenaryKey, Vec3[]> catenaryCacheLeft = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build(CacheLoader.from(makeCacheCreator(false)));
	public static final LoadingCache<CatenaryKey, Vec3[]> catenaryCacheRight = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build(CacheLoader.from(makeCacheCreator(true)));

	// Using Guava caches, which still want Guava Functions
	@SuppressWarnings("Guava")
	private static Function<CatenaryKey, Vec3[]> makeCacheCreator(boolean right)
	{
		return key -> {
			double angleX = key.xTimes1024/1024.;
			double angleZ = key.zTimes1024/1024.;
			double armLength = .75f;
			double x = .3125+(right?1: -1)*armLength*Math.sin(angleZ);
			double y = armLength*Math.cos(angleX);
			double z = armLength*Math.sin(angleX);

			return WireUtils.getConnectionCatenary(new Vec3(.484375, -.75, .25), new Vec3(x, -y, z), 1.5);
		};
	}

	public static LayerDefinition createLayers()
	{
		MeshDefinition data = new MeshDefinition();
		PartDefinition root = data.getRoot();
		root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);

		// TODO better names!
		body.addOrReplaceChild(
				"part0",
				CubeListBuilder.create().texOffs(40, 0).addBox(-4f, -5f, -2f, 8, 10, 3),
				PartPose.offset(0, 5, 4)
		);
		PartDefinition part1 = body.addOrReplaceChild(
				"part1",
				CubeListBuilder.create().texOffs(12, 0).addBox(-3f, -2f, -2f, 6, 4, 4),
				PartPose.offset(0, 12, 4f)
		);
		body.addOrReplaceChild(
				"part2",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1f, -4f, -1f, 2, 8, 2),
				PartPose.offset(-5f, 5, 3f)
		);
		body.addOrReplaceChild(
				"part3",
				CubeListBuilder.create().texOffs(0, 0).addBox(-1f, -4.0f, -1f, 2, 8, 2),
				PartPose.offsetAndRotation(5f, 5, 3f, 0, 0, (float)Math.PI)
		);

		body.addOrReplaceChild(
				"gauge1",
				CubeListBuilder.create().texOffs(40, 13).addBox(-.5f, -1.5f, -.5f, 1, 3, 1),
				PartPose.offset(-3f, 5.5f, 5f)
		);
		body.addOrReplaceChild(
				"gauge2",
				CubeListBuilder.create().texOffs(40, 13).addBox(-.5f, -1.5f, -.5f, 1, 3, 1),
				PartPose.offset(1f, 5.5f, 5f)
		);
		body.addOrReplaceChild(
				"gauge3",
				CubeListBuilder.create().texOffs(44, 13).addBox(-2f, -.5f, -.5f, 3, 1, 1),
				PartPose.offset(-.5f, 3.5f, 5f)
		);
		body.addOrReplaceChild(
				"gauge4",
				CubeListBuilder.create().texOffs(44, 13).addBox(-2f, -.5f, -.5f, 3, 1, 1),
				PartPose.offset(-.5f, 7.5f, 5f)
		);

		body.addOrReplaceChild(
				"meterNeedle",
				CubeListBuilder.create().texOffs(52, 14).addBox(-.5f, -3.5f, -.5f, 1, 4, 1, new CubeDeformation(-.25f)),
				PartPose.offsetAndRotation(-1f, 7.625f, 5f, 0, 0, Mth.PI/4)
		);

		PartDefinition connector = part1.addOrReplaceChild("connector", CubeListBuilder.create(), PartPose.ZERO);
		connector.addOrReplaceChild(
				"part1",
				CubeListBuilder.create().texOffs(17, 9).addBox(-1f, -1.5f, -1.5f, 3, 3, 3),
				PartPose.offset(-4.5f, 0f, 0f)
		);
		connector.addOrReplaceChild(
				"part2",
				CubeListBuilder.create().texOffs(17, 9).addBox(-1f, -1.5f, -1.5f, 3, 3, 3, new CubeDeformation(-.375f)),
				PartPose.offset(-6f, 0f, 0)
		);
		connector.addOrReplaceChild(
				"part3",
				CubeListBuilder.create().texOffs(29, 9).addBox(-1f, -1.5f, -1.5f, 1, 3, 3),
				PartPose.offset(-6.25f, 0f, 0)
		);
		connector.addOrReplaceChild(
				"part4",
				CubeListBuilder.create().texOffs(12, 8).addBox(-.5f, -1f, -1f, 2, 2, 2),
				PartPose.offset(-7.5f, 0f, 0f)
		);
		connector.addOrReplaceChild(
				"part5",
				CubeListBuilder.create().texOffs(17, 9).addBox(-1f, -1.5f, -1.5f, 3, 3, 3),
				PartPose.offsetAndRotation(4.5f, 0f, 0f, 0, Mth.PI, 0)
		);
		connector.addOrReplaceChild(
				"part6",
				CubeListBuilder.create().texOffs(17, 9).addBox(-1f, -1.5f, -1.5f, 3, 3, 3, new CubeDeformation(-.375f)),
				PartPose.offsetAndRotation(6f, 0f, 0, 0, Mth.PI, 0)
		);
		connector.addOrReplaceChild(
				"part7",
				CubeListBuilder.create().texOffs(29, 9).addBox(-1f, -1.5f, -1.5f, 1, 3, 3),
				PartPose.offsetAndRotation(6.25f, 0f, 0, 0, Mth.PI, 0)
		);
		connector.addOrReplaceChild(
				"part8",
				CubeListBuilder.create().texOffs(12, 8).addBox(-.5f, -1f, -1f, 2, 2, 2),
				PartPose.offsetAndRotation(7.5f, 0f, 0f, 0, Mth.PI, 0)
		);

		for(int i = 0; i < 3; i++)
		{
			float pos = 3.125f-i*2.25f;
			PartDefinition tube = body.addOrReplaceChild(
					"tube"+i,
					CubeListBuilder.create().texOffs(56, 19).addBox(-1f, -1f, -1f, 2, 2, 2, new CubeDeformation(-.25f)),
					PartPose.offsetAndRotation(pos, 1, 5, -Mth.PI/4, 0, 0)
			);

			tube.addOrReplaceChild(
					"part1",
					CubeListBuilder.create().texOffs(52, 18).addBox(-.5f, -2f, -.5f, 1, 3, 1),
					PartPose.offset(0, -.5f, 0)
			);
			tube.addOrReplaceChild(
					"part2",
					CubeListBuilder.create().texOffs(56, 15).addBox(-1f, -2f, -1f, 2, 2, 2),
					PartPose.offset(0, -.75f, 0)
			);
			tube.addOrReplaceChild(
					"part3",
					CubeListBuilder.create().texOffs(56, 13).addBox(-.5f, -1f, -.5f, 1, 1, 1),
					PartPose.offset(0, -2.25f, 0)
			);
			tube.addOrReplaceChild(
					"part4",
					CubeListBuilder.create().texOffs(56, 13).addBox(-.5f, -1f, -.5f, 1, 1, 1, new CubeDeformation(-.25f)),
					PartPose.offset(0, -3f, 0)
			);
		}
		return LayerDefinition.create(data, 64, 32);
	}

	private static class ArmorModel extends ModelIEArmorBase
	{
		private final ModelPart meterNeedle;

		public ArmorModel(ModelPart part)
		{
			super(part, RenderType::entityTranslucent);
			this.meterNeedle = part.getChild("body").getChild("meterNeedle");
		}
	}
}
