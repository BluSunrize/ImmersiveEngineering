package blusunrize.immersiveengineering.common.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class RecipeShapedArrayList extends ShapedOreRecipe
{
	static HashMap<Integer, List> replacements = new HashMap<Integer, List>();
	public RecipeShapedArrayList(ItemStack result, Object... recipe)
	{
		super(result, saveIngredients(recipe));
		for(Map.Entry<Integer, List> entry : replacements.entrySet())
			if(entry.getKey()>=0 && entry.getKey()<this.input.length)
				this.input[entry.getKey()] = new ArrayList<>(entry.getValue());
	}

	public static Object[] saveIngredients(Object... recipe)
	{
		replacements = new HashMap<Integer, List>();
		Object[] converted = new Object[recipe.length];
		String shape = "";
		boolean shapeDone = false;
		for(int i=0; i<converted.length; i++)
		{
			converted[i] = recipe[i];
			if(!shapeDone)
				if(recipe[i] instanceof String[])
				{
					String[] parts = ((String[])recipe[i]);
					for(String s : parts)
						shape += s;
				}
				else if(recipe[i] instanceof String)
					shape += (String)recipe[i];

			if(recipe[i] instanceof Character)
			{
				shapeDone = true;
				Character chr = (Character)recipe[i];
				Object in = recipe[i+1];
				if(in instanceof List)
				{
					for(int j=0; j<shape.length(); j++)
						if(chr.charValue()==shape.charAt(j))
							replacements.put(j, (List)in);
					recipe[i+1] = Blocks.fire;
				}
			}
		}
		return converted;
	}
}