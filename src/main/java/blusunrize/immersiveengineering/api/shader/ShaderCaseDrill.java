/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.shader;

import net.minecraft.item.ItemStack;

import java.util.Collection;

public class ShaderCaseDrill extends ShaderCase
{
	private int headLayers = 1;

	public ShaderCaseDrill(ShaderLayer... layers)
	{
		super(layers);
	}

	public ShaderCaseDrill(Collection<ShaderLayer> layers)
	{
		super(layers);
	}

	@Override
	public String getShaderType()
	{
		return "immersiveengineering:drill";
	}

	@Override
	public int getLayerInsertionIndex()
	{
		return layers.length-1;
	}

	@Override
	public boolean renderModelPartForPass(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		if("drill_head".equals(modelPart)||"upgrade_damage0".equals(modelPart)||"upgrade_damage1".equals(modelPart)||"upgrade_damage2".equals(modelPart)||"upgrade_damage3".equals(modelPart)||"upgrade_damage4".equals(modelPart))
			return pass >= getLayers().length-headLayers;
		if(pass >= getLayers().length-headLayers)//Last pass on drills is just for the head and augers
			return false;
		if("upgrade_speed".equals(modelPart)||"upgrade_waterproof".equals(modelPart))//Upgrades only render on the uncoloured pass
			return pass==getLayers().length-2;

		if("drill_grip".equals(modelPart))
			return pass==0;
		return pass!=0;

	}

	public ShaderCaseDrill addHeadLayers(ShaderLayer... addedLayers)
	{
		addLayers(layers.length, addedLayers);
		headLayers += addedLayers.length;
		return this;
	}

//	@Override
//	public int getPasses(ItemStack shader, ItemStack item, String modelPart)
//	{
//		if()
//			return 1;
//
//			return 2;
//		int i = additionalTexture!=null?1:0;
//		return 3+i;
//	}
//
//	@Override
//	public TextureAtlasSprite getReplacementSprite(ItemStack shader, ItemStack item, String modelPart, int pass)
//	{
//		if(modelPart.equals("drill_head")||modelPart.equals("upgrade_damage0")||modelPart.equals("upgrade_damage1")||modelPart.equals("upgrade_damage2")||modelPart.equals("upgrade_damage3")||modelPart.equals("upgrade_damage4"))
//			return null;
//		int maxPass = getPasses(shader, item, modelPart);
//		if(pass==maxPass-1)//uncoloured
//			return i_drillUncoloured;
//		if(pass==maxPass-2 && i_drillAdditional!=null)
//			return i_drillAdditional;
//
//		return pass==0?i_drillBase: i_drillOverlay;
//	}
//
//	@Override
//	public int[] getRGBAColourModifier(ItemStack shader, ItemStack item, String modelPart, int pass)
//	{
//		int maxPass = getPasses(shader, item, modelPart);
//		if(pass==maxPass-1)
//			return defaultWhite;
//		if(pass==maxPass-2 && i_drillAdditional!=null)
//			return colourOverlay;
//
//		int i=getTextureType(modelPart,pass); //0 == grip, 1==main, 2==detail
//		if(i==0)
//			return colourUnderlying;
//		if(i==1)
//			return colourPrimary;
//		if(i==2)
//			return colourSecondary;
//		return defaultWhite;
//	}
//
//	public int getTextureType(String modelPart, int pass)
//	{
//		//0 == grip, 1==main, 2==detail
//		if(modelPart.equals("drill_grip"))
//			return pass==0?0:pass+1;
//		return pass+1;
//	}
//
//	public TextureAtlasSprite i_drillBase;
//	public TextureAtlasSprite i_drillOverlay;
//	public TextureAtlasSprite i_drillUncoloured;
//	public TextureAtlasSprite i_drillAdditional;
//	@Override
//	public void stichTextures(TextureMap map, int sheetID)
//	{
//		i_drillBase = ApiUtils.getRegisterSprite(map, "immersiveengineering:items/shaders/drill_diesel_0");
//		i_drillOverlay = ApiUtils.getRegisterSprite(map, this.baseTexturePath+"1_"+this.overlayType);
//		i_drillUncoloured = ApiUtils.getRegisterSprite(map, "immersiveengineering:items/shaders/drill_diesel_uncoloured");
//		if(this.additionalTexture!=null)
//			i_drillAdditional = ApiUtils.getRegisterSprite(map, this.baseTexturePath+additionalTexture);
//	}
}