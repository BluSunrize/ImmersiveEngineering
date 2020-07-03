/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("deprecation")
//T must be ItemStack for Items or IBlockState for TileEntities implementing this
public interface IOBJModelCallback<T>
{
	ModelProperty<IOBJModelCallback> PROPERTY = new ModelProperty<>();

	@OnlyIn(Dist.CLIENT)
	default TextureAtlasSprite getTextureReplacement(T object, String group, String material)
	{
		return null;
	}

	@OnlyIn(Dist.CLIENT)
	default boolean shouldRenderGroup(T object, String group)
	{
		return true;
	}

	default VisibilityList getVisibility(Collection<String> allGroups, T object)
	{
		List<String> visible = new ArrayList<>();
		for(String g : allGroups)
			if(shouldRenderGroup(object, g))
				visible.add(g);
		return VisibilityList.show(visible);
	}

	@OnlyIn(Dist.CLIENT)
	default TransformationMatrix applyTransformations(T object, String group, TransformationMatrix transform)
	{
		return transform;
	}

	@OnlyIn(Dist.CLIENT)
	default void handlePerspective(T Object, TransformType cameraTransformType, MatrixStack mat, @Nullable LivingEntity entity)
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
	default TransformationMatrix getTransformForGroups(ItemStack stack, String[] groups, TransformType transform, LivingEntity entity,
													   float partialTicks)
	{
		return TransformationMatrix.identity();
	}

	@OnlyIn(Dist.CLIENT)
	default boolean areGroupsFullbright(ItemStack stack, String[] groups)
	{
		return false;
	}
}