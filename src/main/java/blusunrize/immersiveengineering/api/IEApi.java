package blusunrize.immersiveengineering.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.registry.GameRegistry;

public class IEApi
{
	public static List<String> modPreference;
	public static HashMap<String, ItemStack> oreOutputPreference = new HashMap<String, ItemStack>();

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
			if(stack!=null)
			{
				String modId = GameRegistry.findUniqueIdentifierFor(stack.getItem()).modId;
				int idx = modId==null||modId.isEmpty()?-1: modPreference.indexOf(modId);
				if(preferredStack==null || (idx>=0 && (lastPref<0 || idx<lastPref)))
				{
					preferredStack = stack;
					lastPref = idx;
				}
			}
		return preferredStack;
	}
}