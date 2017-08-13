package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;

/**
 * @author BluSunrize - 08.07.2017
 */
public class ModelIEPlayer extends ModelPlayer
{
	public ModelIEPlayer(ModelPlayer base)
	{
		super(.0625f, false);
		this.smallArms = base.smallArms;
		this.bipedHead = base.bipedHead;
		this.bipedHeadwear = base.bipedHeadwear;
		this.bipedBody = base.bipedBody;
		this.bipedRightArm = base.bipedRightArm;
		this.bipedLeftArm = base.bipedLeftArm;
		this.bipedRightLeg = base.bipedRightLeg;
		this.bipedLeftLeg = base.bipedLeftLeg;

		this.bipedBodyWear = base.bipedBodyWear;
		this.bipedLeftArmwear = base.bipedLeftArmwear;
		this.bipedRightArmwear = base.bipedRightArmwear;
		this.bipedLeftLegwear = base.bipedLeftLegwear;
		this.bipedRightLegwear = base.bipedRightLegwear;
		this.bipedCape = base.bipedCape;
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn)
	{
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);

		ClientUtils.handleBipedRotations(this, entityIn);
		copyModelAngles(this.bipedLeftArm, this.bipedLeftArmwear);
		copyModelAngles(this.bipedRightArm, this.bipedRightArmwear);
	}
}