/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import net.minecraft.client.model.ModelRenderer;

public class ModelManeuverGear extends ModelIEArmorBase
{
	public ModelRenderer[] cannisters;

	public ModelManeuverGear(float modelSize, float p_i1149_2_, int textureWidthIn, int textureHeightIn)
	{
		super(modelSize, p_i1149_2_, textureWidthIn, textureHeightIn);
		cannisters = new ModelRenderer[8];
		this.cannisters[0] = new ModelRenderer(this, 0, 0);
		this.cannisters[0].addBox(-.5f, -1.5f, -3, 2, 3, 5, modelSize);
		this.cannisters[0].setRotationPoint(-5.5f, 14, 0);
		this.cannisters[0].rotateAngleX = (float)Math.toRadians(-20);
		this.bipedBody.addChild(cannisters[0]);

		this.cannisters[1] = new ModelRenderer(this, 0, 0);
		this.cannisters[1].mirror = true;
		this.cannisters[1].addBox(-1.5f, -1.5f, -3, 2, 3, 5, modelSize);
		this.cannisters[1].setRotationPoint(5.5f, 14, 0);
		this.cannisters[1].rotateAngleX = (float)Math.toRadians(-20);
		this.bipedBody.addChild(cannisters[1]);

		this.cannisters[2] = new ModelRenderer(this, 14, 10);
		this.cannisters[2].mirror = true;
		this.cannisters[2].addBox(-3.5f, -4.5f, 2.5f, 7, 3, 3, modelSize);
		this.cannisters[2].setRotationPoint(0, 14, 0);
		this.cannisters[2].rotateAngleZ = (float)Math.toRadians(15);
		this.bipedBody.addChild(cannisters[2]);

		this.cannisters[3] = new ModelRenderer(this, 6, 10);
		this.cannisters[3].mirror = true;
		this.cannisters[3].addBox(-6.5f, -3.5f, 3, 3, 1, 1, modelSize);
		this.cannisters[3].setRotationPoint(0, -1.5f, -0.5f);
		this.cannisters[3].rotateAngleX = (float)Math.toRadians(-22.5);
		this.cannisters[2].addChild(cannisters[3]);

		this.cannisters[4] = new ModelRenderer(this, 6, 8);
		this.cannisters[4].mirror = true;
		this.cannisters[4].addBox(-6f, -4.5f, 3, 1, 1, 1, modelSize);
		this.cannisters[3].addChild(cannisters[4]);

		this.cannisters[5] = new ModelRenderer(this, 14, 6);
		this.cannisters[5].mirror = true;
		this.cannisters[5].addBox(-7f, -5f, 2, 3, 1, 3, modelSize);
		this.cannisters[3].addChild(cannisters[5]);

		this.cannisters[6] = new ModelRenderer(this, 0, 8);
		this.cannisters[6].addBox(1, 7, 2, 2, 4, 1, modelSize);
		this.bipedBody.addChild(cannisters[6]);

		this.cannisters[7] = new ModelRenderer(this, 0, 8);
		this.cannisters[7].mirror = true;
		this.cannisters[7].addBox(-3, 7, 2, 2, 4, 1, modelSize);
		this.bipedBody.addChild(cannisters[7]);

		this.bipedHead.isHidden = true;
		this.bipedHeadwear.isHidden = true;
	}

//	@Override
//	public void render(Entity entity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale)
//	{
//		if(entity instanceof EntityLivingBase)
//		{
//			isChild = ((EntityLivingBase)entity).isChild();
//			isSneak = ((EntityLivingBase)entity).isSneaking();
//			isRiding = ((EntityLivingBase)entity).isRiding();
//			swingProgress = ((EntityLivingBase)entity).getSwingProgress(ClientUtils.timer().renderPartialTicks);
//		}
//		super.render(entity, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale);
//	}

	static ModelManeuverGear modelInstance;

	public static ModelManeuverGear getModel()
	{
		if(modelInstance==null)
			modelInstance = new ModelManeuverGear(.0625f, 0, 64, 32);
		return modelInstance;
	}
}