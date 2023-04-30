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
import blusunrize.immersiveengineering.client.models.obj.callback.item.RailgunCallbacks.Key;
import blusunrize.immersiveengineering.common.entities.illager.Fusilier;
import blusunrize.immersiveengineering.common.items.RailgunItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class RailgunCallbacks implements ItemCallback<Key>
{
	public static RailgunCallbacks INSTANCE = new RailgunCallbacks();

	@Override
	public Key extractKey(ItemStack stack, LivingEntity owner)
	{
		CompoundTag upgrades = RailgunItem.getUpgradesStatic(stack);
		return new Key(upgrades.getBoolean("scope"), upgrades.getDouble("speed") > 0);
	}

	@Override
	public boolean shouldRenderGroup(Key stack, String group, RenderType layer)
	{
		if(group.equals("upgrade_scope"))
			return stack.scope();
		if(group.equals("upgrade_speed"))
			return stack.speed();
		if(group.equals("barrel_top"))
			return !stack.speed();
		return true;
	}

	@Override
	public void handlePerspective(Key key, LivingEntity holder, TransformType cameraTransformType, PoseStack mat)
	{
		if(holder instanceof Fusilier fusilier&&(cameraTransformType==TransformType.THIRD_PERSON_RIGHT_HAND||cameraTransformType==TransformType.THIRD_PERSON_LEFT_HAND))
		{
			switch(fusilier.getArmPose())
			{
				case CROSSBOW_HOLD ->
				{
					mat.mulPose(new Quaternion(-10, -15, -40, true));
					mat.translate(.5, -2.5, .75);
				}
				case CELEBRATING ->
				{
					mat.translate(-4.5, -4, 2);
					mat.mulPose(Vector3f.YP.rotationDegrees(-95));
					mat.mulPose(Vector3f.ZP.rotationDegrees(-50));
					mat.mulPose(Vector3f.XP.rotationDegrees(20));
				}
				default ->
				{
					mat.mulPose(new Quaternion(-8, 0, 110, true));
					mat.translate(4.75, 4, 0);
				}
			}
		}
	}

	public record Key(boolean scope, boolean speed)
	{
	}
}
