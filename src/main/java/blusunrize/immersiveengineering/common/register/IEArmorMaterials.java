/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem.Type;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterial.Layer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Map;

import static net.minecraft.world.item.ArmorItem.Type.*;

public class IEArmorMaterials
{
	private static final DeferredRegister<ArmorMaterial> REGISTER = DeferredRegister.create(
			Registries.ARMOR_MATERIAL, Lib.MODID
	);
	public static final Holder<ArmorMaterial> FARADAY = REGISTER.register("faraday", () -> new ArmorMaterial(
			Map.of(BOOTS, 1, HELMET, 1, LEGGINGS, 2, CHESTPLATE, 3),
			0,
			SoundEvents.ARMOR_EQUIP_CHAIN,
			() -> Ingredient.of(IETags.getTagsFor(EnumMetals.ALUMINUM).plate),
			// TODO may be wrong
			List.of(new Layer(IEApi.ieLoc("faraday"))),
			0,
			0
	));
	public static final Holder<ArmorMaterial> STEEL = REGISTER.register("steel", () -> new ArmorMaterial(
			Map.of(BOOTS, 2, HELMET, 2, LEGGINGS, 6, CHESTPLATE, 7),
			10,
			SoundEvents.ARMOR_EQUIP_IRON,
			() -> Ingredient.of(IETags.getTagsFor(EnumMetals.STEEL).ingot),
			List.of(new Layer(IEApi.ieLoc("steel"))),
			0,
			0
	));

	public static void init(IEventBus modBus)
	{
		REGISTER.register(modBus);
	}

	public static Item.Properties getProperties(Holder<ArmorMaterial> material, Type type)
	{
		return new Properties().durability(getDurability(material, type));
	}

	public static int getDurability(Holder<ArmorMaterial> material, Type type)
	{
		if(material.value()==STEEL.value())
		{
			return switch(type)
			{
				case BOOTS -> 273;
				case LEGGINGS -> 315;
				case CHESTPLATE -> 336;
				case HELMET -> 231;
				case BODY -> throw new UnsupportedOperationException("Steel body armor not implemented");
			};
		}
		else if(material.value()==FARADAY.value())
		{
			return switch(type)
			{
				case BOOTS -> 13;
				case LEGGINGS -> 15;
				case CHESTPLATE -> 16;
				case HELMET -> 11;
				case BODY -> throw new UnsupportedOperationException("Faraday body armor not implemented");
			};
		}
		else
			throw new UnsupportedOperationException("Unknown material "+material.getRegisteredName());
	}
}
