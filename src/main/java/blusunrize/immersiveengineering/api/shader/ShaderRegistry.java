package blusunrize.immersiveengineering.api.shader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;

import net.minecraft.item.EnumRarity;

public class ShaderRegistry
{
	/**
	 * A list of shader names
	 */
	public static ArrayList<String> shaderList = new ArrayList<String>();
	/**
	 * A map of shader name to ShaderCase
	 */
	public static ArrayListMultimap<String,ShaderCase> shaderCaseRegistry = ArrayListMultimap.create();
	/**
	 * A list of shader names that can't be found as loot
	 */
	public static ArrayList<String> shaderLootBlacklist = new ArrayList<String>();
	/**
	 * A list of shader names that can't be found as a villager trade
	 */
	public static ArrayList<String> shaderTradeBlacklist = new ArrayList<String>();
	/**
	 * A map of shaders name to Rarity. This influences the colour of the item name as well as the rarity in grab bags
	 */
	public static HashMap<String, EnumRarity> shaderRarityMap = new HashMap<String, EnumRarity>();


	public static ShaderCase getShader(String name, String shaderType)
	{
		for(ShaderCase sCase : shaderCaseRegistry.get(name))
			if(sCase.getShaderType().equalsIgnoreCase(shaderType))
				return sCase;
		return null;
	}
	
	public static List<ShaderCase> registerShader(String name, String overlayType, EnumRarity rarity, int[] colourPrimary, int[] colourSecondary, int[] colourBackground, int[] colourBlade, String additionalTexture)
	{
		registerShader_Revolver(name, overlayType, colourBackground, colourPrimary, colourSecondary, colourBlade, additionalTexture);
		registerShader_Chemthrower(name, overlayType, colourBackground, colourPrimary, colourSecondary, true,false, additionalTexture);
		registerShader_Minecart(name, overlayType, colourPrimary, colourSecondary, additionalTexture);
		registerShader_Balloon(name, overlayType, colourPrimary, colourSecondary, additionalTexture);
		shaderRarityMap.put(name, rarity!=null?rarity:EnumRarity.common);
		return shaderCaseRegistry.get(name);
	}
	
	public static ShaderCaseRevolver registerShader_Revolver(String name, String overlayType, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, int[] colourBlade, String additionalTexture)
	{
		if(!shaderList.contains(name))
			shaderList.add(name);
		ShaderCaseRevolver shader = new ShaderCaseRevolver(overlayType, colourGrip, colourPrimary, colourSecondary, colourBlade, additionalTexture);
		shaderCaseRegistry.put(name, shader);
		return shader;
	}
	public static ShaderCaseChemthrower registerShader_Chemthrower(String name, String overlayType, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, boolean cageOnBase, boolean tanksUncoloured, String additionalTexture)
	{
		if(!shaderList.contains(name))
			shaderList.add(name);
		ShaderCaseChemthrower shader = new ShaderCaseChemthrower(overlayType, colourGrip, colourPrimary, colourSecondary, cageOnBase, tanksUncoloured, additionalTexture);
		shaderCaseRegistry.put(name, shader);
		return shader;
	}
	public static ShaderCaseMinecart registerShader_Minecart(String name, String overlayType, int[] colourPrimary, int[] colourSecondary, String additionalTexture)
	{
		if(!shaderList.contains(name))
			shaderList.add(name);
		ShaderCaseMinecart shader = new ShaderCaseMinecart(overlayType, colourPrimary, colourSecondary, additionalTexture);
		shaderCaseRegistry.put(name, shader);
		return shader;
	}
	public static ShaderCaseBalloon registerShader_Balloon(String name, String overlayType, int[] colourPrimary, int[] colourSecondary, String additionalTexture)
	{
		if(!shaderList.contains(name))
			shaderList.add(name);
		ShaderCaseBalloon shader = new ShaderCaseBalloon(overlayType, colourPrimary, colourSecondary, additionalTexture);
		shaderCaseRegistry.put(name, shader);
		return shader;
	}
	
	public static void blacklistShaderForLoot(String name)
	{
		if(!shaderLootBlacklist.contains(name))
			shaderLootBlacklist.add(name);
	}
	public static void blacklistShaderForTrade(String name)
	{
		if(!shaderTradeBlacklist.contains(name))
			shaderTradeBlacklist.add(name);
	}
}
