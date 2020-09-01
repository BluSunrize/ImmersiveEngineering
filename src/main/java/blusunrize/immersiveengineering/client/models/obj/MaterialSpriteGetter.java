/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

import java.util.function.BiFunction;
import java.util.function.Function;

public class MaterialSpriteGetter<T> implements BiFunction<String, RenderMaterial, TextureAtlasSprite>
{
	private final Function<RenderMaterial, TextureAtlasSprite> getter;
	private final String groupName;
	private final IOBJModelCallback<T> callback;
	private final T callbackObject;
	private final ShaderCase shaderCase;

	private int renderPass = 0;

	public MaterialSpriteGetter(Function<RenderMaterial, TextureAtlasSprite> getter, String groupName,
								IOBJModelCallback<T> callback, T callbackObject, ShaderCase shaderCase)
	{
		this.getter = getter;
		this.groupName = groupName;
		this.callback = callback;
		this.callbackObject = callbackObject;
		this.shaderCase = shaderCase;
	}

	/**
	 * Set the renderpass for use by the shader case
	 *
	 * @param pass
	 */
	public void setRenderPass(int pass)
	{
		this.renderPass = pass;
	}

	@Override
	public TextureAtlasSprite apply(String material, RenderMaterial resourceLocation)
	{
		TextureAtlasSprite sprite = null;
		if(callback!=null)
			sprite = callback.getTextureReplacement(callbackObject, groupName, material);
		if(shaderCase!=null)
		{
			ResourceLocation rl = shaderCase.getTextureReplacement(groupName, renderPass);
			if(rl!=null)
				sprite = getter.apply(new RenderMaterial(PlayerContainer.LOCATION_BLOCKS_TEXTURE, rl));
		}
		if(sprite==null)
			sprite = getter.apply(resourceLocation);
		return sprite;
	}
}
