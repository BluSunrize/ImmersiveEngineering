package blusunrize.immersiveengineering.api.shader;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;

/**
 * @author BluSunrize - 29.10.2015
 *
 * To be implemented into new ShaderCases for new items<br>
 * Pre-configured ones exist (ShaderCaseRevolver.class) but when a new, shader-ready item is implemented, it'll need a shadercase.
 */
public abstract class ShaderCase
{
	protected final int[] colourUnderlying;
	protected final int[] colourPrimary;
	protected final int[] colourSecondary;
	protected int[] colourOverlay;
	protected static final int[] defaultWhite = {255,255,255,255};
	protected String overlayType="0";
	protected String baseTexturePath="";
	
	public ShaderCase(String overlayType, int[] colourUnderlying, int[] colourPrimary, int[] colourSecondary, String baseTexturePath)
	{
		this.overlayType = overlayType;
		this.colourUnderlying = colourUnderlying;
		this.colourPrimary = colourPrimary;
		this.colourSecondary = colourSecondary;
		this.colourOverlay = defaultWhite;
		this.baseTexturePath = baseTexturePath;
	}
	
	public int[] getUnderlyingColour()
	{
		return colourUnderlying;
	}
	public int[] getPrimaryColour()
	{
		return colourPrimary;
	}
	public int[] getSecondaryColour()
	{
		return colourSecondary;
	}
	public String getOverlayType()
	{
		return overlayType;
	}
	public String getBaseTexturePath()
	{
		return baseTexturePath;
	}
	public ShaderCase setBaseTexturePath(String path)
	{
		baseTexturePath = path;
		return this;
	}
	public ShaderCase setOverlayColour(int... col)
	{
		colourOverlay = col;
		return this;
	}
	
	/**
	 * @return A string representing which item this shader case applies to. e.g.: "revolver"
	 */
	public abstract String getShaderType();
	
	/**
	 * @return how many renderpasses are required for the part of the model
	 */
	public abstract int getPasses(ItemStack shader, ItemStack item, String modelPart);
	
	/**
	 * @return which icon is to be used for the given pass and model part. These obviously need to be stitched on the given sheet (mind the revolvers!)
	 */
	public abstract TextureAtlasSprite getReplacementSprite(ItemStack shader, ItemStack item, String modelPart, int pass);
	
	/**
	 * Called upon texutre stitching. Replacement icons are stitched from this method.<br>
	 * Make sure to compare against the sheetID, the revolver sheet ID can be found in IEApi.class
	 */
	public abstract void stichTextures(TextureMap map, int sheetID);
	
	/**
	 * @return the RGBA values to be appleid to the given part in the given pass
	 */
	public abstract int[] getRGBAColourModifier(ItemStack shader, ItemStack item, String modelPart, int pass);

	/**
	 * @param pre indicates whether this is before or after the part was rendered
	 * @return make specific changes to the render, like GL calls
	 */
	public abstract void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre, boolean inventory);
}