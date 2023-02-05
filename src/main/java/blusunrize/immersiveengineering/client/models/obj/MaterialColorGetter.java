/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj;

import blusunrize.immersiveengineering.api.client.ieobj.IEOBJCallback;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import org.joml.Vector4f;

import java.util.function.BiFunction;

public class MaterialColorGetter<T> implements BiFunction<String, Vector4f, Vector4f>
{
	private final String groupName;
	private final IEOBJCallback<T> callback;
	private final T callbackObject;
	private final ShaderCase shaderCase;

	private int renderPass = 0;

	public MaterialColorGetter(String groupName, IEOBJCallback<T> callback, T callbackObject, ShaderCase shaderCase)
	{
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
	public Vector4f apply(String material, Vector4f originalColor)
	{
		Vector4f color = originalColor;
		if(callback!=null)
			color = callback.getRenderColor(callbackObject, groupName, material, shaderCase, color);
		if(shaderCase!=null)
			color = shaderCase.getRenderColor(groupName, renderPass, color);
		return color;
	}
}
