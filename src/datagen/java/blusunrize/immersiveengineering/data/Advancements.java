/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.BasicConveyor;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.register.IEItems.Metals;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.register.IEItems.Weapons;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.advancements.MultiblockTrigger;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.PlacedBlockTrigger;
import net.minecraft.commands.CommandFunction;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

public class Advancements extends AdvancementProvider
{
	private final Path OUTPUT;
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();

	public Advancements(DataGenerator gen)
	{
		super(gen);
		OUTPUT = gen.getOutputFolder();
	}

	@Override
	public void run(HashCache cache)
	{
		Set<ResourceLocation> set = Sets.newHashSet();
		Consumer<Advancement> consumer = (advancement) -> {
			if(!set.add(advancement.getId()))
			{
				throw new IllegalStateException("Duplicate advancement "+advancement.getId());
			}
			else
			{
				Path path1 = createPath(OUTPUT, advancement);

				try
				{
					DataProvider.save(GSON, cache, advancement.deconstruct().serializeToJson(), path1);
				} catch(IOException ioexception)
				{
					IELogger.error("Couldn't save advancement {}", path1, ioexception);
				}

			}
		};

		new IEAdvancements().accept(consumer);
	}

	public static class IEAdvancements implements Consumer<Consumer<Advancement>>
	{
		public void accept(Consumer<Advancement> consumer)
		{

			Advancement rtfm = Advancement.Builder.advancement()
					.display(Tools.MANUAL,
							Component.translatable("advancement.immersiveengineering.root"),
							Component.translatable("advancement.immersiveengineering.root.desc"),
							new ResourceLocation("immersiveengineering:textures/block/wooden_decoration/treated_wood.png"),
							FrameType.TASK, true, true, false)
					.addCriterion("manual", InventoryChangeTrigger.TriggerInstance.hasItems(Tools.MANUAL))
					.save(consumer, "immersiveengineering:main/root");

			Advancement hammer = advancement(rtfm, Tools.HAMMER, "craft_hammer", FrameType.TASK, true, true, false)
					.addCriterion("hammer", InventoryChangeTrigger.TriggerInstance.hasItems(Tools.HAMMER))
					.save(consumer, "immersiveengineering:main/craft_hammer");

			Advancement wire = advancement(rtfm, Misc.WIRE_COILS.get(WireType.COPPER), "connect_wire", FrameType.TASK, true, true, false)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.save(consumer, "immersiveengineering:main/connect_wire");

			Advancement blastfurnace = advancement(hammer, Multiblocks.BLAST_FURNACE, "mb_blastfurnace", FrameType.GOAL, true, true, false)
					.addCriterion("blastfurnace",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/blast_furnace"),
									ItemPredicate.Builder.item().of(Tools.HAMMER).build()))
					.save(consumer, "immersiveengineering:main/mb_blastfurnace");

			Advancement steel = advancement(blastfurnace, Metals.INGOTS.get(EnumMetals.STEEL), "make_steel", FrameType.TASK, true, true, false)
					.addCriterion("steel", InventoryChangeTrigger.TriggerInstance.hasItems(Metals.INGOTS.get(EnumMetals.STEEL)))
					.save(consumer, "immersiveengineering:main/make_steel");

			Advancement.Builder b_conveyor = advancement(rtfm, MetalDevices.CONVEYORS.get(BasicConveyor.TYPE), "place_conveyor", FrameType.TASK, true, true, false)
					.requirements(RequirementsStrategy.OR);
			MetalDevices.CONVEYORS.entrySet().stream()
					.sorted(Comparator.comparing((Entry<IConveyorType<?>, ?> e) -> e.getKey().getId()))
					.forEach(e -> {
						String name = e.getKey().getId().getPath();
						b_conveyor.addCriterion(
								name, PlacedBlockTrigger.TriggerInstance.placedBlock(e.getValue().get())
						);
					});
			Advancement conveyor = b_conveyor.save(consumer, "immersiveengineering:main/place_conveyor");

