package blusunrize.immersiveengineering.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderCaseChemthrower;
import blusunrize.immersiveengineering.api.shader.ShaderCaseMinecart;
import blusunrize.immersiveengineering.api.shader.ShaderCaseRevolver;

import com.google.common.collect.ArrayListMultimap;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

/**
 * @author BluSunrize - 13.08.2015
 *
 * An API class, for features that should be accessible in compatibility
 */
public class IEApi
{
	/**
	 * A list of mod-ids, representing the mods an ore should be used from in order of priority
	 */
	public static List<String> modPreference;
	
	/**
	 * This map caches the preferred ores for the given OreDict name
	 */
	public static HashMap<String, ItemStack> oreOutputPreference = new HashMap<String, ItemStack>();
	
	/**
	 * The TextureSheet id for the revolver's icons
	 */
	public static int revolverTextureSheetID;
	
	/**
	 * A list of shader names
	 */
	public static ArrayList<String> shaderList = new ArrayList<String>();
	/**
	 * A map of shader name to ShaderCase
	 */
	public static ArrayListMultimap<String,ShaderCase> shaderCaseRegistry = ArrayListMultimap.create();
	
	/**
	 * This map stores a list of OreDict prefixes (ingot, plate, gear, nugget) and their ingot relation (ingot:component) <br>
	 * Examples:<br>"plate"-{1,1},<br>"nugget"-{1,9},<br>"block"-{9,1},<br>"gear"-{4,1}
	 */
	public static HashMap<String, Integer[]> prefixToIngotMap = new HashMap<String, Integer[]>();
	
	/**
	 * An array of all potions added by IE. indices are as follows:<br>
	 * 0: flammable, increases all fire damage done<br>
	 * 1: slippery, makes the target slide around and randomly drop their held item<br>
	 * 2: conductive, increases flux damage done to the target (CoFH/RedstoneArsenal compat)<br>
	 */
	public static Potion[] potions;
	
	public static ItemStack getPreferredOreStack(String oreName)
	{
		if(!oreOutputPreference.containsKey(oreName))
		{
			ItemStack preferredStack = ApiUtils.isExistingOreName(oreName)?getPreferredStackbyMod(OreDictionary.getOres(oreName)): null;
			oreOutputPreference.put(oreName, preferredStack);
			return preferredStack;
		}
		ItemStack s = oreOutputPreference.get(oreName);
		return s!=null?s.copy():null;
	}
	public static ItemStack getPreferredStackbyMod(ArrayList<ItemStack> list)
	{
		ItemStack preferredStack = null;
		int lastPref = -1;
		for(ItemStack stack : list)
			if(stack!=null && stack.getItem()!=null)
			{
				UniqueIdentifier id = GameRegistry.findUniqueIdentifierFor(stack.getItem());
				if(id!=null)
				{
					String modId = id.modId;
					int idx = modId==null||modId.isEmpty()?-1: modPreference.indexOf(modId);
					if(preferredStack==null || (idx>=0 && (lastPref<0 || idx<lastPref)))
					{
						preferredStack = stack;
						lastPref = idx;
					}
				}
			}
		return preferredStack!=null?preferredStack.copy():null;
	}
	
	public static ShaderCase getShader(String name, String shaderType)
	{
		for(ShaderCase sCase : shaderCaseRegistry.get(name))
			if(sCase.getShaderType().equalsIgnoreCase(shaderType))
				return sCase;
		return null;
	}
	public static ShaderCaseRevolver registerShader_Revolver(String name, int overlayType, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, int[] colourBlade, String additionalTexture)
	{
		if(!shaderList.contains(name))
			shaderList.add(name);
		ShaderCaseRevolver shader = new ShaderCaseRevolver(overlayType, colourGrip, colourPrimary, colourSecondary, colourBlade, additionalTexture);
		shaderCaseRegistry.put(name, shader);
		return shader;
	}
	public static ShaderCaseChemthrower registerShader_Chemthrower(String name, int overlayType, int[] colourGrip, int[] colourPrimary, int[] colourSecondary, boolean cageOnBase, boolean tanksUncoloured, String additionalTexture)
	{
		if(!shaderList.contains(name))
			shaderList.add(name);
		ShaderCaseChemthrower shader = new ShaderCaseChemthrower(overlayType, colourGrip, colourPrimary, colourSecondary, cageOnBase, tanksUncoloured, additionalTexture);
		shaderCaseRegistry.put(name, shader);
		return shader;
	}
	public static ShaderCaseMinecart registerShader_Minecart(String name, int overlayType, int[] colourPrimary, int[] colourSecondary, String additionalTexture)
	{
		if(!shaderList.contains(name))
			shaderList.add(name);
		ShaderCaseMinecart shader = new ShaderCaseMinecart(overlayType, colourPrimary, colourSecondary, additionalTexture);
		shaderCaseRegistry.put(name, shader);
		return shader;
	}
	
}