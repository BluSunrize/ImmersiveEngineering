/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.loot;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries.Keys;

import javax.annotation.Nonnull;

public class GrassDropModifier extends LootModifier
{
	private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> REGISTER = DeferredRegister.create(
			Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ImmersiveEngineering.MODID
	);
	private static final DeferredHolder<Codec<? extends IGlobalLootModifier>, Codec<GrassDropModifier>> GRASS_DROPS = REGISTER.register(
			"hemp_seed_drops", () -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, GrassDropModifier::new))
	);


	protected GrassDropModifier(LootItemCondition[] conditionsIn)
	{
		super(conditionsIn);
	}

	public static void init(IEventBus modBus)
	{
		REGISTER.register(modBus);
	}

	@Nonnull
	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
	{
		generatedLoot.add(new ItemStack(Misc.HEMP_SEEDS));
		return generatedLoot;
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec()
	{
		return GRASS_DROPS.value();
	}
}
