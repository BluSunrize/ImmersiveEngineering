package blusunrize.immersiveengineering.common.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntryItem;
import net.minecraft.world.storage.loot.RandomValueRange;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.RandomChance;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.SetCount;
import net.minecraft.world.storage.loot.functions.SetMetadata;
import net.minecraft.world.storage.loot.functions.SetNBT;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static java.lang.Math.min;

/**
 * @author BluSunrize - 22.07.2016
 */
public class IELootEntry extends LootEntryItem
{
	public static LootFunction blueprintRename;
	static{
		NBTTagCompound tag = new NBTTagCompound();
		tag.setTag("display", new NBTTagCompound());
		tag.getCompoundTag("display").setString("name", "Super Special BluPrintz");
		NBTTagList list = new NBTTagList();
		list.appendTag(new NBTTagString("Congratulations!"));
		list.appendTag(new NBTTagString("You have found an easter egg!"));
		tag.getCompoundTag("display").setTag("Lore", list);
		blueprintRename = new SetNBT(new LootCondition[]{new RandomChance(.125f)}, tag);
	}

	ItemStack stack;

	public IELootEntry(String entryName, @Nonnull ItemStack stack, int weight, int quality, LootFunction... functions)
	{
		super(stack.getItem(), weight, quality, buildFunctions(stack, functions), new LootCondition[0], entryName);
		this.stack = stack;
	}

	static LootFunction[] buildFunctions(ItemStack stack, LootFunction[] functions)
	{
		List<LootFunction> totalFunctions = new ArrayList();
		for(LootFunction f : functions)
			totalFunctions.add(f);
		if(stack.getMetadata()!=0)
			totalFunctions.add(new SetMetadata(new LootCondition[0], new RandomValueRange(stack.getMetadata(),stack.getMetadata())));
		if(stack.getCount() > 1)
			totalFunctions.add(new SetCount(new LootCondition[0], new RandomValueRange(stack.getMetadata(),stack.getMetadata())));
		if(stack.getTagCompound()!=null)
			totalFunctions.add(new SetNBT(new LootCondition[0], stack.getTagCompound()));
		return totalFunctions.toArray(new LootFunction[totalFunctions.size()]);
	}

	@Override
	public void addLoot(Collection<ItemStack> stacks, Random rand, LootContext context)
	{
		ItemStack itemstack = stack.copy();
		int i = 0;
		if(itemstack.getCount() > 0)
		{
			if(itemstack.getCount() < this.stack.getItem().getItemStackLimit(itemstack))
				stacks.add(itemstack);
			else
			{
				i = itemstack.getCount();
				while(i > 0)
				{
					ItemStack itemstack1 = itemstack.copy();
					itemstack1.setCount(min(itemstack.getMaxStackSize(), i));
					i -= itemstack1.getCount();
					stacks.add(itemstack1);
				}
			}
		}
	}
}
