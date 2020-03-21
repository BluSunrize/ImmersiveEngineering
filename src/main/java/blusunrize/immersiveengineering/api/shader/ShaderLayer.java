package blusunrize.immersiveengineering.api.shader;

import net.minecraft.util.ResourceLocation;

public class ShaderLayer
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

	public blusunrize.immersiveengineering.api.shader.ShaderLayer setTextureBounds(double... bounds)
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

	public blusunrize.immersiveengineering.api.shader.ShaderLayer setCutoutBounds(double... bounds)
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
