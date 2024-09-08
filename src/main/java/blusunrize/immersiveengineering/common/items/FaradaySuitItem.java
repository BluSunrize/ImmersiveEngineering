/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.common.register.IEArmorMaterials;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Map;

public class FaradaySuitItem extends ArmorItem implements IElectricEquipment
{
	public FaradaySuitItem(Type type)
	{
		super(IEArmorMaterials.FARADAY, type, IEArmorMaterials.getProperties(IEArmorMaterials.FARADAY, type));
	}

	@Override
	public void onStrike(ItemStack equipped, EquipmentSlot eqSlot, LivingEntity owner, Map<String, Object> cache,
						 @Nullable DamageSource dSource, ElectricSource eSource)
	{
		if(!(dSource instanceof ElectricDamageSource))
			return;
		ElectricDamageSource dmg = (ElectricDamageSource)dSource;
		if(dmg.source.level < 1.75)
		{
			if(cache.containsKey("faraday"))
				cache.put("faraday", (1<<this.type.ordinal())|((Integer)cache.get("faraday")));
			else
				cache.put("faraday", 1<<this.type.ordinal());
			if(cache.containsKey("faraday")&&(Integer)cache.get("faraday")==(1<<4)-1)
				dmg.dmg = 0;
		}
		else
		{
			dmg.dmg *= 1.2;
			if(!(owner instanceof Player)||!((Player)owner).getAbilities().instabuild)
				equipped.hurtAndBreak(2, (dmg.getEntity() instanceof ServerPlayer)?(ServerPlayer)dmg.getEntity(): null, eqSlot);
		}
	}

	@Override
	@Nullable
	public ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, Layer layer, boolean innerModel)
	{
		// TODO remove
		//if(slot==EquipmentSlot.LEGS)
		//	return IEApi.ieLoc("textures/models/armor/faraday_layer_2.png");
		//else
			return null;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book)
	{
		return false;
	}
}