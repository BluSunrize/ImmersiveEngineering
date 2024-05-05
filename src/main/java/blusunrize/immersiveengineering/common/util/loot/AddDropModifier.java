/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition.Builder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;

import javax.annotation.Nonnull;
import java.util.Arrays;

public class AddDropModifier extends LootModifier
{
	private static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> REGISTER = DeferredRegister.create(
			Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ImmersiveEngineering.MODID
	);
	private static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddDropModifier>> GRASS_DROPS = REGISTER.register(
			"add_drop", () -> RecordCodecBuilder.mapCodec(
					inst -> codecStart(inst)
							.and(BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(m -> m.item))
							.apply(inst, AddDropModifier::new)
			)
	);

	private final Item item;

	protected AddDropModifier(LootItemCondition[] conditionsIn, Item item)
	{
		super(conditionsIn);
		this.item = item;
	}

	public AddDropModifier(ItemLike item, LootItemCondition.Builder... conditionsIn)
	{
		this(Arrays.stream(conditionsIn).map(Builder::build).toArray(LootItemCondition[]::new), item.asItem());
	}

	public static void init(IEventBus modBus)
	{
		REGISTER.register(modBus);
	}

	@Nonnull
	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
	{
		generatedLoot.add(new ItemStack(item));
		return generatedLoot;
	}

	@Override
	public MapCodec<? extends IGlobalLootModifier> codec()
	{
		return GRASS_DROPS.value();
	}
}
