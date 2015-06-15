package blusunrize.immersiveengineering.common.items;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IUpgrade;
import blusunrize.immersiveengineering.common.util.Lib;

import com.google.common.collect.ImmutableSet;

public class ItemToolUpgrade extends ItemIEBase implements IUpgrade {

	public ItemToolUpgrade()
	{
		super("toolupgrade", 1, "drillWaterproof","drillSpeed","drillDamage");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		if(stack.getItemDamage()<getSubNames().length)
		{
			String[] flavour = ImmersiveEngineering.proxy.splitStringOnWidth(StatCollector.translateToLocal(Lib.DESC_FLAVOUR+"toolupgrade."+this.getSubNames()[stack.getItemDamage()]), 200);
			for(String s : flavour)
				list.add(s);
		}
	}
	@Override
	public int getItemStackLimit(ItemStack stack)
	{
		if(stack.getItemDamage() == 2)
			return 3;
		return super.getItemStackLimit(stack);
	}

	@Override
	public Set<UpgradeType> getUpgradeTypes(ItemStack upgrade)
	{
		return ImmutableSet.of(IUpgrade.UpgradeType.DRILL);
	}

	@Override
	public boolean canApplyUpgrades(ItemStack target, ItemStack upgrade)
	{
		return true;
	}

	@Override
	public void applyUpgrades(ItemStack target, ItemStack upgrade, HashMap<String, Object> modifications)
	{
		switch(upgrade.getItemDamage())
		{
		case 0:
			modifications.put("waterproof", true);
			break;
		case 1:
			Integer mod = (Integer)modifications.get("speed");
			modifications.put("speed", (mod==null?0:mod)+1);
			break;
		case 2:
			mod = (Integer)modifications.get("damage");
			modifications.put("damage", (mod==null?0:mod)+upgrade.stackSize);
			break;
		}
	}

}
