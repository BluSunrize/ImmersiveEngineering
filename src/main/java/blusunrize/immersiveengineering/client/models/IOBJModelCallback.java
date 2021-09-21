/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("deprecation")
//T must be ItemStack for Items or IBlockState for TileEntities implementing this
public interface IOBJModelCallback<T>
{
	ModelProperty<IOBJModelCallback> PROPERTY = new ModelProperty<>();

	@OnlyIn(Dist.CLIENT)
	@Nullable
	default TextureAtlasSprite getTextureReplacement(T object, String group, String material)
	{
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	default boolean shouldRenderGroup(T object, String group)
	{
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	default Transformation applyTransformations(T object, String group, Transformation transform)
	{
		return transform;
	}

	@OnlyIn(Dist.CLIENT)
	default void handlePerspective(T Object, TransformType cameraTransformType, PoseStack mat, @Nullable LivingEntity entity)
	{

	}

	@OnlyIn(Dist.CLIENT)
	default Vector4f getRenderColor(T object, String group, Vector4f original)
	{
		return original;
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
	default Transformation getTransformForGroups(ItemStack stack, String[] groups, TransformType transform, LivingEntity entity,
													   float partialTicks)
	{
		return Transformation.identity();
	}

	@OnlyIn(Dist.CLIENT)
	default boolean areGroupsFullbright(ItemStack stack, String[] groups)
	{
		return false;
	}
}