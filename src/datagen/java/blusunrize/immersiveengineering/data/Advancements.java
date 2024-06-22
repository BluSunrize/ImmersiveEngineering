/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.BasicConveyor;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.DropConveyor;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.ExtractConveyor;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.items.bullets.IEBullets;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEItems.*;
import blusunrize.immersiveengineering.common.register.IEPotions;
import blusunrize.immersiveengineering.common.world.Villages;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.*;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Advancements extends AdvancementProvider
{
	public Advancements(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, ExistingFileHelper exFileHelper)
	{
		super(output, provider, exFileHelper, List.of(Advancements::registerAdvancements));
	}

	private static void registerAdvancements(
			HolderLookup.Provider lookup, Consumer<AdvancementHolder> consumer, ExistingFileHelper existingFileHelper
	)
	{
		/* MAIN */
		AdvancementBuilder.setPage("main");
		AdvancementHolder rtfm = AdvancementBuilder.root("block/wooden_decoration/treated_wood").getItem(Tools.MANUAL).save(consumer);

		// Conveyors
		AdvancementHolder conveyor = AdvancementBuilder.child("place_conveyor", rtfm).icon(MetalDevices.CONVEYORS.get(BasicConveyor.TYPE))
				.orRequirements().placeBlocks(MetalDevices.CONVEYORS.values()).save(consumer);
		AdvancementHolder dropConveyor = AdvancementBuilder.child("craft_drop_conveyor", conveyor)
				.getItem(MetalDevices.CONVEYORS.get(DropConveyor.TYPE)).save(consumer);
		AdvancementHolder chute = AdvancementBuilder.child("chute_bonk", dropConveyor).icon(MetalDevices.CHUTES.get(EnumMetals.IRON))
				.codeTriggered().save(consumer);
		AdvancementHolder extractConveyor = AdvancementBuilder.child("craft_extract_conveyor", conveyor)
				.getItem(MetalDevices.CONVEYORS.get(ExtractConveyor.TYPE)).save(consumer);
		AdvancementHolder router = AdvancementBuilder.child("craft_router", extractConveyor)
				.getItem(WoodenDevices.SORTER).save(consumer);
		AdvancementHolder batcher = AdvancementBuilder.child("craft_batcher", router).goal()
				.getItem(WoodenDevices.ITEM_BATCHER).save(consumer);

		// Power Generation
		AdvancementHolder wire = AdvancementBuilder.child("connect_wire", rtfm).icon(Misc.WIRE_COILS.get(WireType.COPPER)).codeTriggered().save(consumer);
		AdvancementHolder dynamo = AdvancementBuilder.child("place_dynamo", wire).placeBlock(MetalDevices.DYNAMO).save(consumer);
		AdvancementHolder windmill = AdvancementBuilder.child("place_windmill", dynamo).placeBlock(WoodenDevices.WINDMILL).save(consumer);

		// Machines
		AdvancementHolder heater = AdvancementBuilder.child("craft_heater", wire).getItem(MetalDevices.FURNACE_HEATER).save(consumer);
		AdvancementHolder pump = AdvancementBuilder.child("craft_pump", heater).getItem(MetalDevices.FLUID_PUMP).save(consumer);
		AdvancementHolder cloche = AdvancementBuilder.child("chorus_cloche", pump).icon(MetalDevices.CLOCHE).codeTriggered().save(consumer);

		// Multiblock start
		AdvancementHolder hammer = AdvancementBuilder.child("craft_hammer", rtfm).getItem(Tools.HAMMER).save(consumer);
		AdvancementHolder cokeoven = AdvancementBuilder.child("mb_cokeoven", hammer).multiblock(IEMultiblocks.COKE_OVEN).save(consumer);
		AdvancementHolder blastfurnace = AdvancementBuilder.child("mb_blastfurnace", cokeoven).multiblock(IEMultiblocks.BLAST_FURNACE).save(consumer);
		AdvancementHolder steel = AdvancementBuilder.child("make_steel", blastfurnace).goal().getItem(Metals.INGOTS.get(EnumMetals.STEEL)).save(consumer);

		// Treated Wood
		AdvancementHolder creosote = AdvancementBuilder.child("creosote", cokeoven).getItem(IEFluids.CREOSOTE.getBucket()).save(consumer);
		AdvancementHolder treatedWood = AdvancementBuilder.child("craft_treatedwood", creosote).getItem(WoodenDecoration.TREATED_WOOD.get(TreatedWoodStyles.HORIZONTAL)).save(consumer);
		AdvancementHolder workbench = AdvancementBuilder.child("craft_workbench", treatedWood).getItem(WoodenDevices.WORKBENCH).save(consumer);

		// Villagers
		AdvancementHolder villagers = AdvancementBuilder.child("villager", rtfm).icon(Items.EMERALD).orRequirements()
				.talkToVillagers(Villages.ENGINEER, Villages.MACHINIST, Villages.ELECTRICIAN, Villages.OUTFITTER, Villages.GUNSMITH)
				.save(consumer);
		AdvancementHolder shaderbag = AdvancementBuilder.child("buy_shaderbag", villagers).icon(Misc.SHADER_BAG.get(Rarity.COMMON))
				.addCriterion("buy_shaderbag", tradedForItem(ItemPredicate.Builder.item().of(
						Misc.SHADER_BAG.get(Rarity.COMMON),
						Misc.SHADER_BAG.get(Rarity.UNCOMMON),
						Misc.SHADER_BAG.get(Rarity.RARE)
				))).save(consumer);

		// TODO
		//CompoundTag displayTag = new CompoundTag();
		//displayTag.putString("Name", Component.Serializer.toJson(
		//		Component.translatable("item.immersiveengineering.map_orevein"), lookup
		//));
		//CompoundTag mapNBT = new CompoundTag();
		//mapNBT.put("display", displayTag);
		//AdvancementHolder oremap = AdvancementBuilder.child("buy_oremap", villagers).icon(Items.FILLED_MAP)
		//		.addCriterion("buy_oremap", tradedForItem(
		//				ItemPredicate.Builder.item().of(Items.FILLED_MAP).hasNbt(mapNBT)
		//		)).save(consumer);

		AdvancementHolder illager = AdvancementBuilder.child("kill_illager", villagers).goal().icon(Raid.getLeaderBannerInstance(
						lookup.lookupOrThrow(Registries.BANNER_PATTERN)
				))
				.orRequirements()
				.addCriterion("revolver_kill", KilledTrigger.TriggerInstance.playerKilledEntity(
						EntityPredicate.Builder.entity().of(EntityTypeTags.RAIDERS),
						DamageSourcePredicate.Builder.damageType().direct(EntityPredicate.Builder.entity().of(IEEntityTypes.REVOLVERSHOT.get()))
				)).addCriterion("railgun_kill", KilledTrigger.TriggerInstance.playerKilledEntity(
						EntityPredicate.Builder.entity().of(EntityTypeTags.RAIDERS),
						DamageSourcePredicate.Builder.damageType().tag(TagPredicate.is(DamageTypeTags.IS_PROJECTILE)).source(EntityPredicate.Builder.entity().equipment(
								EntityEquipmentPredicate.Builder.equipment().mainhand(
										ItemPredicate.Builder.item().of(Weapons.RAILGUN.asItem())
								).build()
						))
				)).addCriterion("chemthrower_kill", KilledTrigger.TriggerInstance.playerKilledEntity(
						EntityPredicate.Builder.entity().of(EntityTypeTags.RAIDERS),
						DamageSourcePredicate.Builder.damageType().direct(EntityPredicate.Builder.entity().of(IEEntityTypes.CHEMTHROWER_SHOT.get()))
				))
				.loot("shader_rare").save(consumer);

		AdvancementHolder friedbird = AdvancementBuilder.child("secret_friedbird", wire).challenge().hidden()
				.icon(Misc.ICON_FRIED).codeTriggered().loot("shader_masterwork").save(consumer);
		AdvancementHolder luckofthedraw = AdvancementBuilder.child("secret_luckofthedraw", shaderbag).challenge().hidden()
				.icon(Misc.ICON_LUCKY).codeTriggered().loot("shader_masterwork").save(consumer);

		/* MULTIBLOCKS */
		AdvancementBuilder.setPage("multiblocks");
		AdvancementHolder multiblocks = AdvancementBuilder.root("block/wooden_decoration/treated_wood").quiet().hasItems(Metals.INGOTS.get(EnumMetals.STEEL))
				.icon(IEBlocks.MetalDecoration.STEEL_SCAFFOLDING.get(MetalScaffoldingType.STANDARD)).save(consumer);

		// Furnace upgrades
		AdvancementHolder improvedblastfurnace = AdvancementBuilder.child("mb_improvedblastfurnace", multiblocks).multiblock(IEMultiblocks.ADVANCED_BLAST_FURNACE).save(consumer);
		AdvancementHolder arcfurnace = AdvancementBuilder.child("mb_arcfurnace", improvedblastfurnace).challenge().multiblock(IEMultiblocks.ARC_FURNACE).save(consumer);

		// Sheetmetal
		AdvancementHolder metalpress = AdvancementBuilder.child("mb_metalpress", multiblocks).multiblock(IEMultiblocks.METAL_PRESS).save(consumer);
		AdvancementHolder sheetmetal = AdvancementBuilder.child("craft_sheetmetal", metalpress).getItem(IEBlocks.Metals.SHEETMETAL.get(EnumMetals.IRON)).save(consumer);
		AdvancementHolder silo = AdvancementBuilder.child("mb_silo", sheetmetal).multiblock(IEMultiblocks.SILO).save(consumer);
		AdvancementHolder tank = AdvancementBuilder.child("mb_tank", sheetmetal).multiblock(IEMultiblocks.SHEETMETAL_TANK).save(consumer);

		// Fluid machines
		AdvancementHolder squeezer = AdvancementBuilder.child("mb_squeezer", multiblocks).multiblock(IEMultiblocks.SQUEEZER).save(consumer);
		AdvancementHolder fermenter = AdvancementBuilder.child("mb_fermenter", squeezer).multiblock(IEMultiblocks.FERMENTER).save(consumer);
		AdvancementHolder mixer = AdvancementBuilder.child("mb_mixer", fermenter).multiblock(IEMultiblocks.MIXER).save(consumer);
		AdvancementHolder concrete = AdvancementBuilder.child("liquid_concrete", mixer).icon(IEFluids.CONCRETE.getBucket())
				.addCriterion("concrete_feet", EffectsChangedTrigger.TriggerInstance.hasEffects(
						MobEffectsPredicate.Builder.effects().and(IEPotions.CONCRETE_FEET))
				).save(consumer);
		AdvancementHolder refinery = AdvancementBuilder.child("mb_refinery", fermenter).multiblock(IEMultiblocks.REFINERY).save(consumer);
		AdvancementHolder plastic = AdvancementBuilder.child("craft_duroplast", refinery).goal().getItem(Ingredients.DUROPLAST_PLATE).save(consumer);
		AdvancementHolder dieselgen = AdvancementBuilder.child("mb_dieselgen", refinery).challenge().multiblock(IEMultiblocks.DIESEL_GENERATOR).save(consumer);

		// Ores
		AdvancementHolder crusher = AdvancementBuilder.child("mb_crusher", multiblocks).goal().multiblock(IEMultiblocks.CRUSHER).save(consumer);
		AdvancementHolder excavator = AdvancementBuilder.child("mb_excavator", crusher).challenge().multiblock(IEMultiblocks.EXCAVATOR).save(consumer);

		/* TOOLS */
		AdvancementBuilder.setPage("tools");
		AdvancementHolder tools = AdvancementBuilder.root("block/wooden_decoration/treated_wood").quiet()
				.getItem(WoodenDevices.WORKBENCH).save(consumer);

		AdvancementHolder revolver = AdvancementBuilder.child("craft_revolver", tools).getItem(Weapons.REVOLVER).save(consumer);

		// TODO
		//ItemStack upgradedRevolver = new ItemStack(Weapons.REVOLVER);
		//CompoundTag upgrades = new CompoundTag();
		//upgrades.putInt("bullets", 6);
		//upgrades.putBoolean("electro", true);
		//ItemNBTHelper.setTagCompound(upgradedRevolver, "upgrades", upgrades);
		//AdvancementHolder upgradeRevolver = AdvancementBuilder.child("upgrade_revolver", revolver).challenge()
		//		.icon(upgradedRevolver).codeTriggered().loot("shader_rare").save(consumer);

		AdvancementHolder wolfpack = AdvancementBuilder.child("craft_wolfpack", revolver).hidden()
				.getItem(BulletHandler.getBulletItem(IEBullets.WOLFPACK)).save(consumer);

		AdvancementHolder drill = AdvancementBuilder.child("craft_drill", tools).getItem(Tools.DRILL).save(consumer);

		//TODO
		//ItemStack upgradedDrill = new ItemStack(Tools.DRILL);
		//upgrades = new CompoundTag();
		//upgrades.putInt("damage", 3);
		//upgrades.putBoolean("waterproof", true);
		//upgrades.putBoolean("oiled", true);
		//upgrades.putFloat("speed", 6.0f);
		//ItemNBTHelper.setTagCompound(upgradedDrill, "upgrades", upgrades);
		//ItemNBTHelper.setItemStack(upgradedDrill, "head", new ItemStack(Tools.DRILLHEAD_STEEL));
		//AdvancementHolder upgradeDrill = AdvancementBuilder.child("upgrade_drill", drill).challenge()
		//		.icon(upgradedDrill).codeTriggered().loot("shader_rare").save(consumer);

		AdvancementHolder buzzsaw = AdvancementBuilder.child("craft_buzzsaw", tools).getItem(Tools.BUZZSAW).save(consumer);

		// TODO
		//ItemStack upgradedBuzzsaw = new ItemStack(Tools.BUZZSAW);
		//upgrades = new CompoundTag();
		//upgrades.putBoolean("oiled", true);
		//upgrades.putBoolean("spareblades", true);
		//ItemNBTHelper.setTagCompound(upgradedBuzzsaw, "upgrades", upgrades);
		//ItemNBTHelper.setItemStack(upgradedBuzzsaw, "sawblade", new ItemStack(Tools.SAWBLADE));
		//ItemNBTHelper.setItemStack(upgradedBuzzsaw, "sawblade_spare1", new ItemStack(Tools.SAWBLADE));
		//ItemNBTHelper.setItemStack(upgradedBuzzsaw, "sawblade_spare2", new ItemStack(Tools.SAWBLADE));
		//AdvancementHolder upgradeBuzzsaw = AdvancementBuilder.child("upgrade_buzzsaw", buzzsaw).challenge()
		//		.icon(upgradedBuzzsaw).codeTriggered().loot("shader_rare").save(consumer);

		AdvancementHolder skyhook = AdvancementBuilder.child("skyhook_distance", tools).icon(Misc.SKYHOOK).codeTriggered().save(consumer);

		AdvancementHolder chemthrower = AdvancementBuilder.child("craft_chemthrower", tools).getItem(Weapons.CHEMTHROWER).save(consumer);

		AdvancementHolder railgun = AdvancementBuilder.child("craft_railgun", tools).getItem(Weapons.RAILGUN).save(consumer);

		// TODO
		//ItemStack upgradedRailgun = new ItemStack(Weapons.RAILGUN);
		//upgrades = new CompoundTag();
		//upgrades.putBoolean("scope", true);
		//upgrades.putFloat("speed", 1f);
		//ItemNBTHelper.setTagCompound(upgradedRailgun, "upgrades", upgrades);
		//AdvancementHolder upgradeRailgun = AdvancementBuilder.child("upgrade_railgun", railgun).challenge()
		//		.icon(upgradedRailgun).codeTriggered().loot("shader_rare").save(consumer);

		AdvancementHolder powerpack = AdvancementBuilder.child("craft_powerpack", tools).getItem(Misc.POWERPACK).save(consumer);

		// TODO
		//ItemStack upgradedPowerpack = new ItemStack(Misc.POWERPACK);
		//upgrades = new CompoundTag();
		//upgrades.putBoolean("antenna", true);
		//upgrades.putBoolean("induction", true);
		//ItemNBTHelper.setTagCompound(upgradedPowerpack, "upgrades", upgrades);
		//AdvancementHolder upgradePowerpack = AdvancementBuilder.child("upgrade_powerpack", powerpack).challenge()
		//		.icon(upgradedPowerpack).codeTriggered().loot("shader_rare").save(consumer);

		// TODO
		//AdvancementHolder birthdayparty = AdvancementBuilder.child("secret_birthdayparty", upgradeRevolver).challenge().hidden()
		//		.icon(Misc.ICON_BIRTHDAY).codeTriggered().loot("shader_masterwork").save(consumer);
		//AdvancementHolder drillbreak = AdvancementBuilder.child("secret_drillbreak", upgradeDrill).challenge().hidden()
		//		.icon(Misc.ICON_DRILLBREAK).codeTriggered().loot("shader_masterwork").save(consumer);
		//AdvancementHolder ravenholm = AdvancementBuilder.child("secret_ravenholm", upgradeRailgun).challenge().hidden()
		//		.icon(Misc.ICON_RAVENHOLM).codeTriggered().loot("shader_masterwork").save(consumer);
		//AdvancementHolder bttf = AdvancementBuilder.child("secret_bttf", upgradePowerpack).challenge().hidden()
		//		.icon(Misc.ICON_BTTF).codeTriggered().loot("shader_masterwork").save(consumer);
	}

	private static Path createPath(Path pathIn, AdvancementHolder advancementIn)
	{
		return pathIn.resolve("data/"+advancementIn.id().getNamespace()+"/advancements/"+advancementIn.id().getPath()+".json");
	}

	private static Criterion<TradeTrigger.TriggerInstance> tradedForItem(ItemPredicate.Builder builder)
	{
		return CriteriaTriggers.TRADE.createCriterion(
				new TradeTrigger.TriggerInstance(Optional.empty(), Optional.empty(), Optional.of(builder.build()))
		);
	}
}