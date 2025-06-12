/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.loot;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.loot.AddDropModifier;
import blusunrize.immersiveengineering.common.util.loot.LootBlockStateFromLocationPredicate;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.neoforged.neoforge.common.Tags.Items;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;

import java.util.concurrent.CompletableFuture;

import static net.minecraft.advancements.critereon.ItemPredicate.Builder.item;
import static net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition.invert;
import static net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition.randomChance;
import static net.minecraft.world.level.storage.loot.predicates.MatchTool.toolMatches;

public class LootModifierProvider extends GlobalLootModifierProvider
{
	public LootModifierProvider(PackOutput output, CompletableFuture<Provider> provider)
	{
		super(output, provider, Lib.MODID);
	}

	@Override
	protected void start()
	{
		add("hemp_from_grass", new AddDropModifier(
				() -> Misc.HEMP_SEEDS.asItem().getDefaultInstance(),
				randomChance(0.1f),
				invert(toolMatches(item().of(Items.TOOLS_SHEAR))),
				LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.SHORT_GRASS)
		));
		add("hemp_from_tall_grass", new AddDropModifier(
				() -> Misc.HEMP_SEEDS.asItem().getDefaultInstance(),
				randomChance(0.1f),
				invert(toolMatches(item().of(Items.TOOLS_SHEAR))),
				LootItemBlockStatePropertyCondition.hasBlockStateProperties(Blocks.TALL_GRASS)
		));
		add("trial_vault_blueprint", new AddDropModifier(
				() -> BlueprintCraftingRecipe.getTypedBlueprint("automatons"),
				randomChance(0.062f), // same chance as Bolt Armor Trim
				new LootBlockStateFromLocationPredicate.Builder(Blocks.VAULT)
		));
	}
}
