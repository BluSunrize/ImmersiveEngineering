/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.shader;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

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
	public abstract boolean renderModelPartForPass(ItemStack shader, ItemStack item, String modelPart, int pass);

	/**
	 * @return A string representing which item this shader case applies to. e.g.: "immersiveengineering:revolver"
	 */
	public abstract String getShaderType();

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
	public ResourceLocation getReplacementSprite(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		return getLayers()[pass].getTexture();
	}

	/**
	 * @return the ARGB values to be appleid to the given part in the given pass
	 */
	public int getARGBColourModifier(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		return getLayers()[pass].getColour();
	}

	/**
	 * DEPRECATED. WILL BE REMOVED IN 1.13
	 *
	 * @param pre indicates whether this is before or after the part was rendered
	 * @return make specific changes to the render, like GL calls
	 */
	@Deprecated
	public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre, boolean inventory)
	{
	}

	public static class ShaderLayer
	{
		/**
		 * A resource location pointing to a texture on the sheet
		 */
		private final ResourceLocation texture;
		/**
		 * An ARGB formatted colour
		 */
		private final int colour;
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

		public ShaderLayer(ResourceLocation texture, int colour)
		{
			this.texture = texture;
			this.colour = colour;
			if(ShaderRegistry.defaultLayerBounds.containsKey(texture))
				this.setTextureBounds(ShaderRegistry.defaultLayerBounds.get(texture));
		}

		public ShaderLayer setTextureBounds(double... bounds)
		{
			if(bounds==null)
				return this;
			assert (bounds.length==4);
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
			assert (bounds.length==4);
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

		public int getColour()
		{
			return colour;
		}

		/**
		 * @return if this layer is dynamic and should be excluded from batched rendering
		 */
		public boolean isDynamicLayer()
		{
			return false;
		}

		/**
		 * modify the render, provided that the layer is flagged as dynamic
		 */
		public void modifyRender(boolean pre, float partialTick)
		{
		}
	}

	public static class DynamicShaderLayer extends ShaderLayer
	{
		public DynamicShaderLayer(ResourceLocation texture, int colour)
		{
			super(texture, colour);
		}

		@Override
		public boolean isDynamicLayer()
		{
			return true;
		}
	}
}