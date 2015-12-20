package blusunrize.immersiveengineering.api.shader;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.IEApi;

public class ShaderCaseRevolver extends ShaderCase
{
	public int[] colourBlade = new int[4];
	public String additionalTexture = null;
	public int glowLayer = -1;

	public ShaderCaseRevolver(String overlayType, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, int[] colourBlade, String additionalTexture)
	{
		super(overlayType, colourGrip,colourPrimary,colourSecondary);
		this.colourBlade = colourBlade;
		this.additionalTexture = additionalTexture;
	}
	
	@Override
	public String getShaderType()
	{
		return "revolver";
	}

	@Override
	public int getPasses(ItemStack shader, ItemStack item, String modelPart)
	{
		int i = additionalTexture!=null?1:0;
		if(modelPart.equals("cosmetic_compensator"))
			return 1+i;
		if(modelPart.equals("bayonet_attachment") || modelPart.equals("player_bayonet")||modelPart.equals("dev_bayonet") || modelPart.equals("player_mag")||modelPart.equals("dev_mag"))
			return 2+i;
		return 3+i;
	}

	@Override
	public IIcon getReplacementIcon(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		int maxPass = getPasses(shader, item, modelPart);
		if(pass==maxPass-1 && i_revolverAdditional!=null)
			return i_revolverAdditional;

		switch(modelPart)
		{
		case "revolver_frame":
			return pass==0?i_revolverGrip: pass==1?i_revolverBase: i_revolverOverlay;
		case "barrel":
		case "dev_scope":
		case "player_mag":
		case "dev_mag":
		case "player_electro_0":
		case "player_electro_1":
			return pass==0?i_revolverBase: pass==1?i_revolverOverlay: i_revolverUncoloured;
		case "cosmetic_compensator":
			return i_revolverOverlay;
		case "bayonet_attachment":
			if(pass==0)
				return i_revolverGrip;
			return i_revolverOverlay;
		case "player_bayonet":
		case "dev_bayonet":
			return pass==0?i_revolverBase:i_revolverOverlay;
		}
		return i_revolverBase;
	}

	@Override
	public int[] getRGBAColourModifier(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		if(!shader.hasTagCompound() || pass==2&&(modelPart.equals("barrel") || modelPart.equals("dev_scope")||modelPart.equals("player_electro_0")||modelPart.equals("player_electro_1")))
			return defaultWhite;

		int i=getTextureType(modelPart,pass); //0 == grip, 1==main, 2==detail, 3==blade
		if(i==0)
//			return new int[]{40,40,50,220};
			return colourUnderlying;
		if(i==1)
//			return new int[]{26,26,40,220};
			return colourPrimary;
		if(i==2)
//			return new int[]{0,70,49,220};
			return colourSecondary;
		if(i==3)
//			return new int[]{5,10,8,180};
			return colourBlade;
		
		return defaultWhite;
//		0x000000,//BLACK
//		0x0000AA,//DARK_BLUE
//		0x00AA00,//DARK_GREEN
//		0x00AAAA,//DARK_AQUA
//		0xAA0000,//DARK_RED
//		0xAA00AA,//DARK_PURPLE
//		0xFFAA00,//GOLD
//		0xAAAAAA,//GRAY
//		0x555555,//DARK_GRAY
//		0x5555FF,//BLUE
//		0x55FF55,//GREEN
//		0x55FFFF,//AQUA
//		0xFF5555,//RED
//		0xFF55FF,//LIGHT_PURPLE
//		0xFFFF55,//YELLOW
//		0xFFFFFF//WHITE
	}
	
	public int getTextureType(String modelPart, int pass)
	{
		int i=0; //0 == grip, 1==main, 2==detail, 3==blade
		switch(modelPart)
		{
		case "revolver_frame":
			i=pass;
			break;
		case "barrel":
		case "dev_scope":
		case "player_mag":
		case "dev_mag":
		case "player_electro_0":
		case "player_electro_1":
			i=pass+1;
			break;
		case "cosmetic_compensator":
			i=2;
			break;
		case "bayonet_attachment":
			if(pass==1)
				i=2;
			break;
		case "player_bayonet":
		case "dev_bayonet":
			i=pass==1?2:3;
			break;
		}
		return i;
	}

	public IIcon i_revolverBase;
	public IIcon i_revolverOverlay;
	public IIcon i_revolverGrip;
	public IIcon i_revolverUncoloured;
	public IIcon i_revolverAdditional;
	@Override
	public void stichTextures(IIconRegister ir, int sheetID)
	{
		if(sheetID==IEApi.revolverTextureSheetID)
		{
			i_revolverBase = ir.registerIcon("immersiveengineering:shaders/revolver_0");
			i_revolverOverlay = ir.registerIcon("immersiveengineering:shaders/revolver_1_"+this.overlayType);
			i_revolverGrip = ir.registerIcon("immersiveengineering:shaders/revolver_grip");
			i_revolverUncoloured = ir.registerIcon("immersiveengineering:shaders/revolver_uncoloured");
			if(this.additionalTexture!=null)
				i_revolverAdditional = ir.registerIcon("immersiveengineering:shaders/revolver_"+additionalTexture);
		}
	}

	@Override
	public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre)
	{
		if(modelPart.equals("cosmetic_compensator"))
		{
			if(pre)
				GL11.glDisable(GL11.GL_CULL_FACE);
			else
				GL11.glEnable(GL11.GL_CULL_FACE);
		}
		
		if(glowLayer>-1 && (glowLayer&(getTextureType(modelPart,pass)+1))==1)
		{
			if(pre)
			{
				GL11.glDisable(GL11.GL_LIGHTING);
			Tessellator.instance.setBrightness(0xffffff);
			}
			else
				GL11.glEnable(GL11.GL_LIGHTING);
		}
	}
}