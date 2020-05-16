package blusunrize.immersiveengineering.api.shader;

import com.google.common.base.Preconditions;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.client.renderer.model.Material;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.ResourceLocation;

public class ShaderLayer
{
	/**
	 * A resource location pointing to a texture on the sheet
	 */
	private final Material texture;
	/**
	 * An ARGB formatted colour
	 */
	private final Vector4f color;
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
		this(new Material(PlayerContainer.LOCATION_BLOCKS_TEXTURE, texture), color);
	}

	public ShaderLayer(Material texture, int color)
	{
		this.texture = texture;
		this.color = new Vector4f((color >> 16&255)/255f, (color >> 8&255)/255f, (color&255)/255f, (color >> 24&255)/255f);
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

	public Material getTexture()
	{
		return texture;
	}

	public Vector4f getColor()
	{
		return color;
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
