package blusunrize.immersiveengineering.common.items;

import java.util.Map;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.ITeslaEquipment;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.IEDamageSources.TeslaDamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ItemFaradaySuit extends ItemArmor implements ITeslaEquipment
{
	public static ArmorMaterial mat;
	public ItemFaradaySuit(int type)
	{
		super(mat, 0, type);
		String name = "faradaySuit"+type;
		this.setUnlocalizedName(ImmersiveEngineering.MODID+"."+name);
		this.setCreativeTab(ImmersiveEngineering.creativeTab);
		this.setMaxStackSize(1);
		GameRegistry.registerItem(this, name);
		IEContent.registeredIEItems.add(this);
	}
	
	@Override
	public void onStrike(ItemStack s, int eqSlot, EntityLivingBase p, Map<String, Object> cache, TeslaDamageSource dmg)
	{
		if(dmg.isLowPower)
		{
			if (cache.containsKey("faraday"))
				cache.put("faraday", (1<<armorType)|((Integer)cache.get("faraday")));
			else
				cache.put("faraday", 1<<armorType);
			if(cache.containsKey("faraday")&&(Integer)cache.get("faraday")==(1<<4)-1)
				dmg.dmg = 0;
		}
		else
		{
			dmg.dmg*=1.2;
			if((!(p instanceof EntityPlayer)||!((EntityPlayer)p).capabilities.isCreativeMode)&&s.attemptDamageItem(2, itemRand))
				p.setCurrentItemOrArmor(eqSlot, null);
		}
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type)
	{
		return "immersiveengineering:textures/models/armor_faraday"+(slot==2?"_legs":"")+".png";
	}	
}