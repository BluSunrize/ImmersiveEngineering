/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.shader;

import com.mojang.math.Vector4f;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

/**
 * @author BluSunrize - 29.10.2015
 * Completely rewritten 29.10.2016
 * <p>
 * To be extended for ShaderCases all items, entities and whatever else you use them for<br>
 * Pre-configured ones exist (ShaderCaseRevolver.class) but when a new, shader-ready thing is implemented, it'll need a shadercase.
 */
public abstract class ShaderCase
{
	/**
	 * An array of layers that this shader is comprised of.
	 */
	protected ShaderLayer[] layers;

	protected ShaderCase(ShaderLayer... layers)
	{
		this.layers = layers;
	}

	protected ShaderCase(Collection<ShaderLayer> layers)
	{
		this.layers = layers.toArray(new ShaderLayer[layers.size()]);
	}

	public ShaderLayer[] getLayers()
	{
		return this.layers;
	}

	public ShaderCase addLayers(ShaderLayer... addedLayers)
	{
		addLayers(getLayerInsertionIndex(), addedLayers);
		return this;
	}

	public ShaderCase addLayers(int index, ShaderLayer... addedLayers)
	{
		ShaderLayer[] newLayers = new ShaderLayer[layers.length+addedLayers.length];
		System.arraycopy(this.layers, 0, newLayers, 0, index);
		System.arraycopy(addedLayers, 0, newLayers, index, addedLayers.length);
		System.arraycopy(this.layers, index, newLayers, index+addedLayers.length, this.layers.length-index);
		this.layers = newLayers;
		return this;
	}

	public abstract int getLayerInsertionIndex();

	/**
	 * @return if the given part of the model renders on the pass
	 */
	public abstract boolean shouldRenderGroupForPass(String modelPart, int pass);

	/**
	 * @return A string representing which item this shader case applies to. e.g.: "immersiveengineering:revolver"
	 */
	public abstract ResourceLocation getShaderType();

	/**
	 * @return if the ResourceLocations of the layers should be stitched into the main texturemap<br>
	 * defaults to true, since item and block textures are generally handled by that map<br
	 * Minecarts return false here
	 */
	public boolean stitchIntoSheet()
	{
		return true;
	}

	/**
	 * @return which icon is to be used for the given pass and model part. These obviously need to be stitched on the given sheet (mind the revolvers!)
	 */
	public ResourceLocation getTextureReplacement(String modelPart, int pass)
	{
		return getLayers()[pass].getTexture();
	}

	/**
	 * @return the ARGB values to be appleid to the given part in the given pass
	 */
	public Vector4f getRenderColor(String modelPart, int pass, Vector4f original)
	{
		return getLayers()[pass].getColor();
	}
}