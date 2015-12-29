package blusunrize.immersiveengineering.api.shader;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ShaderCaseMinecart extends ShaderCase
{
	public static Set<Class<? extends EntityMinecart>> invalidMinecartClasses = new HashSet();
	public String additionalTexture = null;
	public boolean[] overlaySides = {true, true,true,true,true, true,true};
	public boolean[] mirrorSideForPass = {true,true,true,true};

	public ShaderCaseMinecart(String overlayType, int[] colourPrimary, int[] colourSecondary, String additionalTexture)
	{
		super(overlayType, defaultWhite,colourPrimary,colourSecondary, "immersiveengineering:textures/models/shaders/minecart_");
		this.additionalTexture = additionalTexture;
		if(overlayType=="1" || overlayType=="2" || overlayType=="7")
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
	public IIcon getReplacementIcon(ItemStack shader, ItemStack item, String modelPart, int pass)
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
	public void stichTextures(IIconRegister ir, int sheetID)
	{
	}

	@Override
	public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre, boolean inventory)
	{
	}
}