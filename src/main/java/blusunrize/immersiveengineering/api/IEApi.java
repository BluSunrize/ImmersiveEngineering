package blusunrize.immersiveengineering.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

/**
 * @author BluSunrize - 13.08.2015
 *
 * An API class, for features that should be accessible in compatibility
 */
public class IEApi
{
	/**A list of mod-ids, representing the mods an ore should be used from in order of priority
	 */
	public static List<String> modPreference;
	/**This map caches the preferred ores for the given OreDict name
	 */
	public static HashMap<String, ItemStack> oreOutputPreference = new HashMap<String, ItemStack>();
	/**The TextureSheet id for the revolver's icons
	 */
	public static int revolverTextureSheetID;
	/**A list of NBTTagCompounds that can be applied to the shader items when they are generated as loot or villager trade
	 */
	public static ArrayList<NBTTagCompound> shaderList = new ArrayList<NBTTagCompound>();
	
	public static ItemStack getPreferredOreStack(String oreName)
	{
		if(!oreOutputPreference.containsKey(oreName))
		{
			ItemStack preferredStack = getPreferredStackbyMod(OreDictionary.getOres(oreName));
			oreOutputPreference.put(oreName, preferredStack);
			return preferredStack;
		}
		return oreOutputPreference.get(oreName);
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
		return preferredStack;
	}
}