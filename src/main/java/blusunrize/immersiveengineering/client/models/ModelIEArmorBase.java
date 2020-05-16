/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.util.math.MathHelper;

public abstract class ModelIEArmorBase<T extends LivingEntity> extends BipedModel<T>
{
	public ModelIEArmorBase(float modelSize, float p_i1149_2_, int textureWidthIn, int textureHeightIn)
	{
		super(modelSize, p_i1149_2_, textureWidthIn, textureHeightIn);
	}

	@Override
	public void render(T entity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale)
	{
		isChild = entity.isChild();
		isSneak = entity.isSneaking();
		isSitting = entity.isPassenger()&&(entity.getRidingEntity()!=null&&entity.getRidingEntity().shouldRiderSit());
		this.setLivingAnimations(entity, p_78088_2_, p_78088_3_, ClientUtils.partialTicks());
		super.render(entity, p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_);
	}

	@Override
	public void setRotationAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		swingProgress = entity.getSwingProgress(ClientUtils.partialTicks());
		if(entity instanceof ArmorStandEntity)
			setRotationAnglesStand(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
		else if(entity instanceof SkeletonEntity||entity instanceof ZombieEntity)
			setRotationAnglesZombie(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
		else
			super.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
	}

	public void setRotationAnglesZombie(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor)
	{
		super.setRotationAngles(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		float f6 = MathHelper.sin(this.swingProgress*3.141593F);
		float f7 = MathHelper.sin((1.0F-(1.0F-this.swingProgress)*(1.0F-this.swingProgress))*3.141593F);
		this.bipedRightArm.rotateAngleZ = 0.0F;
		this.bipedLeftArm.rotateAngleZ = 0.0F;
		this.bipedRightArm.rotateAngleY = (-(0.1F-f6*0.6F));
		this.bipedLeftArm.rotateAngleY = (0.1F-f6*0.6F);
		this.bipedRightArm.rotateAngleX = -1.570796F;
		this.bipedLeftArm.rotateAngleX = -1.570796F;
		this.bipedRightArm.rotateAngleX -= f6*1.2F-f7*0.4F;
		this.bipedLeftArm.rotateAngleX -= f6*1.2F-f7*0.4F;
		this.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks*0.09F)*0.05F+0.05F;
		this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks*0.09F)*0.05F+0.05F;
		this.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks*0.067F)*0.05F;
		this.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks*0.067F)*0.05F;
	}

	public void setRotationAnglesStand(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor)
	{
		if(entity instanceof ArmorStandEntity)
		{
			ArmorStandEntity entityarmorstand = (ArmorStandEntity)entity;
			this.bipedHead.rotateAngleX = (0.01745329F*entityarmorstand.getHeadRotation().getX());
			this.bipedHead.rotateAngleY = (0.01745329F*entityarmorstand.getHeadRotation().getY());
			this.bipedHead.rotateAngleZ = (0.01745329F*entityarmorstand.getHeadRotation().getZ());
			this.bipedHead.setRotationPoint(0.0F, 1.0F, 0.0F);
			this.bipedBody.rotateAngleX = (0.01745329F*entityarmorstand.getBodyRotation().getX());
			this.bipedBody.rotateAngleY = (0.01745329F*entityarmorstand.getBodyRotation().getY());
			this.bipedBody.rotateAngleZ = (0.01745329F*entityarmorstand.getBodyRotation().getZ());
			this.bipedLeftArm.rotateAngleX = (0.01745329F*entityarmorstand.getLeftArmRotation().getX());
			this.bipedLeftArm.rotateAngleY = (0.01745329F*entityarmorstand.getLeftArmRotation().getY());
			this.bipedLeftArm.rotateAngleZ = (0.01745329F*entityarmorstand.getLeftArmRotation().getZ());
			this.bipedRightArm.rotateAngleX = (0.01745329F*entityarmorstand.getRightArmRotation().getX());
			this.bipedRightArm.rotateAngleY = (0.01745329F*entityarmorstand.getRightArmRotation().getY());
			this.bipedRightArm.rotateAngleZ = (0.01745329F*entityarmorstand.getRightArmRotation().getZ());
			this.bipedLeftLeg.rotateAngleX = (0.01745329F*entityarmorstand.getLeftLegRotation().getX());
			this.bipedLeftLeg.rotateAngleY = (0.01745329F*entityarmorstand.getLeftLegRotation().getY());
			this.bipedLeftLeg.rotateAngleZ = (0.01745329F*entityarmorstand.getLeftLegRotation().getZ());
			this.bipedLeftLeg.setRotationPoint(1.9F, 11.0F, 0.0F);
			this.bipedRightLeg.rotateAngleX = (0.01745329F*entityarmorstand.getRightLegRotation().getX());
			this.bipedRightLeg.rotateAngleY = (0.01745329F*entityarmorstand.getRightLegRotation().getY());
			this.bipedRightLeg.rotateAngleZ = (0.01745329F*entityarmorstand.getRightLegRotation().getZ());
			this.bipedRightLeg.setRotationPoint(-1.9F, 11.0F, 0.0F);
			this.bipedHeadwear.copyModelAngles(this.bipedHead);
		}
	}
}