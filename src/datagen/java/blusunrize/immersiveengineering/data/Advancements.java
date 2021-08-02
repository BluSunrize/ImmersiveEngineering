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
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.metal.ConveyorBlock;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.BasicConveyor;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.register.IEBlocks.BlockEntry;
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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.io.IOException;
import java.nio.file.Path;
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
					.display(Tools.manual,
							new TranslatableComponent("advancement.immersiveengineering.root"),
							new TranslatableComponent("advancement.immersiveengineering.root.desc"),
							new ResourceLocation("immersiveengineering:textures/block/wooden_decoration/treated_wood.png"),
							FrameType.TASK, true, true, false)
					.addCriterion("manual", InventoryChangeTrigger.TriggerInstance.hasItems(Tools.manual))
					.save(consumer, "immersiveengineering:main/root");

			Advancement hammer = advancement(rtfm, Tools.hammer, "craft_hammer", FrameType.TASK, true, true, false)
					.addCriterion("hammer", InventoryChangeTrigger.TriggerInstance.hasItems(Tools.hammer))
					.save(consumer, "immersiveengineering:main/craft_hammer");

			Advancement wire = advancement(rtfm, Misc.wireCoils.get(WireType.COPPER), "connect_wire", FrameType.TASK, true, true, false)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.save(consumer, "immersiveengineering:main/connect_wire");

			Advancement blastfurnace = advancement(hammer, Multiblocks.blastFurnace, "mb_blastfurnace", FrameType.GOAL, true, true, false)
					.addCriterion("blastfurnace",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/blast_furnace"),
									ItemPredicate.Builder.item().of(Tools.hammer).build()))
					.save(consumer, "immersiveengineering:main/mb_blastfurnace");

			Advancement steel = advancement(blastfurnace, Metals.ingots.get(EnumMetals.STEEL), "make_steel", FrameType.TASK, true, true, false)
					.addCriterion("steel", InventoryChangeTrigger.TriggerInstance.hasItems(Metals.ingots.get(EnumMetals.STEEL)))
					.save(consumer, "immersiveengineering:main/make_steel");

			Advancement.Builder b_conveyor = advancement(rtfm, MetalDevices.CONVEYORS.get(BasicConveyor.NAME), "place_conveyor", FrameType.TASK, true, true, false)
					.requirements(RequirementsStrategy.OR);
			for(Entry<ResourceLocation, BlockEntry<ConveyorBlock>> entry : MetalDevices.CONVEYORS.entrySet())
				b_conveyor.addCriterion(entry.getKey().getPath(), PlacedBlockTrigger.TriggerInstance.placedBlock(entry.getValue().get()));
			Advancement conveyor = b_conveyor.save(consumer, "immersiveengineering:main/place_conveyor");

			Advancement windmill = advancement(rtfm, WoodenDevices.windmill, "place_windmill", FrameType.TASK, true, true, false)
					.addCriterion("windmill", PlacedBlockTrigger.TriggerInstance.placedBlock(WoodenDevices.windmill.get()))
					.save(consumer, "immersiveengineering:main/place_windmill");

			Advancement heater = advancement(wire, MetalDevices.furnaceHeater, "craft_heater", FrameType.TASK, true, true, false)
					.addCriterion("heater", InventoryChangeTrigger.TriggerInstance.hasItems(MetalDevices.furnaceHeater))
					.save(consumer, "immersiveengineering:main/craft_heater");

			Advancement pump = advancement(wire, MetalDevices.fluidPump, "craft_pump", FrameType.TASK, true, true, false)
					.addCriterion("pump", InventoryChangeTrigger.TriggerInstance.hasItems(MetalDevices.fluidPump))
					.save(consumer, "immersiveengineering:main/craft_pump");

			Advancement floodlight = advancement(wire, MetalDevices.floodlight, "place_floodlight", FrameType.TASK, true, true, false)
					.addCriterion("floodlight", PlacedBlockTrigger.TriggerInstance.placedBlock(MetalDevices.floodlight.get()))
					.save(consumer, "immersiveengineering:main/place_floodlight");

			Advancement workbench = advancement(rtfm, WoodenDevices.workbench, "craft_workbench", FrameType.TASK, true, true, false)
					.addCriterion("workbench", InventoryChangeTrigger.TriggerInstance.hasItems(WoodenDevices.workbench))
					.save(consumer, "immersiveengineering:main/craft_workbench");

			Advancement revolver = advancement(workbench, Weapons.revolver, "craft_revolver", FrameType.TASK, true, true, false)
					.addCriterion("revolver", InventoryChangeTrigger.TriggerInstance.hasItems(Weapons.revolver))
					.save(consumer, "immersiveengineering:main/craft_revolver");

			ItemStack upgradedRevolver = new ItemStack(Weapons.revolver);
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

			Advancement drill = advancement(workbench, Tools.drill, "craft_drill", FrameType.TASK, true, true, false)
					.addCriterion("drill", InventoryChangeTrigger.TriggerInstance.hasItems(Tools.drill))
					.save(consumer, "immersiveengineering:main/craft_drill");

			ItemStack upgradedDrill = new ItemStack(Tools.drill);
			upgrades = new CompoundTag();
			upgrades.putInt("damage", 3);
			upgrades.putBoolean("waterproof", true);
			upgrades.putBoolean("oiled", true);
			upgrades.putFloat("speed", 6.0f);
			ItemNBTHelper.setTagCompound(upgradedDrill, "upgrades", upgrades);
			ItemNBTHelper.setItemStack(upgradedDrill, "head", new ItemStack(Tools.drillheadSteel));
			Advancement upgradeDrill = advancement(drill, upgradedDrill, "upgrade_drill", FrameType.CHALLENGE, true, true, false)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.rewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_rare")))
					.save(consumer, "immersiveengineering:main/upgrade_drill");

			Advancement buzzsaw = advancement(workbench, Tools.buzzsaw, "craft_buzzsaw", FrameType.TASK, true, true, false)
					.addCriterion("buzzsaw", InventoryChangeTrigger.TriggerInstance.hasItems(Tools.buzzsaw))
					.save(consumer, "immersiveengineering:main/craft_buzzsaw");

			ItemStack upgradedBuzzsaw = new ItemStack(Tools.buzzsaw);
			upgrades = new CompoundTag();
			upgrades.putBoolean("oiled", true);
			upgrades.putBoolean("spareblades", true);
			ItemNBTHelper.setTagCompound(upgradedBuzzsaw, "upgrades", upgrades);
			ItemNBTHelper.setItemStack(upgradedBuzzsaw, "sawblade", new ItemStack(Tools.sawblade));
			ItemNBTHelper.setItemStack(upgradedBuzzsaw, "sawblade_spare1", new ItemStack(Tools.sawblade));
			ItemNBTHelper.setItemStack(upgradedBuzzsaw, "sawblade_spare2", new ItemStack(Tools.sawblade));
			Advancement upgradeBuzzsaw = advancement(buzzsaw, upgradedBuzzsaw, "upgrade_buzzsaw", FrameType.CHALLENGE, true, true, false)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.rewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_rare")))
					.save(consumer, "immersiveengineering:main/upgrade_buzzsaw");

			Advancement skyhook = advancement(workbench, Misc.skyhook, "craft_skyhook", FrameType.TASK, true, true, false)
					.addCriterion("skyhook", InventoryChangeTrigger.TriggerInstance.hasItems(Misc.skyhook))
					.save(consumer, "immersiveengineering:main/craft_skyhook");

			//Todo: Advancement for traveling 1km by skyhook

			Advancement chemthrower = advancement(workbench, Weapons.chemthrower, "craft_chemthrower", FrameType.TASK, true, true, false)
					.addCriterion("chemthrower", InventoryChangeTrigger.TriggerInstance.hasItems(Weapons.chemthrower))
					.save(consumer, "immersiveengineering:main/craft_chemthrower");

			Advancement railgun = advancement(workbench, Weapons.railgun, "craft_railgun", FrameType.TASK, true, true, false)
					.addCriterion("railgun", InventoryChangeTrigger.TriggerInstance.hasItems(Weapons.railgun))
					.save(consumer, "immersiveengineering:main/craft_railgun");

			ItemStack upgradedRailgun = new ItemStack(Weapons.railgun);
			upgrades = new CompoundTag();
			upgrades.putBoolean("scope", true);
			upgrades.putFloat("speed", 1f);
			ItemNBTHelper.setTagCompound(upgradedRailgun, "upgrades", upgrades);
			Advancement upgradeRailgun = advancement(railgun, upgradedRailgun, "upgrade_railgun", FrameType.CHALLENGE, true, true, false)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.rewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_rare")))
					.save(consumer, "immersiveengineering:main/upgrade_railgun");

			Advancement improvedblastfurnace = advancement(steel, Multiblocks.blastFurnaceAdv, "mb_improvedblastfurnace", FrameType.TASK, true, true, false)
					.addCriterion("improvedblastfurnace",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/improved_blast_furnace"),
									ItemPredicate.Builder.item().of(Tools.hammer).build()))
					.save(consumer, "immersiveengineering:main/mb_improvedblastfurnace");

			Advancement metalpress = advancement(steel, Multiblocks.metalPress, "mb_metalpress", FrameType.TASK, true, true, false)
					.addCriterion("metalpress",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/metal_press"),
									ItemPredicate.Builder.item().of(Tools.hammer).build()))
					.save(consumer, "immersiveengineering:main/mb_metalpress");

			Advancement silo = advancement(steel, Multiblocks.silo, "mb_silo", FrameType.TASK, true, true, false)
					.addCriterion("silo",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/silo"),
									ItemPredicate.Builder.item().of(Tools.hammer).build()))
					.save(consumer, "immersiveengineering:main/mb_silo");

			Advancement crusher = advancement(steel, Multiblocks.crusher, "mb_crusher", FrameType.GOAL, true, true, false)
					.addCriterion("crusher",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/crusher"),
									ItemPredicate.Builder.item().of(Tools.hammer).build()))
					.save(consumer, "immersiveengineering:main/mb_crusher");

			Advancement dieselgen = advancement(steel, Multiblocks.dieselGenerator, "mb_dieselgen", FrameType.GOAL, true, true, false)
					.addCriterion("dieselgenerator",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/diesel_generator"),
									ItemPredicate.Builder.item().of(Tools.hammer).build()))
					.save(consumer, "immersiveengineering:main/mb_dieselgen");

			Advancement excavator = advancement(steel, Multiblocks.excavator, "mb_excavator", FrameType.CHALLENGE, true, true, false)
					.addCriterion("excavator",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/excavator"),
									ItemPredicate.Builder.item().of(Tools.hammer).build()))
					.save(consumer, "immersiveengineering:main/mb_excavator");

			Advancement arcfurnace = advancement(steel, Multiblocks.arcFurnace, "mb_arcfurnace", FrameType.CHALLENGE, true, true, false)
					.addCriterion("arcfurnace",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/arcfurnace"),
									ItemPredicate.Builder.item().of(Tools.hammer).build()))
					.save(consumer, "immersiveengineering:main/mb_arcfurnace");

			Advancement birthdayparty = advancement(revolver, Misc.iconBirthday, "secret_birthdayparty", FrameType.CHALLENGE, true, true, true)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.rewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_masterwork")))
					.save(consumer, "immersiveengineering:main/secret_birthdayparty");

			Advancement luckofthedraw = advancement(rtfm, Misc.iconLucky, "secret_luckofthedraw", FrameType.CHALLENGE, true, true, true)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.rewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_masterwork")))
					.save(consumer, "immersiveengineering:main/secret_luckofthedraw");

			Advancement drillbreak = advancement(drill, Misc.iconDrillbreak, "secret_drillbreak", FrameType.CHALLENGE, true, true, true)
					.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance())
					.rewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_masterwork")))
					.save(consumer, "immersiveengineering:main/secret_drillbreak");

			Advancement ravenholm = advancement(railgun, Misc.iconRavenholm, "secret_ravenholm", FrameType.CHALLENGE, true, true, true)
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
				new TranslatableComponent("advancement.immersiveengineering."+name),
				new TranslatableComponent("advancement.immersiveengineering."+name+".desc"),
				null, frame, showToast, announceToChat, hidden);
	}

	protected static Advancement.Builder advancement(Advancement parent, ItemStack display, String name, FrameType frame, boolean showToast, boolean announceToChat, boolean hidden)
	{
		return Advancement.Builder.advancement().parent(parent).display(display,
				new TranslatableComponent("advancement.immersiveengineering."+name),
				new TranslatableComponent("advancement.immersiveengineering."+name+".desc"),
				null, frame, showToast, announceToChat, hidden);
	}

	protected static AdvancementRewards reward(ResourceLocation loot)
	{
		return new AdvancementRewards(0, new ResourceLocation[]{loot}, new ResourceLocation[0], CommandFunction.CacheableFunction.NONE);
	}

}
