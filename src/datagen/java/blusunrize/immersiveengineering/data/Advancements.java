/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockAdvancementTrigger;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.conveyor.IConveyorType;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.BasicConveyor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.register.IEBlocks.BlockEntry;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.register.IEItems.Metals;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.register.IEItems.Weapons;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.*;
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
			/* MAIN */
			AdvBuilder.setPage("main");

			Advancement rtfm = AdvBuilder.root("block/wooden_decoration/treated_wood").getItem(Tools.MANUAL).save(consumer);

			Advancement hammer = AdvBuilder.child("craft_hammer", rtfm).getItem(Tools.HAMMER).save(consumer);

			Advancement wire = AdvBuilder.child("connect_wire", rtfm).icon(Misc.WIRE_COILS.get(WireType.COPPER)).codeTriggered().save(consumer);

			Advancement blastfurnace = AdvBuilder.child("mb_blastfurnace", hammer).goal()
					.multiblock(IEMultiblocks.BLAST_FURNACE).save(consumer);

			Advancement steel = AdvBuilder.child("make_steel", blastfurnace).getItem(Metals.INGOTS.get(EnumMetals.STEEL)).save(consumer);

			AdvBuilder b_conveyor = AdvBuilder.child("place_conveyor", rtfm).icon(MetalDevices.CONVEYORS.get(BasicConveyor.TYPE))
					.orRequirements();
			MetalDevices.CONVEYORS.entrySet().stream()
					.sorted(Comparator.comparing((Entry<IConveyorType<?>, ?> e) -> e.getKey().getId()))
					.forEach(e -> {
						String name = e.getKey().getId().getPath();
						b_conveyor.addCriterion(
								name, PlacedBlockTrigger.TriggerInstance.placedBlock(e.getValue().get())
						);
					});
			Advancement conveyor = b_conveyor.save(consumer);

			Advancement windmill = AdvBuilder.child("place_windmill", rtfm).placeBlock(WoodenDevices.WINDMILL).save(consumer);

			Advancement heater = AdvBuilder.child("craft_heater", wire).getItem(MetalDevices.FURNACE_HEATER).save(consumer);

			Advancement pump = AdvBuilder.child("craft_pump", wire).getItem(MetalDevices.FLUID_PUMP).save(consumer);

			Advancement floodlight = AdvBuilder.child("place_floodlight", wire).placeBlock(MetalDevices.FLOODLIGHT).save(consumer);

			Advancement workbench = AdvBuilder.child("craft_workbench", rtfm).getItem(WoodenDevices.WORKBENCH).save(consumer);

			Advancement improvedblastfurnace = AdvBuilder.child("mb_improvedblastfurnace", steel)
					.multiblock(IEMultiblocks.ADVANCED_BLAST_FURNACE).save(consumer);

			Advancement metalpress = AdvBuilder.child("mb_metalpress", steel)
					.multiblock(IEMultiblocks.METAL_PRESS).save(consumer);

			Advancement silo = AdvBuilder.child("mb_silo", steel)
					.multiblock(IEMultiblocks.SILO).save(consumer);

			Advancement crusher = AdvBuilder.child("mb_crusher", steel).goal()
					.multiblock(IEMultiblocks.CRUSHER).save(consumer);

			Advancement dieselgen = AdvBuilder.child("mb_dieselgen", steel).goal()
					.multiblock(IEMultiblocks.DIESEL_GENERATOR).save(consumer);

			Advancement excavator = AdvBuilder.child("mb_excavator", steel).challenge()
					.multiblock(IEMultiblocks.EXCAVATOR).save(consumer);

			Advancement arcfurnace = AdvBuilder.child("mb_arcfurnace", steel).challenge()
					.multiblock(IEMultiblocks.ARC_FURNACE).save(consumer);

			Advancement luckofthedraw = AdvBuilder.child("secret_luckofthedraw", rtfm).challenge().hidden()
					.icon(Misc.ICON_LUCKY).codeTriggered().loot("shader_masterwork").save(consumer);


			/* TOOLS */
			AdvBuilder.setPage("tools");

			Advancement toolsRoot = AdvBuilder.root("block/wooden_decoration/treated_wood").quiet()
					.getItem(WoodenDevices.WORKBENCH).save(consumer);

			Advancement revolver = AdvBuilder.child("craft_revolver", toolsRoot).getItem(Weapons.REVOLVER).save(consumer);

			ItemStack upgradedRevolver = new ItemStack(Weapons.REVOLVER);
			CompoundTag upgrades = new CompoundTag();
			upgrades.putInt("bullets", 6);
			upgrades.putBoolean("electro", true);
			ItemNBTHelper.setTagCompound(upgradedRevolver, "upgrades", upgrades);
			Advancement upgradeRevolver = AdvBuilder.child("upgrade_revolver", revolver).challenge()
					.icon(upgradedRevolver).codeTriggered().loot("shader_rare").save(consumer);

			Advancement wolfpack = AdvBuilder.child("craft_wolfpack", revolver).hidden()
					.getItem(BulletHandler.getBulletItem(BulletItem.WOLFPACK)).save(consumer);

			Advancement drill = AdvBuilder.child("craft_drill", toolsRoot).getItem(Tools.DRILL).save(consumer);

			ItemStack upgradedDrill = new ItemStack(Tools.DRILL);
			upgrades = new CompoundTag();
			upgrades.putInt("damage", 3);
			upgrades.putBoolean("waterproof", true);
			upgrades.putBoolean("oiled", true);
			upgrades.putFloat("speed", 6.0f);
			ItemNBTHelper.setTagCompound(upgradedDrill, "upgrades", upgrades);
			ItemNBTHelper.setItemStack(upgradedDrill, "head", new ItemStack(Tools.DRILLHEAD_STEEL));
			Advancement upgradeDrill = AdvBuilder.child("upgrade_drill", drill).challenge()
					.icon(upgradedDrill).codeTriggered().loot("shader_rare").save(consumer);

			Advancement buzzsaw = AdvBuilder.child("craft_buzzsaw", toolsRoot).getItem(Tools.BUZZSAW).save(consumer);

			ItemStack upgradedBuzzsaw = new ItemStack(Tools.BUZZSAW);
			upgrades = new CompoundTag();
			upgrades.putBoolean("oiled", true);
			upgrades.putBoolean("spareblades", true);
			ItemNBTHelper.setTagCompound(upgradedBuzzsaw, "upgrades", upgrades);
			ItemNBTHelper.setItemStack(upgradedBuzzsaw, "sawblade", new ItemStack(Tools.SAWBLADE));
			ItemNBTHelper.setItemStack(upgradedBuzzsaw, "sawblade_spare1", new ItemStack(Tools.SAWBLADE));
			ItemNBTHelper.setItemStack(upgradedBuzzsaw, "sawblade_spare2", new ItemStack(Tools.SAWBLADE));
			Advancement upgradeBuzzsaw = AdvBuilder.child("upgrade_buzzsaw", buzzsaw).challenge()
					.icon(upgradedBuzzsaw).codeTriggered().loot("shader_rare").save(consumer);

			Advancement skyhook = AdvBuilder.child("craft_skyhook", toolsRoot).getItem(Misc.SKYHOOK).save(consumer);

			//Todo: Advancement for traveling 1km by skyhook

			Advancement chemthrower = AdvBuilder.child("craft_chemthrower", toolsRoot).getItem(Weapons.CHEMTHROWER).save(consumer);

			Advancement railgun = AdvBuilder.child("craft_railgun", toolsRoot).getItem(Weapons.RAILGUN).save(consumer);

			ItemStack upgradedRailgun = new ItemStack(Weapons.RAILGUN);
			upgrades = new CompoundTag();
			upgrades.putBoolean("scope", true);
			upgrades.putFloat("speed", 1f);
			ItemNBTHelper.setTagCompound(upgradedRailgun, "upgrades", upgrades);
			Advancement upgradeRailgun = AdvBuilder.child("upgrade_railgun", railgun).challenge()
					.icon(upgradedRailgun).codeTriggered().loot("shader_rare").save(consumer);

			Advancement birthdayparty = AdvBuilder.child("secret_birthdayparty", upgradeRevolver).challenge().hidden()
					.icon(Misc.ICON_BIRTHDAY).codeTriggered().loot("shader_masterwork").save(consumer);

			Advancement drillbreak = AdvBuilder.child("secret_drillbreak", upgradeDrill).challenge().hidden()
					.icon(Misc.ICON_DRILLBREAK).codeTriggered().loot("shader_masterwork").save(consumer);

			Advancement ravenholm = AdvBuilder.child("secret_ravenholm", upgradeRailgun).challenge().hidden()
					.icon(Misc.ICON_RAVENHOLM).codeTriggered().loot("shader_masterwork").save(consumer);
		}
	}

	private static Path createPath(Path pathIn, Advancement advancementIn)
	{
		return pathIn.resolve("data/"+advancementIn.getId().getNamespace()+"/advancements/"+advancementIn.getId().getPath()+".json");
	}

	static class AdvBuilder
	{
		public static String page = null;

		private final Advancement.Builder builder = Advancement.Builder.advancement();

		private final String name;
		private ItemStack icon;
		private ResourceLocation background = null;
		private FrameType frame = FrameType.TASK;
		private boolean hidden = false;

		private boolean quiet = false;

		private AdvBuilder(String name)
		{
			assert page!=null;
			this.name = name;
		}

		public static void setPage(String page)
		{
			AdvBuilder.page = page;
		}

		public static AdvBuilder root(String bg)
		{
			return new AdvBuilder(page+"_root").background(new ResourceLocation(Lib.MODID, "textures/"+bg+".png"));
		}

		public static AdvBuilder child(String name, Advancement parent)
		{
			return new AdvBuilder(name).parent(parent);
		}

		public AdvBuilder getItem(ItemLike item)
		{
			return this.icon(item).hasItems(item);
		}

		public AdvBuilder placeBlock(BlockEntry<?> block)
		{
			return this.icon(block).addCriterion("place_block", PlacedBlockTrigger.TriggerInstance.placedBlock(block.get()));
		}

		public AdvBuilder multiblock(IETemplateMultiblock multiblock)
		{
			return this.icon(multiblock.getBlock()).addCriterion("form_multiblock", MultiblockAdvancementTrigger.create(
					multiblock.getUniqueName(),
					ItemPredicate.Builder.item().of(Tools.HAMMER).build())
			);
		}

		private AdvBuilder parent(Advancement parent)
		{
			this.builder.parent(parent);
			return this;
		}

		private AdvBuilder background(ResourceLocation background)
		{
			this.background = background;
			return this;
		}


		public AdvBuilder icon(ItemStack icon)
		{
			this.icon = icon;
			return this;
		}

		public AdvBuilder icon(ItemLike icon)
		{
			return this.icon(new ItemStack(icon));
		}

		public AdvBuilder goal()
		{
			this.frame = FrameType.GOAL;
			return this;
		}

		public AdvBuilder challenge()
		{
			this.frame = FrameType.CHALLENGE;
			return this;
		}

		public AdvBuilder hidden()
		{
			this.hidden = true;
			return this;
		}

		public AdvBuilder quiet()
		{
			this.quiet = true;
			return this;
		}

		public AdvBuilder loot(String lootPath)
		{
			this.builder.rewards(new AdvancementRewards(
					0,
					new ResourceLocation[]{new ResourceLocation(Lib.MODID, "advancements/"+lootPath)},
					new ResourceLocation[0],
					CommandFunction.CacheableFunction.NONE)
			);
			return this;
		}

		public AdvBuilder addCriterion(String key, CriterionTriggerInstance criterion)
		{
			this.builder.addCriterion(key, criterion);
			return this;
		}

		public AdvBuilder orRequirements()
		{
			this.builder.requirements(RequirementsStrategy.OR);
			return this;
		}

		public AdvBuilder hasItems(ItemLike... items)
		{
			return this.addCriterion("has_item", InventoryChangeTrigger.TriggerInstance.hasItems(items));
		}

		public AdvBuilder codeTriggered()
		{
			return this.addCriterion("code_trigger", new ImpossibleTrigger.TriggerInstance());
		}

		private Advancement.Builder withDisplay()
		{
			return this.builder.display(new DisplayInfo(
					this.icon,
					new TranslatableComponent("advancement.immersiveengineering."+this.name),
					new TranslatableComponent("advancement.immersiveengineering."+this.name+".desc"),
					this.background,
					this.frame,
					!this.quiet,
					!this.quiet,
					this.hidden
			));
		}

		public Advancement save(Consumer<Advancement> consumer)
		{
			return this.withDisplay().save(consumer, Lib.MODID+":"+AdvBuilder.page+"/"+this.name);
		}

	}

}
