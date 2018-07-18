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

public class ShaderCaseRevolver extends ShaderCase
{
	public ShaderCaseRevolver(ShaderLayer... layers)
	{
		super(layers);
	}

	public ShaderCaseRevolver(Collection<ShaderLayer> layers)
	{
		super(layers);
	}

	@Override
	public String getShaderType()
	{
		return "immersiveengineering:revolver";
	}

	@Override
	public int getLayerInsertionIndex()
	{
		return layers.length-1;
	}

	@Override
	public boolean renderModelPartForPass(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		if(pass==0)//first pass is just for the grip
			return "frame".equals(modelPart)||"bayonet_attachment".equals(modelPart);
		if(pass==2)//third pass is just for the blade of the bayonet
			return "player_bayonet".equals(modelPart)||"dev_bayonet".equals(modelPart);
		return true;
	}

//	@Override
//	public int getPasses(ItemStack shader, ItemStack item, String modelPart)
//	{
//		int i = additionalTexture!=null?1:0;
//		if(modelPart.equals("cosmetic_compensator"))
//			return 1+i;
//		if(modelPart.equals("bayonet_attachment") || modelPart.equals("player_bayonet")||modelPart.equals("dev_bayonet") || modelPart.equals("player_mag")||modelPart.equals("dev_mag"))
//			return 2+i;
//		return 3+i;
//	}
//
//	@Override
//	public TextureAtlasSprite getReplacementSprite(ItemStack shader, ItemStack item, String modelPart, int pass)
//	{
//		int maxPass = getPasses(shader, item, modelPart);
//
//		boolean hasUncoloured = modelPart.equals("barrel")||modelPart.equals("dev_scope")||modelPart.equals("player_mag")||modelPart.equals("dev_mag")||modelPart.equals("player_electro_0")||modelPart.equals("player_electro_1");
//		if(pass==maxPass-1 && hasUncoloured)//uncoloured
//			return i_revolverUncoloured;
//		if(pass==maxPass-(hasUncoloured?2:1) && i_revolverAdditional!=null)
//			return i_revolverAdditional;
//
//		switch(modelPart)
//		{
//		case "revolver_frame":
//			return pass==0?i_revolverGrip: pass==1?i_revolverBase: i_revolverOverlay;
//		case "barrel":
//		case "dev_scope":
//		case "player_mag":
//		case "dev_mag":
//		case "player_electro_0":
//		case "player_electro_1":
//		case "player_bayonet":
//		case "dev_bayonet":
//			return pass==0?i_revolverBase: i_revolverOverlay;
//		case "bayonet_attachment":
//			return pass==0?i_revolverGrip: i_revolverOverlay;
//		case "cosmetic_compensator":
//			return i_revolverOverlay;
//		}
//		return i_revolverBase;
//	}
//
//	@Override
//	public int[] getRGBAColourModifier(ItemStack shader, ItemStack item, String modelPart, int pass)
//	{
//		int maxPass = getPasses(shader, item, modelPart);
//		boolean hasUncoloured = modelPart.equals("barrel")||modelPart.equals("dev_scope")||modelPart.equals("player_mag")||modelPart.equals("dev_mag")||modelPart.equals("player_electro_0")||modelPart.equals("player_electro_1");
//		if(hasUncoloured&&pass==maxPass-1)
//			return defaultWhite;
//		if(pass==maxPass-(hasUncoloured?2:1) && i_revolverAdditional!=null)
//			return colourOverlay;
//
//		int i=getTextureType(modelPart,pass); //0 == grip, 1==main, 2==detail, 3==blade
//		if(i==0)
//			return colourUnderlying;
//		if(i==1)
//			return colourPrimary;
//		if(i==2)
//			return colourSecondary;
//		if(i==3)
//			return colourBlade;
//
//		return defaultWhite;
//	}
//
//	public int getTextureType(String modelPart, int pass)
//	{
//		int i=0; //0 == grip, 1==main, 2==detail, 3==blade
//		switch(modelPart)
//		{
//		case "revolver_frame":
//			i=pass;
//			break;
//		case "barrel":
//		case "dev_scope":
//		case "player_mag":
//		case "dev_mag":
//		case "player_electro_0":
//		case "player_electro_1":
//			i=pass+1;
//			break;
//		case "cosmetic_compensator":
//			i=2;
//			break;
//		case "bayonet_attachment":
//			if(pass==1)
//				i=2;
//			break;
//		case "player_bayonet":
//		case "dev_bayonet":
//			i=pass==1?2:3;
//			break;
//		}
//		return i;
//	}
//
//	public TextureAtlasSprite i_revolverBase;
//	public TextureAtlasSprite i_revolverOverlay;
//	public TextureAtlasSprite i_revolverGrip;
//	public TextureAtlasSprite i_revolverUncoloured;
//	public TextureAtlasSprite i_revolverAdditional;
//	@Override
//	public void stichTextures(TextureMap map, int sheetID)
//	{
//		//		i_revolverBase = map.registerSprite(new ResourceLocation("immersiveengineering:revolvers/shaders/revolver_0"));
//		//		i_revolverOverlay = map.registerSprite(new ResourceLocation(this.baseTexturePath+"1_"+this.overlayType));
//		//		i_revolverGrip = map.registerSprite(new ResourceLocation("immersiveengineering:revolvers/shaders/revolver_grip"));
//		//		i_revolverUncoloured = map.registerSprite(new ResourceLocation("immersiveengineering:revolvers/shaders/revolver_uncoloured"));
//		//		if(this.additionalTexture!=null)
//		//			i_revolverAdditional = map.registerSprite(new ResourceLocation(this.baseTexturePath+additionalTexture));
//
//		i_revolverBase = ApiUtils.getRegisterSprite(map, "immersiveengineering:revolvers/shaders/revolver_0");
//		i_revolverOverlay = ApiUtils.getRegisterSprite(map, this.baseTexturePath+"1_"+this.overlayType);
//		i_revolverGrip = ApiUtils.getRegisterSprite(map, "immersiveengineering:revolvers/shaders/revolver_grip");
//		i_revolverUncoloured = ApiUtils.getRegisterSprite(map, "immersiveengineering:revolvers/shaders/revolver_uncoloured");
//		if(this.additionalTexture!=null)
//			i_revolverAdditional = ApiUtils.getRegisterSprite(map, this.baseTexturePath+additionalTexture);
//	}
}