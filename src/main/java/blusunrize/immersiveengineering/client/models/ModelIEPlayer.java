package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.common.items.ItemRevolver;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

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

		if(entityIn instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer)entityIn;
			for(EnumHand hand : EnumHand.values())
			{
				ItemStack heldItem = player.getHeldItem(hand);
				if(!heldItem.isEmpty())
				{
					ArmPose twohanded = null;
//				if(OreDictionary.itemMatches(new ItemStack(IEContent.itemChemthrower), heldItem, true) || OreDictionary.itemMatches(new ItemStack(IEContent.itemDrill), heldItem, true) || OreDictionary.itemMatches(new ItemStack(IEContent.itemRailgun), heldItem, true))
//					twohanded = ArmPose.BLOCK;
//				if(twohanded!=null && event.getEntity().getHeldItem(hand==EnumHand.MAIN_HAND?EnumHand.OFF_HAND:EnumHand.MAIN_HAND).isEmpty())
//				{
//					if(model instanceof ModelBiped)
//					{
//						if(hand==EnumHand.MAIN_HAND)
//							((ModelBiped) model).leftArmPose = twohanded;
//						else
//							((ModelBiped) model).rightArmPose = twohanded;
//					}
//				}
//				ModelBase model = event.getRenderer().getMainModel();
					boolean right = (hand==EnumHand.MAIN_HAND) == (player.getPrimaryHand()==EnumHandSide.RIGHT);
					if(heldItem.getItem() instanceof ItemRevolver)
						if(right)
						{
							this.bipedRightArm.rotateAngleX = (float)Math.toRadians(-80);
							this.bipedRightArm.rotateAngleY = (float)Math.toRadians(-5);
						}
						else
						{
							this.bipedLeftArm.rotateAngleX = (float)Math.toRadians(-80);
							this.bipedLeftArm.rotateAngleY = (float)Math.toRadians(5);
						}
				}
			}
		}
	}
}
