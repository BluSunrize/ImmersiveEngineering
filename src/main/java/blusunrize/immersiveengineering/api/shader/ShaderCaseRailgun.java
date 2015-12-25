package blusunrize.immersiveengineering.api.shader;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ShaderCaseRailgun extends ShaderCase
{
	public String additionalTexture = null;

	public ShaderCaseRailgun(String overlayType, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, String additionalTexture)
	{
		super(overlayType, colourGrip,colourPrimary,colourSecondary, "immersiveengineering:shaders/railgun_");
		this.additionalTexture = additionalTexture;
	}

	@Override
	public String getShaderType()
	{
		return "railgun";
	}


	@Override
	public int getPasses(ItemStack shader, ItemStack item, String modelPart)
	{
		if(modelPart.equals("sled")||modelPart.equals("wires"))
			return 1;
		boolean hasUncoloured = modelPart.equals("barrel")||modelPart.equals("upgrade_scope");
		return 2+(additionalTexture!=null?1:0)+(hasUncoloured?1:0);
	}

	@Override
	public IIcon getReplacementIcon(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		int maxPass = getPasses(shader, item, modelPart);
		boolean hasUncoloured = modelPart.equals("sled")||modelPart.equals("wires")||modelPart.equals("barrel")||modelPart.equals("upgrade_scope");
		if(hasUncoloured && pass==maxPass-1)//uncoloured
			return i_railgunUncoloured;
		if(pass==maxPass-(hasUncoloured?2:1) && i_railgunAdditional!=null)
			return i_railgunAdditional;

		return pass==0?i_railgunBase: i_railgunOverlay;
	}

	@Override
	public int[] getRGBAColourModifier(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		int maxPass = getPasses(shader, item, modelPart);
		boolean hasUncoloured = modelPart.equals("sled")||modelPart.equals("wires")||modelPart.equals("barrel")||modelPart.equals("upgrade_scope");
		if((hasUncoloured&&pass==maxPass-1)||(pass==maxPass-(hasUncoloured?2:1) && i_railgunAdditional!=null))
			return defaultWhite;

		int i=getTextureType(modelPart,pass); //0 == grip, 1==main, 2==detail
		if(i==0)
			return colourUnderlying;
		if(i==1)
			return colourPrimary;
		if(i==2)
			return colourSecondary;
		return defaultWhite;
	}

	public int getTextureType(String modelPart, int pass)
	{
		//0 == grip, 1==main, 2==detail
		if(modelPart.equals("grip"))
			return pass==0?0:pass+1;
		return pass+1;
	}

	public IIcon i_railgunBase;
	public IIcon i_railgunOverlay;
	public IIcon i_railgunUncoloured;
	public IIcon i_railgunAdditional;
	@Override
	public void stichTextures(IIconRegister ir, int sheetID)
	{
		if(sheetID==1)
		{
			i_railgunBase = ir.registerIcon("immersiveengineering:shaders/railgun_0");
			i_railgunOverlay = ir.registerIcon(this.baseTexturePath+"1_"+this.overlayType);
			i_railgunUncoloured = ir.registerIcon("immersiveengineering:shaders/railgun_uncoloured");
			if(this.additionalTexture!=null)
				i_railgunAdditional = ir.registerIcon(this.baseTexturePath+additionalTexture);
		}
	}

	@Override
	public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre, boolean inventory)
	{
	}
}