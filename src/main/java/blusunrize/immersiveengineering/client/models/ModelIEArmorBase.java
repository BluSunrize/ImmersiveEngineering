/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;

import java.util.function.Function;

public abstract class ModelIEArmorBase extends HumanoidModel<LivingEntity>
{
	public ModelIEArmorBase(ModelPart p_170679_, Function<ResourceLocation, RenderType> p_170680_)
	{
		super(p_170679_, p_170680_);
	}

	@Override
	public void setupAnim(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		attackTime = entity.getAttackAnim(ClientUtils.partialTicks());
		if(entity instanceof ArmorStand)
			setRotationAnglesStand(entity);
		else if(entity instanceof Skeleton||entity instanceof Zombie)
			setRotationAnglesZombie(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		else
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		young = entity.isBaby();
		crouching = entity.isShiftKeyDown();
		riding = entity.isPassenger()&&(entity.getVehicle()!=null&&entity.getVehicle().shouldRiderSit());
	}

	public void setRotationAnglesZombie(LivingEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch)
	{
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		float f6 = Mth.sin(this.attackTime*3.141593F);
		float f7 = Mth.sin((1.0F-(1.0F-this.attackTime)*(1.0F-this.attackTime))*3.141593F);
		this.rightArm.zRot = 0.0F;
		this.leftArm.zRot = 0.0F;
		this.rightArm.yRot = (-(0.1F-f6*0.6F));
		this.leftArm.yRot = (0.1F-f6*0.6F);
		this.rightArm.xRot = -1.570796F;
		this.leftArm.xRot = -1.570796F;
		this.rightArm.xRot -= f6*1.2F-f7*0.4F;
		this.leftArm.xRot -= f6*1.2F-f7*0.4F;
		this.rightArm.zRot += Mth.cos(ageInTicks*0.09F)*0.05F+0.05F;
		this.leftArm.zRot -= Mth.cos(ageInTicks*0.09F)*0.05F+0.05F;
		this.rightArm.xRot += Mth.sin(ageInTicks*0.067F)*0.05F;
		this.leftArm.xRot -= Mth.sin(ageInTicks*0.067F)*0.05F;
	}

	private void setRotationAnglesStand(LivingEntity entity)
	{
		if(entity instanceof ArmorStand entityarmorstand)
		{
			this.head.xRot = (0.01745329F*entityarmorstand.getHeadPose().getX());
			this.head.yRot = (0.01745329F*entityarmorstand.getHeadPose().getY());
			this.head.zRot = (0.01745329F*entityarmorstand.getHeadPose().getZ());
			this.head.setPos(0.0F, 1.0F, 0.0F);
			this.body.xRot = (0.01745329F*entityarmorstand.getBodyPose().getX());
			this.body.yRot = (0.01745329F*entityarmorstand.getBodyPose().getY());
			this.body.zRot = (0.01745329F*entityarmorstand.getBodyPose().getZ());
			this.leftArm.xRot = (0.01745329F*entityarmorstand.getLeftArmPose().getX());
			this.leftArm.yRot = (0.01745329F*entityarmorstand.getLeftArmPose().getY());
			this.leftArm.zRot = (0.01745329F*entityarmorstand.getLeftArmPose().getZ());
			this.rightArm.xRot = (0.01745329F*entityarmorstand.getRightArmPose().getX());
			this.rightArm.yRot = (0.01745329F*entityarmorstand.getRightArmPose().getY());
			this.rightArm.zRot = (0.01745329F*entityarmorstand.getRightArmPose().getZ());
			this.leftLeg.xRot = (0.01745329F*entityarmorstand.getLeftLegPose().getX());
			this.leftLeg.yRot = (0.01745329F*entityarmorstand.getLeftLegPose().getY());
			this.leftLeg.zRot = (0.01745329F*entityarmorstand.getLeftLegPose().getZ());
			this.leftLeg.setPos(1.9F, 11.0F, 0.0F);
			this.rightLeg.xRot = (0.01745329F*entityarmorstand.getRightLegPose().getX());
			this.rightLeg.yRot = (0.01745329F*entityarmorstand.getRightLegPose().getY());
			this.rightLeg.zRot = (0.01745329F*entityarmorstand.getRightLegPose().getZ());
			this.rightLeg.setPos(-1.9F, 11.0F, 0.0F);
			this.hat.copyFrom(this.head);
		}
	}
}