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
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.mixin.accessors.client.ModelAccess;
import com.google.common.base.Function;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author BluSunrize
 * @since 15.06.2017
 */
public class ModelPowerpack
{
	private static final Supplier<ArmorModel> MODEL = Suppliers.memoize(ArmorModel::new);
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
			model.modelParts[7].zRot = 0.5235987f-(1.047197f*storage);
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
						buffers.getBuffer(RenderType.entitySolid(InventoryMenu.BLOCK_ATLAS)), matrixStackIn
				);
				double scaleX = right?-1: 1;
				builder.setColor(.93f, .63f, .27f, 1);
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

	private static class CatenaryKey
	{
		private final int xTimes1024;
		private final int zTimes1024;

		private CatenaryKey(int xTimes1024, int zTimes1024)
		{
			this.xTimes1024 = xTimes1024;
			this.zTimes1024 = zTimes1024;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			CatenaryKey that = (CatenaryKey)o;
			return xTimes1024==that.xTimes1024&&zTimes1024==that.zTimes1024;
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(xTimes1024, zTimes1024);
		}
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

	private static class ArmorModel extends ModelIEArmorBase
	{
		public ModelPart[] modelParts;
		public ModelPart[] colouredParts;

		public ArmorModel()
		{
			super(.0625f, 0, 64, 32);
			((ModelAccess)this).setRenderType(RenderType::entityTranslucent);
			modelParts = new ModelPart[8];
			colouredParts = new ModelPart[4];

			this.modelParts[0] = new ModelPart(this, 40, 0);
			this.modelParts[0].addBox(-4f, -5f, -2f, 8, 10, 3, 0);
			this.modelParts[0].setPos(0, 5, 4);
			this.body.addChild(modelParts[0]);

			this.modelParts[1] = new ModelPart(this, 12, 0);
			this.modelParts[1].addBox(-3f, -2f, -2f, 6, 4, 4, 0);
			this.modelParts[1].setPos(0, 12, 4f);
			this.body.addChild(modelParts[1]);

			this.modelParts[2] = new ModelPart(this, 0, 0);
			this.modelParts[2].addBox(-1f, -4f, -1f, 2, 8, 2, 0);
			this.modelParts[2].setPos(-5f, 5, 3f);
			this.body.addChild(modelParts[2]);

			this.modelParts[3] = new ModelPart(this, 0, 0);
			this.modelParts[3].addBox(-1f, -4.0f, -1f, 2, 8, 2, 0);
			this.modelParts[3].setPos(5f, 5, 3f);
			this.modelParts[3].zRot = 3.14159f;
			this.body.addChild(modelParts[3]);


			ModelPart gauge = new ModelPart(this, 40, 13);
			gauge.addBox(-.5f, -1.5f, -.5f, 1, 3, 1, 0);
			gauge.setPos(-3f, 5.5f, 5f);
			this.body.addChild(gauge);

			gauge = new ModelPart(this, 40, 13);
			gauge.addBox(-.5f, -1.5f, -.5f, 1, 3, 1, 0);
			gauge.setPos(1f, 5.5f, 5f);
			this.body.addChild(gauge);

			gauge = new ModelPart(this, 44, 13);
			gauge.addBox(-2f, -.5f, -.5f, 3, 1, 1, 0);
			gauge.setPos(-.5f, 3.5f, 5f);
			this.body.addChild(gauge);

			gauge = new ModelPart(this, 44, 13);
			gauge.addBox(-2f, -.5f, -.5f, 3, 1, 1, 0);
			gauge.setPos(-.5f, 7.5f, 5f);
			this.body.addChild(gauge);

			this.modelParts[7] = new ModelPart(this, 52, 14);
			this.modelParts[7].addBox(-.5f, -3.5f, -.5f, 1, 4, 1, -.25f);
			this.modelParts[7].setPos(-1f, 7.625f, 5f);
			this.modelParts[7].zRot = 0.7853975f;
			this.body.addChild(modelParts[7]);

			ModelPart connector = new ModelPart(this, 17, 9);
			connector.addBox(-1f, -1.5f, -1.5f, 3, 3, 3, 0);
			connector.setPos(-4.5f, 0f, 0f);
			this.modelParts[1].addChild(connector);

			connector = new ModelPart(this, 17, 9);
			connector.addBox(-1f, -1.5f, -1.5f, 3, 3, 3, -.375f);
			connector.setPos(-6f, 0f, 0);
			this.modelParts[1].addChild(connector);

			connector = new ModelPart(this, 29, 9);
			connector.addBox(-1f, -1.5f, -1.5f, 1, 3, 3, 0);
			connector.setPos(-6.25f, 0f, 0);
			this.modelParts[1].addChild(connector);

			connector = new ModelPart(this, 12, 8);
			connector.addBox(-.5f, -1f, -1f, 2, 2, 2, 0);
			connector.setPos(-7.5f, 0f, 0f);
			this.modelParts[1].addChild(connector);


			connector = new ModelPart(this, 17, 9);
			connector.addBox(-1f, -1.5f, -1.5f, 3, 3, 3, 0);
			connector.setPos(4.5f, 0f, 0f);
			connector.yRot = 3.14159f;
			this.modelParts[1].addChild(connector);

			connector = new ModelPart(this, 17, 9);
			connector.addBox(-1f, -1.5f, -1.5f, 3, 3, 3, -.375f);
			connector.setPos(6f, 0f, 0);
			connector.yRot = 3.14159f;
			this.modelParts[1].addChild(connector);

			connector = new ModelPart(this, 29, 9);
			connector.addBox(-1f, -1.5f, -1.5f, 1, 3, 3, 0);
			connector.setPos(6.25f, 0f, 0);
			connector.yRot = 3.14159f;
			this.modelParts[1].addChild(connector);

			connector = new ModelPart(this, 12, 8);
			connector.addBox(-.5f, -1f, -1f, 2, 2, 2, 0);
			connector.setPos(7.5f, 0f, 0f);
			connector.yRot = 3.14159f;
			this.modelParts[1].addChild(connector);

			for(int i = 0; i < 3; i++)
			{
				float pos = 3.125f-i*2.25f;
				ModelPart tube = new ModelPart(this, 56, 19);
				tube.addBox(-1f, -1f, -1f, 2, 2, 2, -.25f);
				tube.setPos(pos, 1, 5);
				tube.xRot = (float)Math.toRadians(-45);
				this.body.addChild(tube);

				ModelPart tube2 = new ModelPart(this, 52, 18);
				tube2.addBox(-.5f, -2f, -.5f, 1, 3, 1, 0);
				tube2.setPos(0, -.5f, 0);
				tube.addChild(tube2);

				tube2 = new ModelPart(this, 56, 15);
				tube2.addBox(-1f, -2f, -1f, 2, 2, 2, 0);
				tube2.setPos(0, -.75f, 0);
				tube.addChild(tube2);

				tube2 = new ModelPart(this, 56, 13);
				tube2.addBox(-.5f, -1f, -.5f, 1, 1, 1, 0);
				tube2.setPos(0, -2.25f, 0);
				tube.addChild(tube2);

				tube2 = new ModelPart(this, 56, 13);
				tube2.addBox(-.5f, -1f, -.5f, 1, 1, 1, -.25f);
				tube2.setPos(0, -3f, 0);
				tube.addChild(tube2);
			}

			this.head.visible = false;
			this.hat.visible = false;
			this.leftArm.visible = false;
			this.rightArm.visible = false;
			this.leftLeg.visible = false;
			this.rightLeg.visible = false;
		}
	}
}
