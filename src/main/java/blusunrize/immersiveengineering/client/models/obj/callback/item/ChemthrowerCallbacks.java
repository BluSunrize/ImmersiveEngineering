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
import blusunrize.immersiveengineering.common.items.ChemthrowerItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractIllager.IllagerArmPose;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

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
	public void handlePerspective(Key key, LivingEntity holder, ItemDisplayContext cameraTransformType, PoseStack mat)
	{
		if(holder instanceof Bulwark bulwark&&(cameraTransformType==ItemDisplayContext.THIRD_PERSON_RIGHT_HAND||cameraTransformType==ItemDisplayContext.THIRD_PERSON_LEFT_HAND))
			if(bulwark.getArmPose()==IllagerArmPose.CELEBRATING)
			{
				mat.translate(-2, -1.25, -0);
				mat.mulPose(new Quaternionf().rotateY(-1.65806f));
				mat.mulPose(new Quaternionf().rotateZ(-0.523599f));
				mat.mulPose(new Quaternionf().rotateX(0.349066f));
			}
			else
			{
				mat.mulPose(new Quaternionf().rotateXYZ(0.0349066f, -0.174533f, -0.174533f));
				mat.translate(-.875, -.75, .3);
			}
	}

	@Override
	public Key getDefaultKey()
	{
		return new Key(false, false);
	}

	public record Key(boolean upgradedCapacity, boolean multitank)
	{
	}
}