			Advancement windmill = advancement(rtfm, WoodenDevices.WINDMILL, "place_windmill", FrameType.TASK, true, true, false)
					.addCriterion("windmill", PlacedBlockTrigger.TriggerInstance.placedBlock(WoodenDevices.WINDMILL.get()))
					.save(consumer, "immersiveengineering:main/place_windmill");

			Advancement heater = advancement(wire, MetalDevices.FURNACE_HEATER, "craft_heater", FrameType.TASK, true, true, false)
					.addCriterion("heater", InventoryChangeTrigger.TriggerInstance.hasItems(MetalDevices.FURNACE_HEATER))
					.save(consumer, "immersiveengineering:main/craft_heater");

			Advancement pump = advancement(wire, MetalDevices.FLUID_PUMP, "craft_pump", FrameType.TASK, true, true, false)
					.addCriterion("pump", InventoryChangeTrigger.TriggerInstance.hasItems(MetalDevices.FLUID_PUMP))
					.save(consumer, "immersiveengineering:main/craft_pump");

			Advancement floodlight = advancement(wire, MetalDevices.FLOODLIGHT, "place_floodlight", FrameType.TASK, true, true, false)
					.addCriterion("floodlight", PlacedBlockTrigger.TriggerInstance.placedBlock(MetalDevices.FLOODLIGHT.get()))
					.save(consumer, "immersiveengineering:main/place_floodlight");

			Advancement workbench = advancement(rtfm, WoodenDevices.WORKBENCH, "craft_workbench", FrameType.TASK, true, true, false)
					.addCriterion("workbench", InventoryChangeTrigger.TriggerInstance.hasItems(WoodenDevices.WORKBENCH))
					.save(consumer, "immersiveengineering:main/craft_workbench");

			Advancement revolver = advancement(workbench, Weapons.REVOLVER, "craft_revolver", FrameType.TASK, true, true, false)
					.addCriterion("revolver", InventoryChangeTrigger.TriggerInstance.hasItems(Weapons.REVOLVER))
					.save(consumer, "immersiveengineering:main/craft_revolver");

			ItemStack upgradedRevolver = new ItemStack(Weapons.REVOLVER);
			CompoundTag upgrades = new CompoundTag();
			upgrades.putInt("bullets", 6);
			upgrades.putBoolean("electro", true);
			ItemNBTHelper.setTagCompound(upgradedRevolver, "upgrades", upgrades);
			Advancement upgrade_revolver = advancement(revolver, upgradedRevolver, "upgrade_revolver", FrameType.CHALLENGE, true, true, false)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.rewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_rare")))
					.save(consumer, "immersiveengineering:main/upgrade_revolver");

			Advancement wolfpack = advancement(revolver, BulletHandler.getBulletItem(BulletItem.WOLFPACK), "craft_wolfpack", FrameType.TASK, true, true, true)
					.addCriterion("wolfpack", InventoryChangeTrigger.TriggerInstance.hasItems(BulletHandler.getBulletItem(BulletItem.WOLFPACK)))
					.save(consumer, "immersiveengineering:main/craft_wolfpack");

			Advancement drill = advancement(workbench, Tools.DRILL, "craft_drill", FrameType.TASK, true, true, false)
					.addCriterion("drill", InventoryChangeTrigger.TriggerInstance.hasItems(Tools.DRILL))
					.save(consumer, "immersiveengineering:main/craft_drill");

			ItemStack upgradedDrill = new ItemStack(Tools.DRILL);
			upgrades = new CompoundTag();
			upgrades.putInt("damage", 3);
			upgrades.putBoolean("waterproof", true);
			upgrades.putBoolean("oiled", true);
			upgrades.putFloat("speed", 6.0f);
			ItemNBTHelper.setTagCompound(upgradedDrill, "upgrades", upgrades);
			ItemNBTHelper.setItemStack(upgradedDrill, "head", new ItemStack(Tools.DRILLHEAD_STEEL));
			Advancement upgradeDrill = advancement(drill, upgradedDrill, "upgrade_drill", FrameType.CHALLENGE, true, true, false)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.rewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_rare")))
					.save(consumer, "immersiveengineering:main/upgrade_drill");

