/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.common.util.IEDamageSources.ElectricDamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class FaradaySuitItem extends ArmorItem implements IElectricEquipment
{
	public static ArmorMaterial mat = new FaradayArmorMaterial();

	public FaradaySuitItem(Type type)
	{
		super(mat, type, new Properties().stacksTo(1));
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
			if((!(owner instanceof Player)||!((Player)owner).getAbilities().instabuild)&&
					equipped.hurt(2, ApiUtils.RANDOM_SOURCE, (dmg.getEntity() instanceof ServerPlayer)?(ServerPlayer)dmg.getEntity(): null))
				owner.setItemSlot(eqSlot, ItemStack.EMPTY);
		}
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type)
	{
		return ImmersiveEngineering.MODID+":textures/models/armor_faraday"+(slot==EquipmentSlot.LEGS?"_legs": "")+".png";
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book)
	{
		return false;
	}

	private static class FaradayArmorMaterial implements ArmorMaterial
	{

		@Override
		public int getDurabilityForType(Type slotIn)
		{
			return switch(slotIn)
					{
						case BOOTS -> 13;
						case LEGGINGS -> 15;
						case CHESTPLATE -> 16;
						case HELMET -> 11;
					};
		}

		@Override
		public int getDefenseForType(Type slotIn)
		{
			return switch(slotIn)
					{
						case BOOTS, HELMET -> 1;
						case LEGGINGS -> 2;
						case CHESTPLATE -> 3;
					};
		}

		@Override
		public int getEnchantmentValue()
		{
			return 0;
		}

		@Nonnull
		@Override
		public SoundEvent getEquipSound()
		{
			return SoundEvents.ARMOR_EQUIP_CHAIN;
		}

		@Nonnull
		@Override
		public Ingredient getRepairIngredient()
		{
			return Ingredient.of(IETags.getTagsFor(EnumMetals.ALUMINUM).plate);
		}

		@Nonnull
		@Override
		public String getName()
		{
			return ImmersiveEngineering.MODID+":faraday";
		}

		@Override
		public float getToughness()
		{
			return 0;
		}

		@Override
		public float getKnockbackResistance()
		{
			return 0;
		}
	}
}