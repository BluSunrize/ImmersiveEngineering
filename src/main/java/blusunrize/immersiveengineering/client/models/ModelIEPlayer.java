package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.common.items.ItemChemthrower;
import blusunrize.immersiveengineering.common.items.ItemDrill;
import blusunrize.immersiveengineering.common.items.ItemRailgun;
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
					boolean right = (hand==EnumHand.MAIN_HAND) == (player.getPrimaryHand()==EnumHandSide.RIGHT);
					if(heldItem.getItem() instanceof ItemRevolver)
					{
						if(right)
						{
							this.bipedRightArm.rotateAngleX = -1.39626f;
							this.bipedRightArm.rotateAngleY = .08726f;
						} else
						{
							this.bipedLeftArm.rotateAngleX = -1.39626f;
							this.bipedLeftArm.rotateAngleY = .08726f;
						}
					}
					else if(heldItem.getItem() instanceof ItemDrill ||heldItem.getItem() instanceof ItemChemthrower)
					{
						if(right)
						{
							this.bipedLeftArm.rotateAngleX = -.87266f;
							this.bipedLeftArm.rotateAngleY = .52360f;
						}
						else
						{
							this.bipedRightArm.rotateAngleX = -.87266f;
							this.bipedRightArm.rotateAngleY = -0.52360f;
						}
					}
					else if(heldItem.getItem() instanceof ItemRailgun)
					{
						if(right)
							this.bipedRightArm.rotateAngleX = -.87266f;
						else
							this.bipedLeftArm.rotateAngleX = -.87266f;
					}

				}
			}
		}
	}
}
