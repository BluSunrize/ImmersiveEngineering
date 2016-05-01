package blusunrize.immersiveengineering.api.shader;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;

public class ShaderCaseDrill extends ShaderCase
{
	public String additionalTexture = null;

	public ShaderCaseDrill(String overlayType, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, String additionalTexture)
	{
		super(overlayType, colourGrip,colourPrimary,colourSecondary, "immersiveengineering:items/shaders/drill_diesel_");
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
		if(modelPart.equals("drill_head")||modelPart.equals("upgrade_damage0")||modelPart.equals("upgrade_damage1")||modelPart.equals("upgrade_damage2")||modelPart.equals("upgrade_damage3")||modelPart.equals("upgrade_damage4"))
			return 1;
		if(modelPart.equals("upgrade_speed")||modelPart.equals("upgrade_waterproof"))
			return 2;
		int i = additionalTexture!=null?1:0;
		return 3+i;
	}

	@Override
	public TextureAtlasSprite getReplacementSprite(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		if(modelPart.equals("drill_head")||modelPart.equals("upgrade_damage0")||modelPart.equals("upgrade_damage1")||modelPart.equals("upgrade_damage2")||modelPart.equals("upgrade_damage3")||modelPart.equals("upgrade_damage4"))
			return null;
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
		if(pass==maxPass-1)
			return defaultWhite;
		if(pass==maxPass-2 && i_drillAdditional!=null)
			return colourOverlay;

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

	public TextureAtlasSprite i_drillBase;
	public TextureAtlasSprite i_drillOverlay;
	public TextureAtlasSprite i_drillUncoloured;
	public TextureAtlasSprite i_drillAdditional;
	@Override
	public void stichTextures(TextureMap map, int sheetID)
	{
		i_drillBase = ApiUtils.getRegisterSprite(map, "immersiveengineering:items/shaders/drill_diesel_0");
		i_drillOverlay = ApiUtils.getRegisterSprite(map, this.baseTexturePath+"1_"+this.overlayType);
		i_drillUncoloured = ApiUtils.getRegisterSprite(map, "immersiveengineering:items/shaders/drill_diesel_uncoloured");
		if(this.additionalTexture!=null)
			i_drillAdditional = ApiUtils.getRegisterSprite(map, this.baseTexturePath+additionalTexture);
	}

	@Override
	public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre, boolean inventory)
	{
	}
}