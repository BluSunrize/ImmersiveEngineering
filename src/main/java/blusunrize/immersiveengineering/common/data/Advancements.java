/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.BasicConveyor;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.items.IEItems.Weapons;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.advancements.MultiblockTrigger;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.IRequirementsStrategy;
import net.minecraft.advancements.criterion.ImpossibleTrigger;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.PlacedBlockTrigger;
import net.minecraft.block.Block;
import net.minecraft.command.FunctionObject;
import net.minecraft.data.AdvancementProvider;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TranslationTextComponent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
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
	public void act(DirectoryCache cache) throws IOException
	{
		Set<ResourceLocation> set = Sets.newHashSet();
		Consumer<Advancement> consumer = (p_204017_3_) -> {
			if(!set.add(p_204017_3_.getId()))
			{
				throw new IllegalStateException("Duplicate advancement "+p_204017_3_.getId());
			}
			else
			{
				Path path1 = getPath(OUTPUT, p_204017_3_);

				try
				{
					IDataProvider.save(GSON, cache, p_204017_3_.copy().serialize(), path1);
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

			Advancement rtfm = Advancement.Builder.builder()
					.withDisplay(Tools.manual,
							new TranslationTextComponent("advancement.immersiveengineering.root"),
							new TranslationTextComponent("advancement.immersiveengineering.root.desc"),
							new ResourceLocation("immersiveengineering:textures/block/wooden_decoration/treated_wood.png"),
							FrameType.TASK, true, true, false)
					.withCriterion("manual", InventoryChangeTrigger.Instance.forItems(Tools.manual))
					.register(consumer, "immersiveengineering:main/root");

			Advancement hammer = advancement(rtfm, Tools.hammer, "craft_hammer", FrameType.TASK, true, true, false)
					.withCriterion("hammer", InventoryChangeTrigger.Instance.forItems(Tools.hammer))
					.register(consumer, "immersiveengineering:main/craft_hammer");

			Advancement wire = advancement(rtfm, Misc.wireCoils.get(WireType.COPPER), "connect_wire", FrameType.TASK, true, true, false)
					.withCriterion("code_trigger", new ImpossibleTrigger.Instance())
					.register(consumer, "immersiveengineering:main/connect_wire");

			Advancement blastfurnace = advancement(hammer, Multiblocks.blastFurnace, "mb_blastfurnace", FrameType.GOAL, true, true, false)
					.withCriterion("blastfurnace",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/blast_furnace"),
									ItemPredicate.Builder.create().item(Tools.hammer).build()))
					.register(consumer, "immersiveengineering:main/mb_blastfurnace");

			Advancement steel = advancement(blastfurnace, Metals.ingots.get(EnumMetals.STEEL), "make_steel", FrameType.TASK, true, true, false)
					.withCriterion("steel", InventoryChangeTrigger.Instance.forItems(Metals.ingots.get(EnumMetals.STEEL)))
					.register(consumer, "immersiveengineering:main/make_steel");

			Advancement.Builder b_conveyor = advancement(rtfm, MetalDevices.CONVEYORS.get(BasicConveyor.NAME), "place_conveyor", FrameType.TASK, true, true, false)
					.withRequirementsStrategy(IRequirementsStrategy.OR);
			for(Map.Entry<ResourceLocation, Block> entry : MetalDevices.CONVEYORS.entrySet())
				b_conveyor.withCriterion(entry.getKey().getPath(), PlacedBlockTrigger.Instance.placedBlock(entry.getValue()));
			Advancement conveyor = b_conveyor.register(consumer, "immersiveengineering:main/place_conveyor");

			Advancement windmill = advancement(rtfm, WoodenDevices.windmill, "place_windmill", FrameType.TASK, true, true, false)
					.withCriterion("windmill", PlacedBlockTrigger.Instance.placedBlock(WoodenDevices.windmill))
					.register(consumer, "immersiveengineering:main/place_windmill");

			Advancement heater = advancement(wire, MetalDevices.furnaceHeater, "craft_heater", FrameType.TASK, true, true, false)
					.withCriterion("heater", InventoryChangeTrigger.Instance.forItems(MetalDevices.furnaceHeater))
					.register(consumer, "immersiveengineering:main/craft_heater");

			Advancement pump = advancement(wire, MetalDevices.fluidPump, "craft_pump", FrameType.TASK, true, true, false)
					.withCriterion("pump", InventoryChangeTrigger.Instance.forItems(MetalDevices.fluidPump))
					.register(consumer, "immersiveengineering:main/craft_pump");

			Advancement floodlight = advancement(wire, MetalDevices.floodlight, "place_floodlight", FrameType.TASK, true, true, false)
					.withCriterion("floodlight", PlacedBlockTrigger.Instance.placedBlock(MetalDevices.floodlight))
					.register(consumer, "immersiveengineering:main/place_floodlight");

			Advancement workbench = advancement(rtfm, WoodenDevices.workbench, "craft_workbench", FrameType.TASK, true, true, false)
					.withCriterion("workbench", InventoryChangeTrigger.Instance.forItems(WoodenDevices.workbench))
					.register(consumer, "immersiveengineering:main/craft_workbench");

			Advancement revolver = advancement(workbench, Weapons.revolver, "craft_revolver", FrameType.TASK, true, true, false)
					.withCriterion("revolver", InventoryChangeTrigger.Instance.forItems(Weapons.revolver))
					.register(consumer, "immersiveengineering:main/craft_revolver");

			ItemStack upgradedRevolver = new ItemStack(Weapons.revolver);
			CompoundNBT upgrades = new CompoundNBT();
			upgrades.putInt("bullets", 6);
			upgrades.putBoolean("electro", true);
			ItemNBTHelper.setTagCompound(upgradedRevolver, "upgrades", upgrades);
			Advancement upgrade_revolver = advancement(revolver, upgradedRevolver, "upgrade_revolver", FrameType.CHALLENGE, true, true, false)
					.withCriterion("code_trigger", new ImpossibleTrigger.Instance())
					.withRewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_rare")))
					.register(consumer, "immersiveengineering:main/upgrade_revolver");

			Advancement wolfpack = advancement(revolver, BulletHandler.getBulletItem(BulletItem.WOLFPACK), "craft_wolfpack", FrameType.TASK, true, true, true)
					.withCriterion("wolfpack", InventoryChangeTrigger.Instance.forItems(BulletHandler.getBulletItem(BulletItem.WOLFPACK)))
					.register(consumer, "immersiveengineering:main/craft_wolfpack");

			Advancement drill = advancement(workbench, Tools.drill, "craft_drill", FrameType.TASK, true, true, false)
					.withCriterion("drill", InventoryChangeTrigger.Instance.forItems(Tools.drill))
					.register(consumer, "immersiveengineering:main/craft_drill");

			ItemStack upgradedDrill = new ItemStack(Tools.drill);
			upgrades = new CompoundNBT();
			upgrades.putInt("damage", 3);
			upgrades.putBoolean("waterproof", true);
			upgrades.putBoolean("oiled", true);
			upgrades.putFloat("speed", 6.0f);
			ItemNBTHelper.setTagCompound(upgradedDrill, "upgrades", upgrades);
			ItemNBTHelper.setItemStack(upgradedDrill, "head", new ItemStack(Tools.drillheadSteel));
			Advancement upgradeDrill = advancement(drill, upgradedDrill, "upgrade_drill", FrameType.CHALLENGE, true, true, false)
					.withCriterion("code_trigger", new ImpossibleTrigger.Instance())
					.withRewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_rare")))
					.register(consumer, "immersiveengineering:main/upgrade_drill");

			Advancement buzzsaw = advancement(workbench, Tools.buzzsaw, "craft_buzzsaw", FrameType.TASK, true, true, false)
					.withCriterion("buzzsaw", InventoryChangeTrigger.Instance.forItems(Tools.buzzsaw))
					.register(consumer, "immersiveengineering:main/craft_buzzsaw");

			ItemStack upgradedBuzzsaw = new ItemStack(Tools.buzzsaw);
			upgrades = new CompoundNBT();
			upgrades.putBoolean("oiled", true);
			upgrades.putBoolean("spareblades", true);
			ItemNBTHelper.setTagCompound(upgradedBuzzsaw, "upgrades", upgrades);
			ItemNBTHelper.setItemStack(upgradedBuzzsaw, "sawblade", new ItemStack(Tools.sawblade));
			ItemNBTHelper.setItemStack(upgradedBuzzsaw, "sawblade_spare1", new ItemStack(Tools.sawblade));
			ItemNBTHelper.setItemStack(upgradedBuzzsaw, "sawblade_spare2", new ItemStack(Tools.sawblade));
			Advancement upgradeBuzzsaw = advancement(buzzsaw, upgradedBuzzsaw, "upgrade_buzzsaw", FrameType.CHALLENGE, true, true, false)
					.withCriterion("code_trigger", new ImpossibleTrigger.Instance())
					.withRewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_rare")))
					.register(consumer, "immersiveengineering:main/upgrade_buzzsaw");

			Advancement skyhook = advancement(workbench, Misc.skyhook, "craft_skyhook", FrameType.TASK, true, true, false)
					.withCriterion("skyhook", InventoryChangeTrigger.Instance.forItems(Misc.skyhook))
					.register(consumer, "immersiveengineering:main/craft_skyhook");

			//Todo: Advancement for traveling 1km by skyhook

			Advancement chemthrower = advancement(workbench, Weapons.chemthrower, "craft_chemthrower", FrameType.TASK, true, true, false)
					.withCriterion("chemthrower", InventoryChangeTrigger.Instance.forItems(Weapons.chemthrower))
					.register(consumer, "immersiveengineering:main/craft_chemthrower");

			Advancement railgun = advancement(workbench, Weapons.railgun, "craft_railgun", FrameType.TASK, true, true, false)
					.withCriterion("railgun", InventoryChangeTrigger.Instance.forItems(Weapons.railgun))
					.register(consumer, "immersiveengineering:main/craft_railgun");

			ItemStack upgradedRailgun = new ItemStack(Weapons.railgun);
			upgrades = new CompoundNBT();
			upgrades.putBoolean("scope", true);
			upgrades.putFloat("speed", 1f);
			ItemNBTHelper.setTagCompound(upgradedRailgun, "upgrades", upgrades);
			Advancement upgradeRailgun = advancement(railgun, upgradedRailgun, "upgrade_railgun", FrameType.CHALLENGE, true, true, false)
					.withCriterion("code_trigger", new ImpossibleTrigger.Instance())
					.withRewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_rare")))
					.register(consumer, "immersiveengineering:main/upgrade_railgun");

			Advancement improvedblastfurnace = advancement(steel, Multiblocks.blastFurnaceAdv, "mb_improvedblastfurnace", FrameType.TASK, true, true, false)
					.withCriterion("improvedblastfurnace",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/improved_blast_furnace"),
									ItemPredicate.Builder.create().item(Tools.hammer).build()))
					.register(consumer, "immersiveengineering:main/mb_improvedblastfurnace");

			Advancement metalpress = advancement(steel, Multiblocks.metalPress, "mb_metalpress", FrameType.TASK, true, true, false)
					.withCriterion("metalpress",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/metal_press"),
									ItemPredicate.Builder.create().item(Tools.hammer).build()))
					.register(consumer, "immersiveengineering:main/mb_metalpress");

			Advancement silo = advancement(steel, Multiblocks.silo, "mb_silo", FrameType.TASK, true, true, false)
					.withCriterion("silo",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/silo"),
									ItemPredicate.Builder.create().item(Tools.hammer).build()))
					.register(consumer, "immersiveengineering:main/mb_silo");

			Advancement crusher = advancement(steel, Multiblocks.crusher, "mb_crusher", FrameType.GOAL, true, true, false)
					.withCriterion("crusher",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/crusher"),
									ItemPredicate.Builder.create().item(Tools.hammer).build()))
					.register(consumer, "immersiveengineering:main/mb_crusher");

			Advancement dieselgen = advancement(steel, Multiblocks.dieselGenerator, "mb_dieselgen", FrameType.GOAL, true, true, false)
					.withCriterion("dieselgenerator",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/diesel_generator"),
									ItemPredicate.Builder.create().item(Tools.hammer).build()))
					.register(consumer, "immersiveengineering:main/mb_dieselgen");

			Advancement excavator = advancement(steel, Multiblocks.excavator, "mb_excavator", FrameType.CHALLENGE, true, true, false)
					.withCriterion("excavator",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/excavator"),
									ItemPredicate.Builder.create().item(Tools.hammer).build()))
					.register(consumer, "immersiveengineering:main/mb_excavator");

			Advancement arcfurnace = advancement(steel, Multiblocks.arcFurnace, "mb_arcfurnace", FrameType.CHALLENGE, true, true, false)
					.withCriterion("arcfurnace",
							MultiblockTrigger.create(
									new ResourceLocation(ImmersiveEngineering.MODID, "multiblocks/arcfurnace"),
									ItemPredicate.Builder.create().item(Tools.hammer).build()))
					.register(consumer, "immersiveengineering:main/mb_arcfurnace");

			Advancement birthdayparty = advancement(revolver, Misc.iconBirthday, "secret_birthdayparty", FrameType.CHALLENGE, true, true, true)
					.withCriterion("code_trigger", new ImpossibleTrigger.Instance())
					.withRewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_masterwork")))
					.register(consumer, "immersiveengineering:main/secret_birthdayparty");

			Advancement luckofthedraw = advancement(rtfm, Misc.iconLucky, "secret_luckofthedraw", FrameType.CHALLENGE, true, true, true)
					.withCriterion("code_trigger", new ImpossibleTrigger.Instance())
					.withRewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_masterwork")))
					.register(consumer, "immersiveengineering:main/secret_luckofthedraw");

			Advancement drillbreak = advancement(drill, Misc.iconDrillbreak, "secret_drillbreak", FrameType.CHALLENGE, true, true, true)
					.withCriterion("code_trigger", new ImpossibleTrigger.Instance())
					.withRewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_masterwork")))
					.register(consumer, "immersiveengineering:main/secret_drillbreak");

			Advancement ravenholm = advancement(railgun, Misc.iconRavenholm, "secret_ravenholm", FrameType.CHALLENGE, true, true, true)
					.withCriterion("code_trigger", new ImpossibleTrigger.Instance())
					.withRewards(reward(new ResourceLocation("immersiveengineering", "advancements/shader_masterwork")))
					.register(consumer, "immersiveengineering:main/secret_ravenholm");
		}
	}

	private static Path getPath(Path pathIn, Advancement advancementIn)
	{
		return pathIn.resolve("data/"+advancementIn.getId().getNamespace()+"/advancements/"+advancementIn.getId().getPath()+".json");
	}

	protected static Advancement.Builder advancement(Advancement parent, IItemProvider display, String name, FrameType frame, boolean showToast, boolean announceToChat, boolean hidden)
	{
		return Advancement.Builder.builder().withParent(parent).withDisplay(display,
				new TranslationTextComponent("advancement.immersiveengineering."+name),
				new TranslationTextComponent("advancement.immersiveengineering."+name+".desc"),
				null, frame, showToast, announceToChat, hidden);
	}

	protected static Advancement.Builder advancement(Advancement parent, ItemStack display, String name, FrameType frame, boolean showToast, boolean announceToChat, boolean hidden)
	{
		return Advancement.Builder.builder().withParent(parent).withDisplay(display,
				new TranslationTextComponent("advancement.immersiveengineering."+name),
				new TranslationTextComponent("advancement.immersiveengineering."+name+".desc"),
				null, frame, showToast, announceToChat, hidden);
	}

	protected static AdvancementRewards reward(ResourceLocation loot)
	{
		return new AdvancementRewards(0, new ResourceLocation[]{loot}, new ResourceLocation[0], FunctionObject.CacheableFunction.EMPTY);
	}

}
