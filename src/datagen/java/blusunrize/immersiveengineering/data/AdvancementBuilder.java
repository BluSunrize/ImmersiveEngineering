/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockAdvancementTrigger;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.register.IEBlocks.BlockEntry;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import net.minecraft.advancements.*;
import net.minecraft.advancements.AdvancementRequirements.Strategy;
import net.minecraft.advancements.critereon.*;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger.TriggerInstance;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.*;
import java.util.function.Consumer;

public class AdvancementBuilder
{
	public static String page = null;

	// recipeAdvancement is badly named, it just means that telemetry is disabled for this advancement. Since
	// telemetry only applies to vanilla events (minecraft namespace) it does not matter much.
	private final Advancement.Builder builder = Advancement.Builder.recipeAdvancement();

	private final String name;
	private ItemStack icon;
	private ResourceLocation background = null;
	private AdvancementType frame = AdvancementType.TASK;
	private boolean hidden = false;

	private boolean quiet = false;

	private AdvancementBuilder(String name)
	{
		assert page!=null;
		this.name = name;
	}

	public static void setPage(String page)
	{
		AdvancementBuilder.page = page;
	}

	public static AdvancementBuilder hiddenRoot()
	{
		return new AdvancementBuilder(page+"_root");
	}

	public static AdvancementBuilder root(String bg)
	{
		return new AdvancementBuilder(page+"_root").background(IEApi.ieLoc("textures/"+bg+".png"));
	}

	public static AdvancementBuilder child(String name, AdvancementHolder parent)
	{
		return new AdvancementBuilder(name).parent(parent);
	}

	public AdvancementBuilder getItem(ItemLike item)
	{
		return this.icon(item).hasItems(item);
	}

	public AdvancementBuilder placeBlock(BlockEntry<?> block)
	{
		return this.icon(block).addCriterion("place_block", TriggerInstance.placedBlock(block.get()));
	}

	public AdvancementBuilder multiblock(IETemplateMultiblock multiblock)
	{
		return this.icon(multiblock.getBlock())
				.addCriterion(
						"form_multiblock",
						MultiblockAdvancementTrigger.create(
								multiblock.getUniqueName(),
								ItemPredicate.Builder.item().of(Tools.HAMMER).build()
						)
				);
	}

	private AdvancementBuilder parent(AdvancementHolder parent)
	{
		this.builder.parent(parent);
		return this;
	}

	private AdvancementBuilder background(ResourceLocation background)
	{
		this.background = background;
		return this;
	}


	public AdvancementBuilder icon(ItemStack icon)
	{
		this.icon = icon;
		return this;
	}

	public AdvancementBuilder icon(ItemLike icon)
	{
		return this.icon(new ItemStack(icon));
	}

	public AdvancementBuilder goal()
	{
		this.frame = AdvancementType.GOAL;
		return this;
	}

	public AdvancementBuilder challenge()
	{
		this.frame = AdvancementType.CHALLENGE;
		return this;
	}

	public AdvancementBuilder hidden()
	{
		this.hidden = true;
		return this;
	}

	public AdvancementBuilder quiet()
	{
		this.quiet = true;
		return this;
	}

	public AdvancementBuilder loot(String lootPath)
	{
		this.builder.rewards(new AdvancementRewards(
				0,
				List.of(ResourceKey.create(Registries.LOOT_TABLE, IEApi.ieLoc("advancements/"+lootPath))),
				List.of(),
				Optional.empty())
		);
		return this;
	}

	public AdvancementBuilder addCriterion(String key, Criterion<?> criterion)
	{
		this.builder.addCriterion(key, criterion);
		return this;
	}

	public AdvancementBuilder orRequirements()
	{
		this.builder.requirements(Strategy.OR);
		return this;
	}

	public AdvancementBuilder hasItems(ItemLike... items)
	{
		return this.addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(
				ItemPredicate.Builder.item().of(items).build()
		));
	}

	public <T extends ItemSubPredicate> AdvancementBuilder hasItemWithPredicate(ItemLike item, ItemSubPredicate.Type<T> type, T subPredicate)
	{
		return this.addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(
				ItemPredicate.Builder.item().of(item)
						.withSubPredicate(type, subPredicate)
						.build()
		));
	}


	public AdvancementBuilder placeBlocks(Collection<? extends BlockEntry<?>> blocks)
	{
		blocks.stream().sorted(Comparator.comparing(BlockEntry::getId))
				.forEachOrdered(block -> addCriterion(block.getId().getPath(), TriggerInstance.placedBlock(block.get())));
		return this;
	}

	public AdvancementBuilder talkToVillagers(ResourceLocation... professions)
	{
		Arrays.stream(professions).sorted(Comparator.comparing(ResourceLocation::getPath))
				.forEachOrdered(prof -> {
					CompoundTag villagerData = new CompoundTag();
					villagerData.putString("profession", prof.toString());
					CompoundTag entityNBT = new CompoundTag();
					entityNBT.put("VillagerData", villagerData);
					ContextAwarePredicate isRightVillager = EntityPredicate.wrap(
							EntityPredicate.Builder.entity().of(EntityType.VILLAGER).nbt(new NbtPredicate(entityNBT)).build()
					);
					addCriterion("meet_"+prof.getPath(), PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(
							Optional.empty(),
							ItemPredicate.Builder.item(),
							Optional.of(isRightVillager)
					));
				});
		return this;
	}

	public AdvancementBuilder codeTriggered()
	{
		return this.addCriterion(
				"code_trigger", CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance())
		);
	}

	private Advancement.Builder withDisplay()
	{
		if(this.icon==null)
			return this.builder;
		return this.builder.display(new DisplayInfo(
				this.icon,
				Component.translatable("advancement.immersiveengineering."+this.name),
				Component.translatable("advancement.immersiveengineering."+this.name+".desc"),
				Optional.ofNullable(this.background),
				this.frame,
				!this.quiet,
				!this.quiet,
				this.hidden
		));
	}

	public void saveForManual(Consumer<AdvancementHolder> consumer)
	{
		this.builder.display(new DisplayInfo(
				new ItemStack(Tools.MANUAL),
				CommonComponents.EMPTY,
				CommonComponents.EMPTY,
				Optional.empty(),
				AdvancementType.TASK,
				true,
				false,
				true
		)).save(consumer, Lib.MODID+":"+AdvancementBuilder.page+"/"+this.name);
	}

	public AdvancementHolder save(Consumer<AdvancementHolder> consumer)
	{
		return this.withDisplay().save(consumer, Lib.MODID+":"+AdvancementBuilder.page+"/"+this.name);
	}

}
