/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.render.IEBipedLayerRenderer;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.items.PowerpackItem;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * @author BluSunrize
 * @since 15.06.2017
 */
public class ModelPowerpack<T extends LivingEntity> extends ModelIEArmorBase<T>
{
	public ModelRenderer[] modelParts;
	public ModelRenderer[] colouredParts;

	public ModelPowerpack(float modelSize, float p_i1149_2_, int textureWidthIn, int textureHeightIn)
	{
		super(modelSize, p_i1149_2_, textureWidthIn, textureHeightIn);
		modelParts = new ModelRenderer[8];
		colouredParts = new ModelRenderer[4];

		this.modelParts[0] = new ModelRenderer(this, 40, 0);
		this.modelParts[0].addBox(-4f, -5f, -2f, 8, 10, 3, 0);
		this.modelParts[0].setRotationPoint(0, 5, 4);
		this.bipedBody.addChild(modelParts[0]);

		this.modelParts[1] = new ModelRenderer(this, 12, 0);
		this.modelParts[1].addBox(-3f, -2f, -2f, 6, 4, 4, 0);
		this.modelParts[1].setRotationPoint(0, 12, 4f);
		this.bipedBody.addChild(modelParts[1]);

		this.modelParts[2] = new ModelRenderer(this, 0, 0);
		this.modelParts[2].addBox(-1f, -4f, -1f, 2, 8, 2, 0);
		this.modelParts[2].setRotationPoint(-5f, 5, 3f);
		this.bipedBody.addChild(modelParts[2]);

		this.modelParts[3] = new ModelRenderer(this, 0, 0);
		this.modelParts[3].addBox(-1f, -4.0f, -1f, 2, 8, 2, 0);
		this.modelParts[3].setRotationPoint(5f, 5, 3f);
		this.modelParts[3].rotateAngleZ = 3.14159f;
		this.bipedBody.addChild(modelParts[3]);


		ModelRenderer gauge = new ModelRenderer(this, 40, 13);
		gauge.addBox(-.5f, -1.5f, -.5f, 1, 3, 1, 0);
		gauge.setRotationPoint(-3f, 5.5f, 5f);
		this.bipedBody.addChild(gauge);

		gauge = new ModelRenderer(this, 40, 13);
		gauge.addBox(-.5f, -1.5f, -.5f, 1, 3, 1, 0);
		gauge.setRotationPoint(1f, 5.5f, 5f);
		this.bipedBody.addChild(gauge);

		gauge = new ModelRenderer(this, 44, 13);
		gauge.addBox(-2f, -.5f, -.5f, 3, 1, 1, 0);
		gauge.setRotationPoint(-.5f, 3.5f, 5f);
		this.bipedBody.addChild(gauge);

		gauge = new ModelRenderer(this, 44, 13);
		gauge.addBox(-2f, -.5f, -.5f, 3, 1, 1, 0);
		gauge.setRotationPoint(-.5f, 7.5f, 5f);
		this.bipedBody.addChild(gauge);

		this.modelParts[7] = new ModelRenderer(this, 52, 14);
		this.modelParts[7].addBox(-.5f, -3.5f, -.5f, 1, 4, 1, -.25f);
		this.modelParts[7].setRotationPoint(-1f, 7.625f, 5f);
		this.modelParts[7].rotateAngleZ = 0.7853975f;
		this.bipedBody.addChild(modelParts[7]);

		ModelRenderer connector = new ModelRenderer(this, 17, 9);
		connector.addBox(-1f, -1.5f, -1.5f, 3, 3, 3, 0);
		connector.setRotationPoint(-4.5f, 0f, 0f);
		this.modelParts[1].addChild(connector);

		connector = new ModelRenderer(this, 17, 9);
		connector.addBox(-1f, -1.5f, -1.5f, 3, 3, 3, -.375f);
		connector.setRotationPoint(-6f, 0f, 0);
		this.modelParts[1].addChild(connector);

		connector = new ModelRenderer(this, 29, 9);
		connector.addBox(-1f, -1.5f, -1.5f, 1, 3, 3, 0);
		connector.setRotationPoint(-6.25f, 0f, 0);
		this.modelParts[1].addChild(connector);

		connector = new ModelRenderer(this, 12, 8);
		connector.addBox(-.5f, -1f, -1f, 2, 2, 2, 0);
		connector.setRotationPoint(-7.5f, 0f, 0f);
		this.modelParts[1].addChild(connector);


		connector = new ModelRenderer(this, 17, 9);
		connector.addBox(-1f, -1.5f, -1.5f, 3, 3, 3, 0);
		connector.setRotationPoint(4.5f, 0f, 0f);
		connector.rotateAngleY = 3.14159f;
		this.modelParts[1].addChild(connector);

		connector = new ModelRenderer(this, 17, 9);
		connector.addBox(-1f, -1.5f, -1.5f, 3, 3, 3, -.375f);
		connector.setRotationPoint(6f, 0f, 0);
		connector.rotateAngleY = 3.14159f;
		this.modelParts[1].addChild(connector);

		connector = new ModelRenderer(this, 29, 9);
		connector.addBox(-1f, -1.5f, -1.5f, 1, 3, 3, 0);
		connector.setRotationPoint(6.25f, 0f, 0);
		connector.rotateAngleY = 3.14159f;
		this.modelParts[1].addChild(connector);

		connector = new ModelRenderer(this, 12, 8);
		connector.addBox(-.5f, -1f, -1f, 2, 2, 2, 0);
		connector.setRotationPoint(7.5f, 0f, 0f);
		connector.rotateAngleY = 3.14159f;
		this.modelParts[1].addChild(connector);

		for(int i = 0; i < 3; i++)
		{
			float pos = 3.125f-i*2.25f;
			ModelRenderer tube = new ModelRenderer(this, 56, 19);
			tube.addBox(-1f, -1f, -1f, 2, 2, 2, -.25f);
			tube.setRotationPoint(pos, 1, 5);
			tube.rotateAngleX = (float)Math.toRadians(-45);
			this.bipedBody.addChild(tube);

			ModelRenderer tube2 = new ModelRenderer(this, 52, 18);
			tube2.addBox(-.5f, -2f, -.5f, 1, 3, 1, 0);
			tube2.setRotationPoint(0, -.5f, 0);
			tube.addChild(tube2);

			tube2 = new ModelRenderer(this, 56, 15);
			tube2.addBox(-1f, -2f, -1f, 2, 2, 2, 0);
			tube2.setRotationPoint(0, -.75f, 0);
			tube.addChild(tube2);

			tube2 = new ModelRenderer(this, 56, 13);
			tube2.addBox(-.5f, -1f, -.5f, 1, 1, 1, 0);
			tube2.setRotationPoint(0, -2.25f, 0);
			tube.addChild(tube2);

			tube2 = new ModelRenderer(this, 56, 13);
			tube2.addBox(-.5f, -1f, -.5f, 1, 1, 1, -.25f);
			tube2.setRotationPoint(0, -3f, 0);
			tube.addChild(tube2);
		}

		this.bipedHead.showModel = false;
		this.bipedHeadwear.showModel = false;
		this.bipedLeftArm.showModel = false;
		this.bipedRightArm.showModel = false;
		this.bipedLeftLeg.showModel = false;
		this.bipedRightLeg.showModel = false;
	}

