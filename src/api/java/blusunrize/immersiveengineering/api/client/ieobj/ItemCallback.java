/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.client.ieobj;

import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nonnull;
import java.util.List;

public interface ItemCallback<Key> extends IEOBJCallback<Key>
{
	SetRestrictedField<BlockEntityWithoutLevelRenderer> DYNAMIC_IEOBJ_RENDERER = SetRestrictedField.client();
	IClientItemExtensions USE_IEOBJ_RENDER = new IClientItemExtensions()
	{
		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer()
		{
			return DYNAMIC_IEOBJ_RENDERER.getValue();
		}
	};

	default List<List<String>> getSpecialGroups(ItemStack stack, ItemDisplayContext transform, LivingEntity entity)
	{
		return List.of();
	}

	@Nonnull
	default Transformation getTransformForGroups(
			ItemStack stack, List<String> groups, ItemDisplayContext transform, LivingEntity entity, float partialTicks
	)
	{
		return Transformation.identity();
	}

	default boolean areGroupsFullbright(ItemStack stack, List<String> groups)
	{
		return false;
	}

	Key extractKey(ItemStack stack, LivingEntity owner);

	default void handlePerspective(Key key, LivingEntity holder, ItemDisplayContext cameraItemDisplayContext, PoseStack mat)
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
