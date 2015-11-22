package blusunrize.immersiveengineering.api.shader;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ShaderCaseChemthrower extends ShaderCase
{
	public int overlayType=0;
	public String additionalTexture = null;
	public int glowLayer = -1;
	public boolean renderCageOnBase = true;
	public boolean tanksUncoloured = true;

	public ShaderCaseChemthrower(int overlayType, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, boolean cageOnBase, boolean tanksUncoloured, String additionalTexture)
	{
		super(colourGrip,colourPrimary,colourSecondary);
		this.overlayType = overlayType;
		this.additionalTexture = additionalTexture;
		this.renderCageOnBase = cageOnBase;
		this.tanksUncoloured = tanksUncoloured;
	}

	@Override
	public String getShaderType()
	{
		return "chemthrower";
	}

	@Override
	public int getPasses(ItemStack shader, ItemStack item, String modelPart)
	{
		int i = additionalTexture!=null?1:0;
		if(modelPart.equals("cage"))
			return (renderCageOnBase?2:1)+i;
		if(modelPart.equals("base"))
			return 3+i;
		if(modelPart.equals("tanks"))
			return (tanksUncoloured?1:2)+i;
		return 2+i;
	}

	@Override
	public IIcon getReplacementIcon(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		int maxPass = getPasses(shader, item, modelPart);
		boolean hasUncoloured = modelPart.equals("base") || (tanksUncoloured&&modelPart.equals("tanks"));
		if(pass==maxPass-1 && hasUncoloured)//uncoloured
			return i_chemthrowerUncoloured;
		if(pass==maxPass-(hasUncoloured?2:1) && i_chemthrowerAdditional!=null)
			return i_chemthrowerAdditional;

		if(modelPart.equals("cage"))
			return pass==0&&renderCageOnBase?i_chemthrowerBase:i_chemthrowerOverlay;
		return pass==0?i_chemthrowerBase: i_chemthrowerOverlay;
	}

	@Override
	public int[] getRGBAColourModifier(ItemStack shader, ItemStack item, String modelPart, int pass)
	{
		int maxPass = getPasses(shader, item, modelPart);
		boolean hasUncoloured = modelPart.equals("base") || (tanksUncoloured&&modelPart.equals("tanks"));
		if(pass==maxPass-1 && hasUncoloured)//uncoloured
			return defaultWhite;
		if(pass==maxPass-(hasUncoloured?2:1) && i_chemthrowerAdditional!=null)
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

	public IIcon i_chemthrowerBase;
	public IIcon i_chemthrowerOverlay;
	public IIcon i_chemthrowerUncoloured;
	public IIcon i_chemthrowerAdditional;
	@Override
	public void stichTextures(IIconRegister ir, int sheetID)
	{
		if(sheetID==1)
		{
			i_chemthrowerBase = ir.registerIcon("immersiveengineering:shaders/chemthrower_0");
			i_chemthrowerOverlay = ir.registerIcon("immersiveengineering:shaders/chemthrower_1_"+this.overlayType);
			i_chemthrowerUncoloured = ir.registerIcon("immersiveengineering:shaders/chemthrower_uncoloured");
			if(this.additionalTexture!=null)
				i_chemthrowerAdditional = ir.registerIcon("immersiveengineering:shaders/chemthrower_"+additionalTexture);
		}
	}

	@Override
	public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre)
	{
	}
}