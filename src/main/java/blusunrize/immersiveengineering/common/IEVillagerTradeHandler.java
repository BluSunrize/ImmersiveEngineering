package blusunrize.immersiveengineering.common;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.registry.VillagerRegistry.IVillageTradeHandler;

public class IEVillagerTradeHandler implements IVillageTradeHandler
{
	float baseChance;
	ArrayList<MerchantDeal> dealList = new ArrayList<MerchantDeal>();

	public IEVillagerTradeHandler()
	{
		addDeal(.6f, new ItemStack(IEContent.itemMaterial,1,6),10,16, Items.emerald);
		addDeal(.6f, Items.emerald, new ItemStack(IEContent.itemMaterial,1,6),10,16);

		addDeal(.6f, Items.iron_ingot,2,4, new ItemStack(IEContent.itemMaterial,1,0),16,32);
		addDeal(.6f, new ItemStack(IEContent.itemMaterial,1,0),16,32, Items.iron_ingot,2,4);

		addDeal(.5f, Items.wheat_seeds,8,12, IEContent.itemSeeds,2,8);

		addDeal(.3f, Items.emerald,1,2, new ItemStack(IEContent.itemMetal,1,5),2,6);
		addDeal(.3f, new ItemStack(IEContent.itemMetal,1,5),2,6, Items.emerald,1,2);
		addDeal(.3f, Items.emerald,1,2, new ItemStack(IEContent.itemMetal,1,6),2,5);
		addDeal(.3f, new ItemStack(IEContent.itemMetal,1,6),2,5, Items.emerald,1,2);
		addDeal(.3f, Items.emerald,1,2, new ItemStack(IEContent.itemMetal,1,7),2,4);
		addDeal(.3f, new ItemStack(IEContent.itemMetal,1,7),2,4, Items.emerald,1,2);

		addDeal(.2f, Items.gold_ingot,2,4, new ItemStack(IEContent.itemTool,1,0));
		addDeal(.2f, Items.gold_ingot,2,4, new ItemStack(IEContent.itemTool,1,1));
		addDeal(.2f, Items.gold_ingot,2,4, new ItemStack(IEContent.itemTool,1,2));

		addDeal(.4f, Items.emerald, new ItemStack(IEContent.itemWireCoil,1,0),12,20);
		addDeal(.4f, new ItemStack(IEContent.itemWireCoil,1,0),12,20, Items.emerald);
		addDeal(.3f, Items.emerald,1,3, new ItemStack(IEContent.itemWireCoil,1,1),12,20);
		addDeal(.3f, new ItemStack(IEContent.itemWireCoil,1,1),12,20, Items.emerald,1,3);
		addDeal(.2f, Items.emerald,1,3, new ItemStack(IEContent.itemWireCoil,1,2),6,12);
		addDeal(.2f, new ItemStack(IEContent.itemWireCoil,1,2),6,12, Items.emerald,1,3);

		addDeal(.4f, Items.emerald,1,3, new ItemStack(IEContent.itemMaterial,1,11),2,6);
		addDeal(.4f, new ItemStack(IEContent.itemMaterial,1,11),2,6, Items.emerald,1,3);
		addDeal(.4f, Items.emerald,2,4, new ItemStack(IEContent.itemMaterial,1,12),2,6);
		addDeal(.4f, new ItemStack(IEContent.itemMaterial,1,12),2,6, Items.emerald,2,4);

		String[] blueprintCategories = IEContent.itemBlueprint.getSubNames();
		for(int i=0; i<blueprintCategories.length; i++)
			if(BlueprintCraftingRecipe.villagerPrices.get(blueprintCategories[i])!=null)
			{
				ItemStack price = BlueprintCraftingRecipe.villagerPrices.get(blueprintCategories[i]);
				int min = Math.max(1,price.stackSize-2);
				int max = Math.min(64,price.stackSize+2);
				addDeal(.4f, price,min,max, new ItemStack(IEContent.itemBlueprint,1,i));
				ItemStack special = new ItemStack(IEContent.itemBlueprint,1,i);
				special.setStackDisplayName("Super Special BluPrintz");
				ItemNBTHelper.setLore(special, "Congratulations!","You have found an easter egg!");
				addDeal(.05f, price,min,max, special);
			}
	}
	void addDeal(float chance, Object... objects)
	{
		MerchantItem[] items = new MerchantItem[3];

		byte currentObject=0;
		byte lastType=-1;

		Object tempItem=null;
		int val0=1;
		int val1=1;
		for(Object o : objects)
			if(o!=null)
			{
				if(o instanceof Integer)
				{
					if(lastType==0)
						val0 = ((Integer)o).intValue();
					else if(lastType==1)
						val1 = ((Integer)o).intValue();
					lastType++;
				}
				else if(o instanceof Item || o instanceof Block || o instanceof ItemStack)
				{
					if(tempItem!=null)
						items[currentObject++] = new MerchantItem(tempItem,val0,val1);
					tempItem = o;
					lastType=0;
					val0=1;
					val1=1;
				}
			}
		if(tempItem!=null)
			items[currentObject++] = new MerchantItem(tempItem,val0,val1);


		if(items[0]!=null&&items[1]!=null)
			if(items[2]!=null)
				dealList.add( new MerchantDeal(chance, items[0],items[1],items[2]));
			else
				dealList.add( new MerchantDeal(chance, items[0],null,items[1]));
	}

	@Override
	public void manipulateTradesForVillager(EntityVillager villager, MerchantRecipeList recipeList, Random random)
	{
		baseChance = ((Float)ObfuscationReflectionHelper.getPrivateValue(EntityVillager.class, villager, new String[] { "field_82191_bN" })).floatValue();
		for(MerchantDeal deal : dealList)
			deal.addToList(recipeList, random);
	}

	class MerchantDeal
	{
		float chance;
		MerchantItem input0;
		MerchantItem input1;
		MerchantItem sale;

		public MerchantDeal(float chance, MerchantItem input0, MerchantItem input1, MerchantItem sale)
		{
			this.chance = chance;
			this.input0 = input0;
			this.input1 = input1;
			this.sale = sale;
		}

		public void addToList(MerchantRecipeList recipeList, Random rand)
		{
			if(rand.nextFloat() < adjustProbability(chance))
				if(input1!=null)
					recipeList.add(new MerchantRecipe(input0.getItem(rand),input1.getItem(rand), sale.getItem(rand)));
				else
					recipeList.add(new MerchantRecipe(input0.getItem(rand),sale.getItem(rand)));
		}
	}

	class MerchantItem
	{
		final ItemStack item;
		final int minAmount;
		final int maxAmount;

		public MerchantItem(Object item, int minAmount, int maxAmount)
		{
			if(item instanceof Item)
				this.item = new ItemStack((Item)item);
			else if(item instanceof Block)
				this.item = new ItemStack((Block)item);
			else
				this.item = (ItemStack)item;
			this.minAmount = minAmount;
			this.maxAmount = maxAmount;
		}

		public MerchantItem(Object item, int amount)
		{
			this(item, amount, amount);
		}

		public ItemStack getItem(Random rand)
		{
			if(minAmount==maxAmount)
				return item;
			int dif = maxAmount-minAmount;
			return Utils.copyStackWithAmount(item, minAmount+rand.nextInt(dif+1));
		}
	}

	private float adjustProbability(float chance)
	{
		float f1 = chance + this.baseChance;
		return f1 > 0.9F ? 0.9F - (f1 - 0.9F) : f1;
	}
}