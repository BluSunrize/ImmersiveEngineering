package blusunrize.immersiveengineering.common.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;

public class ItemGraphiteElectrode extends ItemIEBase
{
	static int electrodeMaxDamage;
	public ItemGraphiteElectrode()
	{
		super("graphiteElectrode", 1);
		electrodeMaxDamage = Config.getInt("arcfurnace_electrodeDamage");
	}
	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list)
	{
		super.getSubItems(item, tab, list);
	}
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		float integrity = 100-(float)getDurabilityForDisplay(stack)*100f;
		list.add( String.format("%s %.2f %%", StatCollector.translateToLocal(Lib.DESC_INFO+"electrodeIntegrity"),integrity) );
		if(super.getDamage(stack)!=0)
			list.add("This item is deprecated. Hold it in your inventory to update it.");
	}
	@Override
	public void onUpdate(ItemStack stack, World world, Entity ent, int slot, boolean hand)
	{
		if(ent instanceof EntityPlayer)
			if(super.getDamage(stack)!=0)
			{
				ItemStack fixed = new ItemStack(this);
				ItemNBTHelper.setInt(fixed, "graphDmg", stack.getItemDamage());
				((EntityPlayer)ent).inventory.setInventorySlotContents(slot, fixed);
			}
	}
	@Override
	public boolean isItemTool(ItemStack stack)
	{
		return false;
	}
	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "graphDmg") / (double)electrodeMaxDamage;
	}
	@Override
	public int getMaxDamage(ItemStack stack)
	{
		return electrodeMaxDamage;
	}
	@Override
	public boolean isDamaged(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "graphDmg") > 0;
	}
	@Override
	public int getDamage(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "graphDmg");
	}
	@Override
	public void setDamage(ItemStack stack, int damage)
	{
		ItemNBTHelper.setInt(stack, "graphDmg", damage);
	}
}