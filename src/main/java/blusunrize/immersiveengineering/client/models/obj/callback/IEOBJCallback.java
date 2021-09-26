/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import com.mojang.math.Transformation;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import javax.annotation.Nullable;
import java.util.List;

public interface IEOBJCallback<Key>
{
	@Nullable
	default TextureAtlasSprite getTextureReplacement(Key object, String group, String material)
	{
		return null;
	}

	default boolean shouldRenderGroup(Key object, String group)
	{
		return true;
	}

	default Transformation applyTransformations(Key object, String group, Transformation transform)
	{
		return transform;
	}

	default Vector4f getRenderColor(Key object, String group, Vector4f original)
	{
		return original;
	}

	default List<BakedQuad> modifyQuads(Key object, List<BakedQuad> quads)
	{
		return quads;
	}

	default IEObjState getIEOBJState(Key key)
	{
		return new IEObjState(VisibilityList.showAll());
	}
}
