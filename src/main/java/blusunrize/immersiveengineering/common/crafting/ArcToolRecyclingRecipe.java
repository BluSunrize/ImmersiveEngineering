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
			//			float nuggetOut = scaledOut-(int)scaledOut;
			//			Utils.getNuggetForItem();
			outs.add(Utils.copyStackWithAmount(e.getKey(),(int)scaledOut));
		}
		return outs.toArray(new ItemStack[outs.size()]);
		//		if(input!=null)
		//			if(input.getItem() instanceof ItemTool || input.getItem() instanceof ItemSword || input.getItem() instanceof ItemHoe || input.getItem() instanceof ItemArmor)
		//			{
		//				ItemStack ss = IEApi.getPreferredOreStack("ingot"+metal);
		//				if(ss!=null && input.getItem().getIsRepairable(input, ss))
		//				{
		//					int amount = 0;
		//					int classes = 0;
		//					if(input.getItem() instanceof ItemHoe)
		//					{
		//						amount += 2;
		//						classes++;
		//					}
		//					if(input.getItem() instanceof ItemSword)
		//					{
		//						amount += 2;
		//						classes++;
		//					}
		//					Set<String> toolClasses = input.getItem().getToolClasses(input);
		//					if(toolClasses!=null)
		//					{
		//						classes += toolClasses.size();
		//						for(String c : toolClasses)
		//						{
		//							if(c.equalsIgnoreCase("pickaxe"))
		//								amount += 3;
		//							else if(c.equalsIgnoreCase("axe"))
		//								amount += 3;
		//							else if(c.equalsIgnoreCase("shovel"))
		//								amount += 1;
		//						}
		//					}
		//					if(input.getItem() instanceof ItemArmor)
		//					{
		//						((ItemArmor)input.getItem()).armorType
		//					}
		//					if(classes>0)
		//					{
		//						amount = (int)(amount/(float)classes);
		//						if(amount>0)
		//							return new ItemStack[]{Utils.copyStackWithAmount(ss, amount)};
		//					}
		//				}
		//			}
//		return new ItemStack[]{};
	}
	public boolean matches(ItemStack input, ItemStack[] additives)
	{
		//		if(input!=null)
		//			if(input.getItem() instanceof ItemTool || input.getItem() instanceof ItemSword || input.getItem() instanceof ItemHoe || input.getItem() instanceof ItemArmor)
		//			{
		//				ItemStack ss = IEApi.getPreferredOreStack("ingot"+metal);
		//				if(ss!=null && input.getItem().getIsRepairable(input, ss))
		//				{
		//					return true;
		//				}
		//			}
		//		ToolMaterial toolMat = null;
		//		if(item instanceof ItemTool)
		//			toolMat = ((ItemTool)item).func_150913_i();
		//		else if(item instanceof ItemHoe)
		//		{
		//			toolMat = ToolMaterial.valueOf(((ItemHoe)item).getToolMaterialName());
		//			ingotAmount += 2;
		//			toolClasses++;
		//		}
		//		else if(item instanceof ItemSword)
		//		{
		//			toolMat = ToolMaterial.valueOf(((ItemSword)item).getToolMaterialName());
		//			ingotAmount += 2;
		//			toolClasses++;
		//		}
		//		if(toolMat!=null)
		//		{
//		if(input!=null)
//			System.out.println("comparing: "+input+" to "+this.input);

		return input!=null&&this.input!=null && this.input instanceof ItemStack && input.getItem().equals(((ItemStack)this.input).getItem());
	}
}
