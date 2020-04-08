/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.shader.impl;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;

public class ShaderCaseBuzzsaw extends ShaderCase
{
	private int bladeLayers = 1;

	public ShaderCaseBuzzsaw(ShaderLayer... layers)
	{
		super(layers);
	}

	public ShaderCaseBuzzsaw(Collection<ShaderLayer> layers)
	{
		super(layers);
	}

	@Override
	public ResourceLocation getShaderType()
	{
		return new ResourceLocation(ImmersiveEngineering.MODID, "buzzsaw");
	}

	@Override
	public int getLayerInsertionIndex()
	{
		return layers.length-1;
	}

	@Override
	public boolean shouldRenderGroupForPass(String modelPart, int pass)
	{
		if("blade".equals(modelPart)||"upgrade_blades1".equals(modelPart)||"upgrade_blades2".equals(modelPart))
			return pass >= getLayers().length-bladeLayers;
		if(pass >= getLayers().length-bladeLayers)//Last pass on the buzzsaw is for the blade
			return false;
		if("upgrade_lube".equals(modelPart))//Upgrades only render on the uncoloured pass
			return pass==getLayers().length-2;

		if("grip".equals(modelPart))
			return pass==0;
		return pass!=0;

	}

	public ShaderCaseBuzzsaw addHeadLayers(ShaderLayer... addedLayers)
	{
		addLayers(layers.length, addedLayers);
		bladeLayers += addedLayers.length;
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
//		i_drillBase = ApiUtils.getRegisterSprite(map, "immersiveengineering:item/shaders/drill_diesel_0");
//		i_drillOverlay = ApiUtils.getRegisterSprite(map, this.baseTexturePath+"1_"+this.overlayType);
//		i_drillUncoloured = ApiUtils.getRegisterSprite(map, "immersiveengineering:item/shaders/drill_diesel_uncoloured");
//		if(this.additionalTexture!=null)
//			i_drillAdditional = ApiUtils.getRegisterSprite(map, this.baseTexturePath+additionalTexture);
//	}
}