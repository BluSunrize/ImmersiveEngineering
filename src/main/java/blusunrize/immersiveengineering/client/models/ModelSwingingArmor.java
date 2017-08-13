package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;

public class ModelSwingingArmor extends ModelBiped
{
	public ModelSwingingArmor(ModelBiped base)
	{
		this.bipedHead = base.bipedHead;
		this.bipedHeadwear = base.bipedHeadwear;
		this.bipedBody = base.bipedBody;
		this.bipedRightArm = base.bipedRightArm;
		this.bipedLeftArm = base.bipedLeftArm;
		this.bipedRightLeg = base.bipedRightLeg;
		this.bipedLeftLeg = base.bipedLeftLeg;
		this.leftArmPose = base.leftArmPose;
		this.rightArmPose = base.rightArmPose;
		this.isSneak = base.isSneak;
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
	{
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		ClientUtils.handleBipedRotations(this, entityIn);
	}
}
