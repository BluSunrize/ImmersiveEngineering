/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("deprecation")
//T must be ItemStack for Items or IBlockState for TileEntities implementing this
public interface IOBJModelCallback<T>
{
	ModelProperty<IOBJModelCallback> PROPERTY = new ModelProperty<>();

	@OnlyIn(Dist.CLIENT)
	default TextureAtlasSprite getTextureReplacement(T object, String material)
	{
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	default boolean shouldRenderGroup(T object, String group)
	{
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	default Optional<TRSRTransformation> applyTransformations(T object, String group, Optional<TRSRTransformation> transform)
	{
		return transform;
	}

	@OnlyIn(Dist.CLIENT)
	default Matrix4 handlePerspective(T Object, TransformType cameraTransformType, Matrix4 perspective, @Nullable LivingEntity entity)
	{
		return perspective;
	}

	@OnlyIn(Dist.CLIENT)
	default int getRenderColour(T object, String group)
	{
		return 0xffffffff;
	}

	@OnlyIn(Dist.CLIENT)
	default List<BakedQuad> modifyQuads(T object, List<BakedQuad> quads)
	{
		return quads;
	}

	@OnlyIn(Dist.CLIENT)
	default String getCacheKey(T object)
	{
		return null;
	}

	String[][] EMPTY_STRING_A = new String[0][];

	@OnlyIn(Dist.CLIENT)
	default String[][] getSpecialGroups(ItemStack stack, TransformType transform, LivingEntity entity)
	{
		return IOBJModelCallback.EMPTY_STRING_A;
	}

	@OnlyIn(Dist.CLIENT)
	@Nonnull
	default Matrix4 getTransformForGroups(ItemStack stack, String[] groups, TransformType transform, LivingEntity entity,
										  Matrix4 mat, float partialTicks)
	{
		return mat.setIdentity();
	}

	@OnlyIn(Dist.CLIENT)
	default boolean areGroupsFullbright(ItemStack stack, String[] groups)
	{
		return false;
	}
}