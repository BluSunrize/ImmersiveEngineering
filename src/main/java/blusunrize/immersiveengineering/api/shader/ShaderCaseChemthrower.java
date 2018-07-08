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

public class ShaderCaseChemthrower extends ShaderCase
{
	public boolean renderCageOnBase = true;
	public boolean tanksUncoloured = false;

	public ShaderCaseChemthrower(ShaderLayer... layers)
	{
		super(layers);
	}

	public ShaderCaseChemthrower(Collection<ShaderLayer> layers)
	{
		super(layers);
	}

	@Override
	public String getShaderType()
	{
		return "immersiveengineering:chemthrower";
	}

	@Override
	public int getLayerInsertionIndex()
	{
		return layers.length-1;
	}

	@Override
	public boolean renderModelPartForPass(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		if("grip".equals(modelPart))
			return pass==0;
		if(pass==0)//first pass is just for the grip
			return false;

		if(pass==1&&"cage".equals(modelPart))//useful for a cage that is different from the base one
			return renderCageOnBase;

		if(tanksUncoloured&&"tanks".equals(modelPart))//if tanks should be unaffected by colour and only render on overlay
			return pass==getLayers().length-1;
		if(!tanksUncoloured&&"tanks".equals(modelPart))//if tanks should be coloured, the uncoloured pass must be ignored
			return pass!=getLayers().length-1;

		return true;
	}

//	@Override
//	public int getPasses(ItemStack shader, ItemStack item, String modelPart)
//	{
//		int i = additionalTexture!=null?1:0;
//		if(modelPart.equals("cage"))
//			return (renderCageOnBase?2:1)+i;
//		if(modelPart.equals("base"))
//			return 3+i;
//		if()
//			return (tanksUncoloured?1:2)+i;
//		return 2+i;
//	}
//
//	@Override
//	public TextureAtlasSprite getReplacementSprite(ItemStack shader, ItemStack item, String modelPart, int pass)
//	{
//		int maxPass = getPasses(shader, item, modelPart);
//		boolean hasUncoloured = modelPart.equals("base") || (tanksUncoloured&&modelPart.equals("tanks"));
//		if(pass==maxPass-1 && hasUncoloured)//uncoloured
//			return i_chemthrowerUncoloured;
//		if(pass==maxPass-(hasUncoloured?2:1) && i_chemthrowerAdditional!=null)
//			return i_chemthrowerAdditional;
//
//		if(modelPart.equals("cage"))
//			return pass==0&&renderCageOnBase?i_chemthrowerBase:i_chemthrowerOverlay;
//		return pass==0?i_chemthrowerBase: i_chemthrowerOverlay;
//	}
//
//	@Override
//	public int[] getRGBAColourModifier(ItemStack shader, ItemStack item, String modelPart, int pass)
//	{
//		int maxPass = getPasses(shader, item, modelPart);
//		boolean hasUncoloured = modelPart.equals("base") || (tanksUncoloured&&modelPart.equals("tanks"));
//		if(pass==maxPass-1 && hasUncoloured)//uncoloured
//			return defaultWhite;
//		if(pass==maxPass-(hasUncoloured?2:1) && i_chemthrowerAdditional!=null)
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
//		if(modelPart.equals("grip"))
//			return pass==0?0:pass+1;
//		return pass+1;
//	}
//
//	public TextureAtlasSprite i_chemthrowerBase;
//	public TextureAtlasSprite i_chemthrowerOverlay;
//	public TextureAtlasSprite i_chemthrowerUncoloured;
//	public TextureAtlasSprite i_chemthrowerAdditional;
//	@Override
//	public void stichTextures(TextureMap map, int sheetID)
//	{
//		i_chemthrowerBase = ApiUtils.getRegisterSprite(map, "immersiveengineering:items/shaders/chemthrower_0");
//		i_chemthrowerOverlay = ApiUtils.getRegisterSprite(map, this.baseTexturePath+"1_"+this.overlayType);
//		i_chemthrowerUncoloured = ApiUtils.getRegisterSprite(map, "immersiveengineering:items/shaders/chemthrower_uncoloured");
//		if(this.additionalTexture!=null)
//			i_chemthrowerAdditional = ApiUtils.getRegisterSprite(map, this.baseTexturePath+additionalTexture);
//	}
}