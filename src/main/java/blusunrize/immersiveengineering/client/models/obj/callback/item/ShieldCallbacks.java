/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.item;

import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.client.models.obj.callback.item.ShieldCallbacks.Key;
import blusunrize.immersiveengineering.common.items.IEShieldItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

import static net.minecraft.world.item.ItemDisplayContext.*;

public class ShieldCallbacks implements ItemCallback<Key>
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
	public void handlePerspective(Key key, LivingEntity holder, ItemDisplayContext cameraItemDisplayContext, PoseStack mat)
	{
		if(holder==null||!holder.isUsingItem())
			return;
		boolean leftHand = cameraItemDisplayContext==FIRST_PERSON_LEFT_HAND||cameraItemDisplayContext==THIRD_PERSON_LEFT_HAND;
		boolean rightHand = cameraItemDisplayContext==FIRST_PERSON_RIGHT_HAND||cameraItemDisplayContext==THIRD_PERSON_RIGHT_HAND;
		if(!leftHand&&!rightHand)
			return;
		boolean leftIsMain = holder.getMainArm()==HumanoidArm.LEFT;
		InteractionHand inHand = (leftIsMain==leftHand)?InteractionHand.MAIN_HAND: InteractionHand.OFF_HAND;
		if(holder.getUsedItemHand()!=inHand)
			return;

		if(cameraItemDisplayContext==FIRST_PERSON_RIGHT_HAND)
		{
			mat.mulPose(new Quaternionf().rotateXYZ(-.15F, 0, 0));
			mat.translate(-.25, .5, -.4375);
		}
		else if(cameraItemDisplayContext==THIRD_PERSON_RIGHT_HAND)
		{
			mat.mulPose(new Quaternionf().rotateXYZ(0.52359F, 0, 0));
			mat.mulPose(new Quaternionf().rotateXYZ(0, 0.78539F, 0));
			mat.translate(.40625, -.125, -.125);
		}
		if(cameraItemDisplayContext==FIRST_PERSON_LEFT_HAND)
		{
			mat.mulPose(new Quaternionf().rotateXYZ(.15F, 0, 0));
			mat.translate(-.25, .375, .4375);
		}
		else if(cameraItemDisplayContext==THIRD_PERSON_LEFT_HAND)
		{
			mat.mulPose(new Quaternionf().rotateX(-0.52359F).rotateY(-0.78539F));
			mat.translate(-.1875, .3125, .4375);
		}
	}

	@Override
	public Key getDefaultKey()
	{
		return new Key(false, false);
	}

	public record Key(boolean flash, boolean shock)
	{
	}
}
