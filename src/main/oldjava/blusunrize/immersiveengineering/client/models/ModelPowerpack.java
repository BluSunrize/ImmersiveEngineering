/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import blusunrize.immersiveengineering.client.render.IEBipedLayerRenderer;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.items.PowerpackItem;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.mixin.accessors.client.ModelAccess;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * @author BluSunrize
 * @since 15.06.2017
 */
public class ModelPowerpack<T extends LivingEntity> extends ModelIEArmorBase<T>
{
	public ModelPart[] modelParts;
	public ModelPart[] colouredParts;

	public ModelPowerpack(float modelSize, float yOffsetIn, int textureWidthIn, int textureHeightIn)
	{
		super(modelSize, yOffsetIn, textureWidthIn, textureHeightIn);
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

	@Override
	public void renderToBuffer(PoseStack matrixStackIn, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
	{
		if(entityTemp instanceof LivingEntity)
		{
			ItemStack chest = entityTemp.getItemBySlot(EquipmentSlot.CHEST);
			ItemStack powerpack = null;
			float storage = 0;
			if(!chest.isEmpty()&&chest.getItem() instanceof PowerpackItem)
				powerpack = chest;
			else if(!chest.isEmpty()&&chest.getItem() instanceof ArmorItem&&ItemNBTHelper.hasKey(chest, "IE:Powerpack"))
				powerpack = ItemNBTHelper.getItemStack(chest, "IE:Powerpack");
			else if(IEBipedLayerRenderer.POWERPACK_PLAYERS.containsKey(entityTemp.getUUID()))
				powerpack = IEBipedLayerRenderer.POWERPACK_PLAYERS.get(entityTemp.getUUID()).getLeft();

			if(powerpack!=null)
			{
				float max = EnergyHelper.getMaxEnergyStored(powerpack);
				storage = max <= 0?0: EnergyHelper.getEnergyStored(powerpack)/max;
				this.modelParts[7].zRot = 0.5235987f-(1.047197f*storage);
			}
		}

		super.renderToBuffer(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

		TextureAtlasSprite wireTexture = Minecraft.getInstance()
				.getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
				.apply(new ResourceLocation(Lib.MODID, "textures/block/wire"));
		for(InteractionHand hand : InteractionHand.values())
		{
			ItemStack stack = entityTemp.getItemInHand(hand);
			if(!stack.isEmpty()&&EnergyHelper.isFluxRelated(stack))
			{
				boolean right = (hand==InteractionHand.MAIN_HAND)==(entityTemp.getMainArm()==HumanoidArm.RIGHT);
				float angleX = (right?rightArm: leftArm).xRot;
				float angleZ = (right?rightArm: leftArm).zRot;
				String cacheKey = keyFormat.format(angleX)+"_"+keyFormat.format(angleZ);
				Vec3[] vex;
				try
				{
					vex = (right?catenaryCacheRight: catenaryCacheLeft).get(cacheKey, () ->
					{
						double armLength = .75f;
						double x = .3125+(right?1: -1)*armLength*Math.sin(angleZ);
						double y = armLength*Math.cos(angleX);
						double z = armLength*Math.sin(angleX);

						return WireUtils.getConnectionCatenary(new Vec3(.484375, -.75, .25), new Vec3(x, -y, z), 1.5);
					});
				} catch(Exception e)
				{
					throw new RuntimeException(e);
				}

				float vStep = 1f/vex.length;

				TransformingVertexBuilder builder = new TransformingVertexBuilder(bufferIn, matrixStackIn);
				double scaleX = right?-1: 1;
				builder.setColor(.93f, .63f, .27f, 1);
				builder.setLight(packedLightIn);
				builder.setOverlay(packedOverlayIn);
				final float v0 = wireTexture.getV0();
				final float v1 = wireTexture.getV1();
				for(int i = 1; i < vex.length; i++)
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

	static final DecimalFormat keyFormat = new DecimalFormat("0.0000");
	public static final Cache<String, Vec3[]> catenaryCacheLeft = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build();
	public static final Cache<String, Vec3[]> catenaryCacheRight = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build();

	static ModelPowerpack modelInstance;

	public static ModelPowerpack getModel()
	{
		if(modelInstance==null)
			modelInstance = new ModelPowerpack(.0625f, 0, 64, 32);
		return modelInstance;
	}
}
