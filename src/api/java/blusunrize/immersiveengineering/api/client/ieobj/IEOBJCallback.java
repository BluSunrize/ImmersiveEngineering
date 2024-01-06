/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.client.ieobj;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import com.mojang.math.Transformation;
import org.joml.Vector4f;
import net.minecraft.client.renderer.RenderType;
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

	default boolean useAbsoluteUV(Key object, String material)
	{
		return false;
	}

	default boolean shouldRenderGroup(Key object, String group, RenderType layer)
	{
		return true;
	}

	default Transformation applyTransformations(Key object, String group, Transformation transform)
	{
		return transform;
	}

	default Vector4f getRenderColor(Key object, String group, String material, ShaderCase shaderCase, Vector4f original)
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

	Key getDefaultKey();
}
