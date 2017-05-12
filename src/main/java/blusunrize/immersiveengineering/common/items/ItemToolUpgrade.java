package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.IUpgrade;
import blusunrize.immersiveengineering.api.tool.IUpgradeableTool;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ItemToolUpgrade extends ItemIEBase implements IUpgrade {

	public ItemToolUpgrade()
	{
		super("toolupgrade", 1, "drill_waterproof","drill_lube","drill_damage","drill_capacity",
				"revolver_bayonet","revolver_magazine","revolver_electro",
				"chemthrower_focus","railgun_scope","railgun_capacitors");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		if(stack.getItemDamage()<getSubNames().length)
		{
			String[] flavour = ImmersiveEngineering.proxy.splitStringOnWidth(I18n.format(Lib.DESC_FLAVOUR+"toolupgrade."+this.getSubNames()[stack.getItemDamage()]), 200);
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
	public Set<String> getUpgradeTypes(ItemStack upgrade)
	{
		if(upgrade.getItemDamage()<=2)
			return ImmutableSet.of("DRILL");
		else if(upgrade.getItemDamage()==3)
			return ImmutableSet.of("DRILL","CHEMTHROWER");
		else if(upgrade.getItemDamage()<=6)
			return ImmutableSet.of("REVOLVER");
		else if(upgrade.getItemDamage()==7)
			return ImmutableSet.of("CHEMTHROWER");
		else
			return ImmutableSet.of("RAILGUN");
	}

	@Override
	public boolean canApplyUpgrades(ItemStack target, ItemStack upgrade)
	{
		if(upgrade.getItemDamage()==5 && target.getItem() instanceof IUpgradeableTool)
			return !((IUpgradeableTool)target.getItem()).getUpgrades(target).hasKey("bullets");
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
			modifications.put("oiled", true);
			break;
		case 2:
			Float speed = (Float)modifications.get("speed");
			modifications.put("speed", (speed==null?0:speed)+upgrade.getCount()*2f);
			Integer mod = (Integer)modifications.get("damage");
			modifications.put("damage", (mod==null?0:mod)+upgrade.getCount());
			break;
		case 3:
			mod = (Integer)modifications.get("capacity");
			modifications.put("capacity", (mod==null?0:mod)+2000);
			break;

		case 4:
			Float melee = (Float)modifications.get("melee");
			modifications.put("melee", (melee==null?0:melee)+6f);
			break;
		case 5:
			mod = (Integer)modifications.get("bullets");
			modifications.put("bullets", (mod==null?0:mod)+6);
			break;
		case 6:
			modifications.put("electro", true);
			break;
			
		case 7:
			modifications.put("focus", true);
			break;
			
		case 8:
			modifications.put("scope", true);
			break;
			
		case 9:
			modifications.put("speed", 1f);
			modifications.put("capacity", 4000);
			break;
		}
	}

}