			Advancement buzzsaw = advancement(workbench, Tools.BUZZSAW, "craft_buzzsaw", FrameType.TASK, true, true, false)
					.addCriterion("buzzsaw", InventoryChangeTrigger.TriggerInstance.hasItems(Tools.BUZZSAW))
					.save(consumer, "immersiveengineering:main/craft_buzzsaw");

			ItemStack upgradedBuzzsaw = new ItemStack(Tools.BUZZSAW);
			upgrades = new CompoundTag();
			upgrades.putBoolean("oiled", true);
			upgrades.putBoolean("spareblades", true);
			ItemNBTHelper.setTagCompound(upgradedBuzzsaw, "upgrades", upgrades);
			ItemNBTHelper.setItemStack(upgradedBuzzsaw, "sawblade", new ItemStack(Tools.SAWBLADE));
			ItemNBTHelper.setItemStack(upgradedBuzzsaw, "sawblade_spare1", new ItemStack(Tools.SAWBLADE));
			ItemNBTHelper.setItemStack(upgradedBuzzsaw, "sawblade_spare2", new ItemStack(Tools.SAWBLADE));
			Advancement upgradeBuzzsaw = advancement(buzzsaw, upgradedBuzzsaw, "upgrade_buzzsaw", FrameType.CHALLENGE, true, true, false)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.rewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_rare")))
					.save(consumer, "immersiveengineering:main/upgrade_buzzsaw");

			Advancement skyhook = advancement(workbench, Misc.SKYHOOK, "craft_skyhook", FrameType.TASK, true, true, false)
					.addCriterion("skyhook", InventoryChangeTrigger.TriggerInstance.hasItems(Misc.SKYHOOK))
					.save(consumer, "immersiveengineering:main/craft_skyhook");

			//Todo: Advancement for traveling 1km by skyhook

			Advancement chemthrower = advancement(workbench, Weapons.CHEMTHROWER, "craft_chemthrower", FrameType.TASK, true, true, false)
					.addCriterion("chemthrower", InventoryChangeTrigger.TriggerInstance.hasItems(Weapons.CHEMTHROWER))
					.save(consumer, "immersiveengineering:main/craft_chemthrower");

			Advancement railgun = advancement(workbench, Weapons.RAILGUN, "craft_railgun", FrameType.TASK, true, true, false)
					.addCriterion("railgun", InventoryChangeTrigger.TriggerInstance.hasItems(Weapons.RAILGUN))
					.save(consumer, "immersiveengineering:main/craft_railgun");

			ItemStack upgradedRailgun = new ItemStack(Weapons.RAILGUN);
			upgrades = new CompoundTag();
			upgrades.putBoolean("scope", true);
			upgrades.putFloat("speed", 1f);
			ItemNBTHelper.setTagCompound(upgradedRailgun, "upgrades", upgrades);
			Advancement upgradeRailgun = advancement(railgun, upgradedRailgun, "upgrade_railgun", FrameType.CHALLENGE, true, true, false)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.rewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_rare")))
					.save(consumer, "immersiveengineering:main/upgrade_railgun");

			Advancement improvedblastfurnace = advancement(steel, Multiblocks.ADVANCED_BLAST_FURNACE, "mb_improvedblastfurnace", FrameType.TASK, true, true, false)
					.addCriterion("improvedblastfurnace",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/improved_blast_furnace"),
									ItemPredicate.Builder.item().of(Tools.HAMMER).build()))
					.save(consumer, "immersiveengineering:main/mb_improvedblastfurnace");

