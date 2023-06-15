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
import blusunrize.immersiveengineering.client.models.obj.callback.item.ChemthrowerCallbacks.Key;
import blusunrize.immersiveengineering.common.entities.illager.Bulwark;
import blusunrize.immersiveengineering.common.entities.illager.Fusilier;
import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractIllager.IllagerArmPose;
import net.minecraft.world.item.ItemStack;

public class ChemthrowerCallbacks implements ItemCallback<Key>
{
	public static final ChemthrowerCallbacks INSTANCE = new ChemthrowerCallbacks();

	@Override
	public Key extractKey(ItemStack stack, LivingEntity owner)
	{
		CompoundTag upgrades = ChemthrowerItem.getUpgradesStatic(stack);
		return new Key(upgrades.getInt("capacity") > 0, upgrades.getBoolean("multitank"));
	}

	@Override
	public boolean shouldRenderGroup(Key stack, String group, RenderType layer)
	{
		if("base".equals(group)||"grip".equals(group)||"cage".equals(group)||"tanks".equals(group))
			return true;
		if("large_tank".equals(group)&&stack.upgradedCapacity())
			return true;
		else if("multi_tank".equals(group)&&stack.multitank())
			return true;
		else
			return "tank".equals(group);
	}

	@Override
	public void handlePerspective(Key key, LivingEntity holder, TransformType cameraTransformType, PoseStack mat)
	{
		if(holder instanceof Bulwark bulwark&&(cameraTransformType==TransformType.THIRD_PERSON_RIGHT_HAND||cameraTransformType==TransformType.THIRD_PERSON_LEFT_HAND))
			if(bulwark.getArmPose()==IllagerArmPose.CELEBRATING)
			{
				mat.translate(-2, -1.25, -0);
				mat.mulPose(Vector3f.YP.rotationDegrees(-95));
				mat.mulPose(Vector3f.ZP.rotationDegrees(-30));
				mat.mulPose(Vector3f.XP.rotationDegrees(20));
			}
			else
			{
				mat.mulPose(new Quaternion(2, -10, -10, true));
				mat.translate(-.875, -.75, .3);
			}
	}


	public record Key(boolean upgradedCapacity, boolean multitank)
	{
	}
}
