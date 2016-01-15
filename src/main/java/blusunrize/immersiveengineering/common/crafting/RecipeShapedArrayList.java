package blusunrize.immersiveengineering.common.crafting;

import java.lang.reflect.Field;
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
		try{
			Field f_Input = ShapedOreRecipe.class.getDeclaredField("input");
			f_Input.setAccessible(true);
			Object[] rInput = (Object[])f_Input.get(this);

			for(Map.Entry<Integer, List> entry : replacements.entrySet())
				if(entry.getKey()>=0 && entry.getKey()<rInput.length)
					rInput[entry.getKey()] = new ArrayList<>(entry.getValue());
			
			f_Input.set(this, rInput);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
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