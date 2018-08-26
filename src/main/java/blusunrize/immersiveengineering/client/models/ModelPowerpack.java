/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.render.IEBipedLayerRenderer;
import blusunrize.immersiveengineering.common.items.ItemPowerpack;
import blusunrize.immersiveengineering.common.util.EnergyHelper;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * @author BluSunrize
 * @since 15.06.2017
 */
public class ModelPowerpack extends ModelIEArmorBase
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

		this.bipedHead.isHidden = true;
		this.bipedHeadwear.isHidden = true;
		this.bipedLeftArm.isHidden = true;
		this.bipedRightArm.isHidden = true;
		this.bipedLeftLeg.isHidden = true;
		this.bipedRightLeg.isHidden = true;
	}

	@Override
	public void render(Entity entity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale)
	{
		if(entity instanceof EntityLivingBase)
		{
			ItemStack chest = ((EntityLivingBase)entity).getItemStackFromSlot(EntityEquipmentSlot.CHEST);
			ItemStack powerpack = null;
			float storage = 0;
			if(!chest.isEmpty()&&chest.getItem() instanceof ItemPowerpack)
				powerpack = chest;
			else if(!chest.isEmpty()&&chest.getItem() instanceof ItemArmor&&ItemNBTHelper.hasKey(chest, "IE:Powerpack"))
				powerpack = ItemNBTHelper.getItemStack(chest, "IE:Powerpack");
			else if(IEBipedLayerRenderer.POWERPACK_PLAYERS.containsKey(entity.getUniqueID()))
				powerpack = IEBipedLayerRenderer.POWERPACK_PLAYERS.get(entity.getUniqueID()).getLeft();

			if(powerpack!=null)
			{
				float max = EnergyHelper.getMaxEnergyStored(powerpack);
				storage = max <= 0?0: EnergyHelper.getEnergyStored(powerpack)/max;
				this.modelParts[7].rotateAngleZ = 0.5235987f-(1.047197f*storage);
			}
		}

		GlStateManager.enableBlend();
		super.render(entity, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale);
		GlStateManager.disableBlend();

		ClientUtils.bindTexture("immersiveengineering:textures/blocks/wire.png");
		GlStateManager.pushMatrix();
		if(entity instanceof EntityLivingBase)
			for(EnumHand hand : EnumHand.values())
			{
				ItemStack stack = ((EntityLivingBase)entity).getHeldItem(hand);
				if(!stack.isEmpty()&&EnergyHelper.isFluxItem(stack))
				{
					boolean right = (hand==EnumHand.MAIN_HAND)==(((EntityLivingBase)entity).getPrimaryHand()==EnumHandSide.RIGHT);
					float angleX = (right?bipedRightArm: bipedLeftArm).rotateAngleX;
					float angleZ = (right?bipedRightArm: bipedLeftArm).rotateAngleZ;
					String cacheKey = keyFormat.format(angleX)+"_"+keyFormat.format(angleZ);
					Vec3d[] vex = new Vec3d[0];
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
					}

					float vStep = 1f/vex.length;
					int i = 0;
					Tessellator tes = ClientUtils.tes();
					BufferBuilder worldrenderer = tes.getBuffer();

//					float[] colour = {.7f,.42f,.25f,1};
					float[] colour = {.93f, .63f, .27f, 1};
					worldrenderer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);
					for(Vec3d v : vex)
					{
						worldrenderer.pos((right?-v.x: v.x)-.015625, -v.y, v.z).tex(vStep*i, 0).color(colour[0], colour[1], colour[2], colour[3]).endVertex();
						worldrenderer.pos((right?-v.x: v.x)+.015625, -v.y, v.z).tex(vStep*i++, 1).color(colour[0], colour[1], colour[2], colour[3]).endVertex();
					}
					tes.draw();
					worldrenderer.begin(GL11.GL_QUAD_STRIP, DefaultVertexFormats.POSITION_TEX_COLOR);
					i = 0;
					for(Vec3d v : vex)
					{
						worldrenderer.pos((right?-v.x: v.x), -v.y-.015625, v.z).tex(vStep*i, 0).color(colour[0], colour[1], colour[2], colour[3]).endVertex();
						worldrenderer.pos((right?-v.x: v.x), -v.y+.015625, v.z).tex(vStep*i++, 1).color(colour[0], colour[1], colour[2], colour[3]).endVertex();
					}
					tes.draw();
				}
			}
		GlStateManager.popMatrix();
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
