//package blusunrize.immersiveengineering.api.shader;
//
//import blusunrize.immersiveengineering.api.ApiUtils;
//import net.minecraft.client.renderer.texture.TextureAtlasSprite;
//import net.minecraft.client.renderer.texture.TextureMap;
//import net.minecraft.item.ItemStack;
//
//public class ShaderCaseBalloon extends ShaderCase
//{
//	public String additionalTexture = null;
//
//	public ShaderCaseBalloon(String overlayType, int[] colourPrimary, int[] colourSecondary, String additionalTexture)
//	{
//		super(overlayType, defaultWhite,colourPrimary,colourSecondary, "immersiveengineering:blocks/shaders/balloon_");
//		this.additionalTexture = additionalTexture;
//	}
//
//	@Override
//	public String getShaderType()
//	{
//		return "balloon";
//	}
//
//	@Override
//	public int getPasses(ItemStack shader, ItemStack item, String modelPart)
//	{
//		return additionalTexture!=null?3:2;
//	}
//
//	@Override
//	public TextureAtlasSprite getReplacementSprite(ItemStack shader, ItemStack item, String modelPart, int pass)
//	{
//		return pass==2?i_balloonAdditional: pass==1?i_balloonOverlay: i_balloonBase;
//	}
//
//	@Override
//	public int[] getRGBAColourModifier(ItemStack shader, ItemStack item, String modelPart, int pass)
//	{
//		if(pass==2 && additionalTexture!=null)
//			return colourOverlay;
//		return pass==1?colourSecondary : colourPrimary;
//	}
//
//	public TextureAtlasSprite i_balloonBase;
//	public TextureAtlasSprite i_balloonOverlay;
//	public TextureAtlasSprite i_balloonAdditional;
//	@Override
//	public void stichTextures(TextureMap map, int sheetID)
//	{
//		i_balloonBase = ApiUtils.getRegisterSprite(map, "immersiveengineering:blocks/shaders/balloon_0");
//		i_balloonOverlay = ApiUtils.getRegisterSprite(map, this.baseTexturePath+"1_"+this.overlayType);
//		if(this.additionalTexture!=null)
//			i_balloonAdditional = ApiUtils.getRegisterSprite(map, this.baseTexturePath+additionalTexture);
//	}
//
//	@Override
//	public void modifyRender(ItemStack shader, ItemStack item, String modelPart, int pass, boolean pre, boolean inventory)
//	{
//	}
//}