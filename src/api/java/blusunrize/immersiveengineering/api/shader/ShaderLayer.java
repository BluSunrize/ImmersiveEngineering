/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.shader;

import com.google.common.base.Preconditions;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ShaderLayer
{
	/**
	 * A resource location pointing to a texture on the sheet
	 */
	private final ResourceLocation texture;
	/**
	 * An ARGB formatted colour
	 */
	private final int color;
	/**
	 * An optional double array (uMin, vMin, uMax, vMax; values of 0-1) to define which part of the original texture is overriden<br>
	 * The model will then only render faces who's coords lie within that limited space.<br>
	 * Useful for keeping additional decorations (see Sponsor Shader) in smaller textures
	 */
	private double[] textureBounds;
	/**
	 * An optional double array (uMin, vMin, uMax, vMax; values of 0-1) for the parts of the texture to be used.<br>
	 * Useful when putting multiple shader textures into one file
	 */
	private double[] cutoutBounds;

	public ShaderLayer(ResourceLocation texture, int color)
	{
		this.texture = texture;
		this.color = color;
		if(ShaderRegistry.defaultLayerBounds.containsKey(texture))
			this.setTextureBounds(ShaderRegistry.defaultLayerBounds.get(texture));
	}

	public ShaderLayer setTextureBounds(double... bounds)
	{
		if(bounds==null)
			return this;
		Preconditions.checkArgument(bounds.length==4);
		this.textureBounds = bounds;
		return this;
	}

	/**
	 * @return An optional double array (uMin, vMin, uMax, vMax; values of 0-1) to define which part of the original texture is overriden<br>
	 * The model will then only render faces who's coords lie within that limited space.<br>
	 * Useful for keeping additional decorations (see Sponsor Shader) in smaller textures
	 */
	public double[] getTextureBounds()
	{
		return this.textureBounds;
	}

	public ShaderLayer setCutoutBounds(double... bounds)
	{
		if(bounds==null)
			return this;
		Preconditions.checkArgument(bounds.length==4);
		this.cutoutBounds = bounds;
		return this;
	}

	/**
	 * @return An optional double array (uMin, vMin, uMax, vMax; values of 0-1) for the parts of the texture to be used.<br>
	 * Useful when putting multiple shader textures into one file
	 */
	public double[] getCutoutBounds()
	{
		return this.cutoutBounds;
	}

	public ResourceLocation getTexture()
	{
		return texture;
	}

	@OnlyIn(Dist.CLIENT)
	public Vector4f getColor()
	{
		return new Vector4f((color >> 16&255)/255f, (color >> 8&255)/255f, (color&255)/255f, (color >> 24&255)/255f);
	}

	/**
	 * @return if this layer is dynamic and should be excluded from batched rendering
	 */
	public boolean isDynamicLayer()
	{
		return false;
	}

	public RenderType getRenderType(RenderType baseType)
	{
		return baseType;
	}

	public boolean isTranslucent()
	{
		return false;
	}
}
