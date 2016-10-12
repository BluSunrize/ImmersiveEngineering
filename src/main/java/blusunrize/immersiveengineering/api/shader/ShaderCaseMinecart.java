package blusunrize.immersiveengineering.api.shader;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemStack;

import java.util.HashSet;
import java.util.Set;

public class ShaderCaseMinecart extends ShaderCase
{
	public static Set<Class<? extends EntityMinecart>> invalidMinecartClasses = new HashSet();
	public String additionalTexture = null;
	public boolean[] overlaySides = {true, true,true,true,true, true,true};
	public boolean[] mirrorSideForPass = {true,true,true,true};

	/**
	 * @param colourUnderlying is never used but is needed to colour the shader item
	 */
	public ShaderCaseMinecart(String overlayType, int colourUnderlying[], int[] colourPrimary, int[] colourSecondary, String additionalTexture)
	{
		super(overlayType, colourUnderlying,colourPrimary,colourSecondary, "immersiveengineering:textures/models/shaders/minecart_");
		this.additionalTexture = additionalTexture;
		if(overlayType.equals("1") || overlayType.equals("2") || overlayType.equals("7"))
		{
			overlaySides[1] = false;
			overlaySides[2] = false;
		}
	}

	@Override
	public String getShaderType()
	{
		return "minecart";
	}

	@Override
	public int getPasses(ItemStack shader, ItemStack item, String modelPart)
	{
		return additionalTexture!=null?4:3;
	}

	@Override
	public TextureAtlasSprite getReplacementSprite(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		return null;
	}

	@Override
	public int[] getRGBAColourModifier(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		if(pass==2 && additionalTexture!=null)
			return colourOverlay;

		if(pass==0)
			return colourPrimary;
		if(pass==1)
			return colourSecondary;
		return defaultWhite;
	}

	@Override
	public void stichTextures(TextureMap map, int sheetID)
	{
	}

	@Override
	public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre, boolean inventory)
	{
	}
}