package blusunrize.immersiveengineering.api.shader;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ShaderCaseDrill extends ShaderCase
{
	public String additionalTexture = null;

	public ShaderCaseDrill(String overlayType, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, String additionalTexture)
	{
		super(overlayType, colourGrip,colourPrimary,colourSecondary, "immersiveengineering:shaders/drill_diesel_");
		this.additionalTexture = additionalTexture;
	}

	@Override
	public String getShaderType()
	{
		return "drill";
	}


	@Override
	public int getPasses(ItemStack shader, ItemStack item, String modelPart)
	{
		if(modelPart.equals("upgrade_speed")||modelPart.equals("upgrade_waterproof"))
			return 2;
		int i = additionalTexture!=null?1:0;
		return 3+i;
	}

	@Override
	public IIcon getReplacementIcon(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		int maxPass = getPasses(shader, item, modelPart);
		if(pass==maxPass-1)//uncoloured
			return i_drillUncoloured;
		if(pass==maxPass-2 && i_drillAdditional!=null)
			return i_drillAdditional;
		
		return pass==0?i_drillBase: i_drillOverlay;
	}

	@Override
	public int[] getRGBAColourModifier(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		int maxPass = getPasses(shader, item, modelPart);
		if((pass==maxPass-1)||(pass==maxPass-2 && i_drillAdditional!=null))
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
		if(modelPart.equals("drill_grip"))
			return pass==0?0:pass+1;
		return pass+1;
	}

	public IIcon i_drillBase;
	public IIcon i_drillOverlay;
	public IIcon i_drillUncoloured;
	public IIcon i_drillAdditional;
	@Override
	public void stichTextures(IIconRegister ir, int sheetID)
	{
		if(sheetID==1)
		{
			i_drillBase = ir.registerIcon("immersiveengineering:shaders/drill_diesel_0");
			i_drillOverlay = ir.registerIcon(this.baseTexturePath+"1_"+this.overlayType);
			i_drillUncoloured = ir.registerIcon("immersiveengineering:shaders/drill_diesel_uncoloured");
			if(this.additionalTexture!=null)
				i_drillAdditional = ir.registerIcon(this.baseTexturePath+additionalTexture);
		}
	}

	@Override
	public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre, boolean inventory)
	{
	}
}