			Advancement metalpress = advancement(steel, Multiblocks.METAL_PRESS, "mb_metalpress", FrameType.TASK, true, true, false)
					.addCriterion("metalpress",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/metal_press"),
									ItemPredicate.Builder.item().of(Tools.HAMMER).build()))
					.save(consumer, "immersiveengineering:main/mb_metalpress");

			Advancement silo = advancement(steel, Multiblocks.SILO, "mb_silo", FrameType.TASK, true, true, false)
					.addCriterion("silo",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/silo"),
									ItemPredicate.Builder.item().of(Tools.HAMMER).build()))
					.save(consumer, "immersiveengineering:main/mb_silo");

			Advancement crusher = advancement(steel, Multiblocks.CRUSHER, "mb_crusher", FrameType.GOAL, true, true, false)
					.addCriterion("crusher",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/crusher"),
									ItemPredicate.Builder.item().of(Tools.HAMMER).build()))
					.save(consumer, "immersiveengineering:main/mb_crusher");

			Advancement dieselgen = advancement(steel, Multiblocks.DIESEL_GENERATOR, "mb_dieselgen", FrameType.GOAL, true, true, false)
					.addCriterion("dieselgenerator",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/diesel_generator"),
									ItemPredicate.Builder.item().of(Tools.HAMMER).build()))
					.save(consumer, "immersiveengineering:main/mb_dieselgen");

			Advancement excavator = advancement(steel, Multiblocks.EXCAVATOR, "mb_excavator", FrameType.CHALLENGE, true, true, false)
					.addCriterion("excavator",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/excavator"),
									ItemPredicate.Builder.item().of(Tools.HAMMER).build()))
					.save(consumer, "immersiveengineering:main/mb_excavator");

			Advancement arcfurnace = advancement(steel, Multiblocks.ARC_FURNACE, "mb_arcfurnace", FrameType.CHALLENGE, true, true, false)
					.addCriterion("arcfurnace",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/arcfurnace"),
									ItemPredicate.Builder.item().of(Tools.HAMMER).build()))
					.save(consumer, "immersiveengineering:main/mb_arcfurnace");

			Advancement birthdayparty = advancement(revolver, Misc.ICON_BIRTHDAY, "secret_birthdayparty", FrameType.CHALLENGE, true, true, true)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.rewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_masterwork")))
					.save(consumer, "immersiveengineering:main/secret_birthdayparty");

			Advancement luckofthedraw = advancement(rtfm, Misc.ICON_LUCKY, "secret_luckofthedraw", FrameType.CHALLENGE, true, true, true)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.rewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_masterwork")))
					.save(consumer, "immersiveengineering:main/secret_luckofthedraw");

			Advancement drillbreak = advancement(drill, Misc.ICON_DRILLBREAK, "secret_drillbreak", FrameType.CHALLENGE, true, true, true)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.rewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_masterwork")))
					.save(consumer, "immersiveengineering:main/secret_drillbreak");

			Advancement ravenholm = advancement(railgun, Misc.ICON_RAVENHOLM, "secret_ravenholm", FrameType.CHALLENGE, true, true, true)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.rewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_masterwork")))
					.save(consumer, "immersiveengineering:main/secret_ravenholm");
		}
	}

	private static Path createPath(Path pathIn, Advancement advancementIn)
	{
		return pathIn.resolve("data/"+advancementIn.getId().getNamespace()+"/advancements/"+advancementIn.getId().getPath()+".json");
	}

	protected static Advancement.Builder advancement(Advancement parent, ItemLike display, String name, FrameType frame, boolean showToast, boolean announceToChat, boolean hidden)
	{
		return Advancement.Builder.advancement().parent(parent).display(display,
				Component.translatable("advancement.immersiveengineering."+name),
				Component.translatable("advancement.immersiveengineering."+name+".desc"),
				null, frame, showToast, announceToChat, hidden);
	}

	protected static Advancement.Builder advancement(Advancement parent, ItemStack display, String name, FrameType frame, boolean showToast, boolean announceToChat, boolean hidden)
	{
		return Advancement.Builder.advancement().parent(parent).display(display,
				Component.translatable("advancement.immersiveengineering."+name),
				Component.translatable("advancement.immersiveengineering."+name+".desc"),
				null, frame, showToast, announceToChat, hidden);
	}

	protected static AdvancementRewards reward(ResourceLocation loot)
	{
		return new AdvancementRewards(0, new ResourceLocation[]{loot}, new ResourceLocation[0], CommandFunction.CacheableFunction.NONE);
	}

}
