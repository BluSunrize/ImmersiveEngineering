/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.item;

import blusunrize.immersiveengineering.common.items.IEShieldItem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class ShieldCallbacks implements ItemCallback<ShieldCallbacks.Key>
{
	public static final ShieldCallbacks INSTANCE = new ShieldCallbacks();

	@Override
	public Key extractKey(ItemStack stack, LivingEntity owner)
	{
		CompoundTag upgrades = IEShieldItem.getUpgradesStatic(stack);
		return new Key(upgrades.getBoolean("flash"), upgrades.getBoolean("shock"));
	}

	@Override
	public boolean shouldRenderGroup(Key object, String group, RenderType layer)
	{
		if("flash".equals(group))
			return object.flash();
		else if("shock".equals(group))
			return object.shock();
		return true;
	}

	@Override
	public void handlePerspective(Key key, LivingEntity holder, TransformType cameraTransformType, PoseStack mat)
	{
		if(holder!=null&&holder.isUsingItem())
			if((holder.getUsedItemHand()==InteractionHand.MAIN_HAND)==(holder.getMainArm()==HumanoidArm.RIGHT))
			{
				if(cameraTransformType==TransformType.FIRST_PERSON_RIGHT_HAND)
				{
					mat.mulPose(new Quaternionf().rotateXYZ(-.15F, 0, 0));
					mat.translate(-.25, .5, -.4375);
				}
				else if(cameraTransformType==TransformType.THIRD_PERSON_RIGHT_HAND)
				{
					mat.mulPose(new Quaternionf().rotateXYZ(0.52359F, 0, 0));
					mat.mulPose(new Quaternionf().rotateXYZ(0, 0.78539F, 0));
					mat.translate(.40625, -.125, -.125);
				}
			}
			else
			{
				if(cameraTransformType==TransformType.FIRST_PERSON_LEFT_HAND)
				{
					mat.mulPose(new Quaternionf().rotateXYZ(.15F, 0, 0));
					mat.translate(.25, .375, .4375);
				}
				else if(cameraTransformType==TransformType.THIRD_PERSON_LEFT_HAND)
				{
					mat.mulPose(new Quaternionf().rotateXYZ(-0.52359F, 1, 0));
					mat.translate(.1875, .3125, .75);
				}
			}
	}

	public record Key(boolean flash, boolean shock)
	{
	}
}
