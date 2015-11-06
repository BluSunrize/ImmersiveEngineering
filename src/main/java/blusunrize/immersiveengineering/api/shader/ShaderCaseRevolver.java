package blusunrize.immersiveengineering.api.shader;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.IEApi;

public class ShaderCaseRevolver extends ShaderCase
{
	public int overlayType=0;
	public int[] colourBlade = new int[4];
	public String additionalTexture = null;
	public int glowLayer = -1;

	public ShaderCaseRevolver(int overlayType, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, int[] colourBlade, String additionalTexture)
	{
		super(colourGrip,colourPrimary,colourSecondary);
		this.colourBlade = colourBlade;
		this.overlayType = overlayType;
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
			return new int[]{255,255,255,255};

		int i=getTextureType(modelPart,pass); //0 == grip, 1==main, 2==detail, 3==blade
		if(i==0)
			return colourUnderlying;
		if(i==1)
			return colourPrimary;
		if(i==2)
			return colourSecondary;
		if(i==3)
			return colourBlade;
		return new int[]{255,255,255,255};
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
			i_revolverUncoloured = ir.registerIcon("immersiveengineering:shaders/revolver_noColour");
			if(this.additionalTexture!=null)
				i_revolverAdditional = ir.registerIcon(additionalTexture);
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