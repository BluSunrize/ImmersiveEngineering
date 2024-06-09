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
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterial.Layer;
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
			List.of(new Layer(IEApi.ieLoc("textures/models/armor_faraday.png"))),
			0,
			0
	));
	public static final Holder<ArmorMaterial> STEEL = REGISTER.register("steel", () -> new ArmorMaterial(
			Map.of(BOOTS, 273, LEGGINGS, 315, CHESTPLATE, 336, HELMET, 231),
			10,
			SoundEvents.ARMOR_EQUIP_IRON,
			() -> Ingredient.of(IETags.getTagsFor(EnumMetals.STEEL).ingot),
			List.of(new Layer(IEApi.ieLoc("textures/models/armor_steel.png"))),
			0,
			0
	));

	public static void init(IEventBus modBus)
	{
		REGISTER.register(modBus);
	}

}
