/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import java.util.function.Supplier;

import static net.minecraft.world.item.Item.BASE_ATTACK_DAMAGE_ID;
import static net.minecraft.world.item.Item.BASE_ATTACK_SPEED_ID;

public class IETools
{
	public static Supplier<ShovelItem> createShovel(Tier tier)
	{
		return () -> new ShovelItem(tier, toolProperties(tier, 1.5, -3));
	}

	public static Supplier<AxeItem> createAxe(Tier tier)
	{
		return () -> new AxeItem(tier, toolProperties(tier, 6, -3.1));
	}

	public static Supplier<PickaxeItem> createPickaxe(Tier tier)
	{
		return () -> new PickaxeItem(tier, toolProperties(tier, 1, -2.8));
	}

	public static Supplier<SwordItem> createSword(Tier tier)
	{
		return () -> new SwordItem(tier, toolProperties(tier, 3, -2.4));
	}

	public static Supplier<HoeItem> createHoe(Tier tier)
	{
		return () -> new HoeItem(tier, toolProperties(tier, -2, -1));
	}

	private static Item.Properties toolProperties(Tier tier, double attackDamage, double attackSpeed)
	{
		return new Item.Properties().stacksTo(1).attributes(ItemAttributeModifiers.builder().add(
				Attributes.ATTACK_DAMAGE,
				new AttributeModifier(BASE_ATTACK_DAMAGE_ID, (attackDamage+tier.getAttackDamageBonus()), Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
		).add(
				Attributes.ATTACK_SPEED,
				new AttributeModifier(BASE_ATTACK_SPEED_ID, attackSpeed, Operation.ADD_VALUE),
				EquipmentSlotGroup.MAINHAND
		).build());
	}
}
