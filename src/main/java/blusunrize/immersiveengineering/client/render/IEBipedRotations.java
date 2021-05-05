/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.common.config.IEClientConfig;
import blusunrize.immersiveengineering.common.items.*;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.HandSide;

public class IEBipedRotations
{
	public static void handleBipedRotations(BipedModel<?> model, Entity entity)
	{
		if(!IEClientConfig.fancyItemHolding.get())
			return;

		if(entity instanceof PlayerEntity)
		{
			PlayerEntity player = (PlayerEntity)entity;
			for(Hand hand : Hand.values())
			{
				ItemStack heldItem = player.getHeldItem(hand);
				if(!heldItem.isEmpty())
				{
					boolean right = (hand==Hand.MAIN_HAND)==(player.getPrimaryHand()==HandSide.RIGHT);
					if(heldItem.getItem() instanceof RevolverItem)
					{
						if(right)
						{
							model.bipedRightArm.rotateAngleX = -1.39626f+model.bipedHead.rotateAngleX;
							model.bipedRightArm.rotateAngleY = -.08726f+model.bipedHead.rotateAngleY;
						}
						else
						{
							model.bipedLeftArm.rotateAngleX = -1.39626f+model.bipedHead.rotateAngleX;
							model.bipedLeftArm.rotateAngleY = .08726f+model.bipedHead.rotateAngleY;
						}
					}
					else if(heldItem.getItem() instanceof DrillItem||heldItem.getItem() instanceof ChemthrowerItem)
					{
						if(right)
						{
							model.bipedLeftArm.rotateAngleX = -.87266f;
							model.bipedLeftArm.rotateAngleY = .52360f;
						}
						else
						{
							model.bipedRightArm.rotateAngleX = -.87266f;
							model.bipedRightArm.rotateAngleY = -.52360f;
						}
					}
					else if(heldItem.getItem() instanceof BuzzsawItem)
					{
						if(right)
						{
							model.bipedLeftArm.rotateAngleX = -.87266f;
							model.bipedLeftArm.rotateAngleY = .78539f;
						}
						else
						{
							model.bipedRightArm.rotateAngleX = -.87266f;
							model.bipedRightArm.rotateAngleY = -.78539f;
						}
					}
					else if(heldItem.getItem() instanceof RailgunItem)
					{
						if(right)
							model.bipedRightArm.rotateAngleX = -.87266f;
						else
							model.bipedLeftArm.rotateAngleX = -.87266f;
					}

				}
			}
		}
	}
}
