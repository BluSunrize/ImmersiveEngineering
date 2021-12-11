/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.item;

import blusunrize.immersiveengineering.client.models.obj.callback.DefaultCallback;
import blusunrize.immersiveengineering.client.models.obj.callback.IEOBJCallback;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;

public interface ItemCallback<Key> extends IEOBJCallback<Key>
{
	default List<List<String>> getSpecialGroups(ItemStack stack, TransformType transform, LivingEntity entity)
	{
		return List.of();
	}

	@Nonnull
	default Transformation getTransformForGroups(
			ItemStack stack, List<String> groups, TransformType transform, LivingEntity entity, float partialTicks
	)
	{
		return Transformation.identity();
	}

	default boolean areGroupsFullbright(ItemStack stack, List<String> groups)
	{
		return false;
	}

	Key extractKey(ItemStack stack, LivingEntity owner);

	default void handlePerspective(Key key, LivingEntity holder, TransformType cameraTransformType, PoseStack mat)
	{
	}

	static <T> ItemCallback<T> castOrDefault(IEOBJCallback<T> generic)
	{
		if(generic instanceof ItemCallback<T> itemCB)
			return itemCB;
		else
			return DefaultCallback.cast();
	}
}