	@Override
	public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
	{
		if(entityTemp instanceof LivingEntity)
		{
			ItemStack chest = entityTemp.getItemStackFromSlot(EquipmentSlotType.CHEST);
			ItemStack powerpack = null;
			float storage = 0;
			if(!chest.isEmpty()&&chest.getItem() instanceof PowerpackItem)
				powerpack = chest;
			else if(!chest.isEmpty()&&chest.getItem() instanceof ArmorItem&&ItemNBTHelper.hasKey(chest, "IE:Powerpack"))
				powerpack = ItemNBTHelper.getItemStack(chest, "IE:Powerpack");
			else if(IEBipedLayerRenderer.POWERPACK_PLAYERS.containsKey(entityTemp.getUniqueID()))
				powerpack = IEBipedLayerRenderer.POWERPACK_PLAYERS.get(entityTemp.getUniqueID()).getLeft();

			if(powerpack!=null)
			{
				float max = EnergyHelper.getMaxEnergyStored(powerpack);
				storage = max <= 0?0: EnergyHelper.getEnergyStored(powerpack)/max;
				this.modelParts[7].rotateAngleZ = 0.5235987f-(1.047197f*storage);
			}
		}

		super.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);

		TextureAtlasSprite wireTexture = Minecraft.getInstance()
				.getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE)
				.apply(new ResourceLocation(Lib.MODID, "textures/block/wire"));
		for(Hand hand : Hand.values())
		{
			ItemStack stack = entityTemp.getHeldItem(hand);
			if(!stack.isEmpty()&&EnergyHelper.isFluxRelated(stack))
			{
				boolean right = (hand==Hand.MAIN_HAND)==(entityTemp.getPrimaryHand()==HandSide.RIGHT);
				float angleX = (right?bipedRightArm: bipedLeftArm).rotateAngleX;
				float angleZ = (right?bipedRightArm: bipedLeftArm).rotateAngleZ;
				String cacheKey = keyFormat.format(angleX)+"_"+keyFormat.format(angleZ);
				Vec3d[] vex;
				try
				{
					vex = (right?catenaryCacheRight: catenaryCacheLeft).get(cacheKey, () ->
					{
						double armLength = .75f;
						double x = .3125+(right?1: -1)*armLength*Math.sin(angleZ);
						double y = armLength*Math.cos(angleX);
						double z = armLength*Math.sin(angleX);

						return ApiUtils.getConnectionCatenary(new Vec3d(.484375, -.75, .25), new Vec3d(x, -y, z), 1.5);
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
				final float v0 = wireTexture.getMinV();
				final float v1 = wireTexture.getMaxV();
				for(int i = 1; i < vex.length; i++)
					for(int offset = 0; offset < 2; ++offset)
					{
						int iHere = i-offset;
						int iThere = i-1+offset;
						Vec3d vecHere = vex[iHere];
						Vec3d vecThere = vex[iThere];
						builder.setNormal((float)(vecThere.z-vecHere.z), 0, (float)(vecHere.x-vecThere.x));
						for(int index : new int[]{iHere, iThere})
						{
							Vec3d vec = vex[index];
							double xA = scaleX*vec.x-.015625;
							double xB = scaleX*vec.x+.015625;
							if(index==iHere)
							{
								double tmp = xA;
								xA = xB;
								xB = tmp;
							}
							builder.pos(xA, -vec.y, vec.z)
									.tex(wireTexture.getInterpolatedU(vStep*index), v0)
									.endVertex();
							builder.pos(xB, -vec.y, vec.z)
									.tex(wireTexture.getInterpolatedU(vStep*index), v1)
									.endVertex();
						}
						builder.setNormal((float)(vecThere.y-vecHere.y), (float)(vecHere.x-vecThere.x), 0);
						for(int index : new int[]{iHere, iThere})
						{
							Vec3d vec = vex[index];
							double yA = -vec.y-.015625;
							double yB = -vec.y;
							if(index==iThere)
							{
								double tmp = yA;
								yA = yB;
								yB = tmp;
							}
							builder.pos(scaleX*vec.x, yA, vec.z)
									.tex(wireTexture.getInterpolatedU(vStep*index), v0)
									.endVertex();
							builder.pos(scaleX*vec.x, yB, vec.z)
									.tex(wireTexture.getInterpolatedU(vStep*index), v1)
									.endVertex();
						}
					}
			}
		}
	}

	@Override
	public RenderType getRenderType(ResourceLocation locationIn)
	{
		return RenderType.getEntityTranslucent(locationIn);
	}

	static final DecimalFormat keyFormat = new DecimalFormat("0.0000");
	public static final Cache<String, Vec3d[]> catenaryCacheLeft = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES)
			.build();
	public static final Cache<String, Vec3d[]> catenaryCacheRight = CacheBuilder.newBuilder()
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
