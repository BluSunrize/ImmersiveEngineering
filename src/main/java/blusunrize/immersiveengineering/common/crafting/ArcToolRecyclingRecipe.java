package blusunrize.immersiveengineering.common.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import net.minecraft.item.ItemStack;
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
import blusunrize.immersiveengineering.common.util.Utils;

public class ArcToolRecyclingRecipe extends ArcFurnaceRecipe
{
	HashMap<ItemStack,Integer> outputs;
	public ArcToolRecyclingRecipe(HashMap<ItemStack,Integer> outputs, Object input, int time, int energyPerTick)
	{
		super(null,input,null, time, energyPerTick);
		this.outputs = outputs;
		this.setSpecialRecipeType("Recycling");
	}

	public ItemStack[] getOutputs(ItemStack input, ItemStack[] additives)
	{
		if(outputs==null)
			return new ItemStack[0];
		float mod = (input.getMaxDamage()-input.getItemDamage())/(float)input.getMaxDamage();
		ArrayList<ItemStack> outs = new ArrayList<ItemStack>();
		for(Entry<ItemStack,Integer> e : outputs.entrySet())
		{
			float scaledOut = mod*e.getValue();
			//Noone likes nuggets anyway >_>
			//			float nuggetOut = scaledOut-(int)scaledOut;
			//			Utils.getNuggetForItem();
			outs.add(Utils.copyStackWithAmount(e.getKey(),(int)scaledOut));
		}
		return outs.toArray(new ItemStack[outs.size()]);
	}
	public boolean matches(ItemStack input, ItemStack[] additives)
	{
		return input!=null&&this.input!=null && this.input instanceof ItemStack && input.getItem().equals(((ItemStack)this.input).getItem());
	}
}
