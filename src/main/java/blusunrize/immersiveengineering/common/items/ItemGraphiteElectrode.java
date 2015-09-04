package blusunrize.immersiveengineering.common.items;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.util.Lib;

public class ItemGraphiteElectrode extends ItemIEBase
{
	public ItemGraphiteElectrode()
	{
		super("graphiteElectrode", 1);
		this.setMaxDamage(Config.getInt("arcfurnace_electrodeDamage"));
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
	}

	@Override
    public boolean isItemTool(ItemStack stack)
	{
		return false;
	}
}