/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.component.ComparatorManager;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockItem;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.bottling_machine.BottlingMachineLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.mixer.MixerLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.sawmill.SawmillLogic;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class IEMultiblockLogic
{
	public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(
			BuiltInRegistries.BLOCK, Lib.MODID
	);
	private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(
			BuiltInRegistries.ITEM, Lib.MODID
	);
	private static final DeferredRegister<BlockEntityType<?>> BE_REGISTER = DeferredRegister.create(
			BuiltInRegistries.BLOCK_ENTITY_TYPE, Lib.MODID
	);

	public static final MultiblockRegistration<CokeOvenLogic.State> COKE_OVEN = stone(new CokeOvenLogic(), "coke_oven", true).structure(() -> IEMultiblocks.COKE_OVEN)
			.gui(IEMenuTypes.COKE_OVEN)
			.build();

	public static final MultiblockRegistration<BlastFurnaceLogic.State> BLAST_FURNACE = stone(new BlastFurnaceLogic(), "blast_furnace", true)
			.structure(() -> IEMultiblocks.BLAST_FURNACE)
			.gui(IEMenuTypes.BLAST_FURNACE)
			.build();

	public static final MultiblockRegistration<AdvBlastFurnaceLogic.State> ADV_BLAST_FURNACE = stone(new AdvBlastFurnaceLogic(), "advanced_blast_furnace", false)
			.structure(() -> IEMultiblocks.ADVANCED_BLAST_FURNACE)
			.gui(IEMenuTypes.BLAST_FURNACE_ADV)
			.build();

	public static final MultiblockRegistration<AlloySmelterLogic.State> ALLOY_SMELTER = stone(new AlloySmelterLogic(), "alloy_smelter", true)
			.structure(() -> IEMultiblocks.ALLOY_SMELTER)
			.gui(IEMenuTypes.ALLOY_SMELTER)
			.build();

	public static final MultiblockRegistration<LightningRodLogic.State> LIGHTNING_ROD = metal(new LightningRodLogic(), "lightning_rod")
			.notMirrored()
			.structure(() -> IEMultiblocks.LIGHTNING_ROD)
			.build();

	public static final MultiblockRegistration<CrusherLogic.State> CRUSHER = metal(new CrusherLogic(), "crusher")
			.structure(() -> IEMultiblocks.CRUSHER)
			.redstone(s -> s.rsState, CrusherLogic.REDSTONE_POS)
			.comparator(CrusherLogic.makeComparator())
			.build();

	public static final MultiblockRegistration<FermenterLogic.State> FERMENTER = metal(new FermenterLogic(), "fermenter")
			.structure(() -> IEMultiblocks.FERMENTER)
			.redstone(s -> s.rsState, FermenterLogic.REDSTONE_POS)
			.gui(IEMenuTypes.FERMENTER)
			.comparator(FermenterLogic.makeComparator())
			.build();

	public static final MultiblockRegistration<DieselGeneratorLogic.State> DIESEL_GENERATOR = metal(new DieselGeneratorLogic(), "diesel_generator")
			.structure(() -> IEMultiblocks.DIESEL_GENERATOR)
			.redstone(s -> s.rsState, DieselGeneratorLogic.REDSTONE_POS)
			.build();

	public static final MultiblockRegistration<MetalPressLogic.State> METAL_PRESS = metal(new MetalPressLogic(), "metal_press")
			.structure(() -> IEMultiblocks.METAL_PRESS)
			.redstone(s -> s.rsState, MetalPressLogic.REDSTONE_POS)
			.build();

	public static final MultiblockRegistration<AssemblerLogic.State> ASSEMBLER = metal(new AssemblerLogic(), "assembler")
			.structure(() -> IEMultiblocks.ASSEMBLER)
			.notMirrored()
			.redstoneNoComputer(s -> s.rsState, AssemblerLogic.REDSTONE_PORTS)
			.gui(IEMenuTypes.ASSEMBLER)
			.comparator(AssemblerLogic.makeComparator())
			.build();

	public static final MultiblockRegistration<AutoWorkbenchLogic.State> AUTO_WORKBENCH = metal(new AutoWorkbenchLogic(), "auto_workbench")
			.structure(() -> IEMultiblocks.AUTO_WORKBENCH)
			.redstone(s -> s.rsState, AutoWorkbenchLogic.REDSTONE_POS)
			.gui(IEMenuTypes.AUTO_WORKBENCH)
			.comparator(AutoWorkbenchLogic.makeComparator())
			.build();

	public static final MultiblockRegistration<BottlingMachineLogic.State> BOTTLING_MACHINE = metal(new BottlingMachineLogic(), "bottling_machine")
			.structure(() -> IEMultiblocks.BOTTLING_MACHINE)
			.redstone(s -> s.rsState, BottlingMachineLogic.REDSTONE_POS)
			.build();

	public static final MultiblockRegistration<SiloLogic.State> SILO = metal(new SiloLogic(), "silo")
			.structure(() -> IEMultiblocks.SILO)
			.redstone(s -> s.rsState, SiloLogic.OUTPUT_POS)
			.withComparator()
			.notMirrored()
			.build();

	public static final MultiblockRegistration<SheetmetalTankLogic.State> TANK = metal(new SheetmetalTankLogic(), "tank")
			.structure(() -> IEMultiblocks.SHEETMETAL_TANK)
			.redstone(s -> s.rsState, SheetmetalTankLogic.IO_POS)
			.withComparator()
			.notMirrored()
			.build();

	public static final MultiblockRegistration<MixerLogic.State> MIXER = metal(new MixerLogic(), "mixer")
			.structure(() -> IEMultiblocks.MIXER)
			.redstone(s -> s.rsState, MixerLogic.REDSTONE_POS)
			.gui(IEMenuTypes.MIXER)
			.comparator(MixerLogic.makeComparator())
			.build();

	public static final MultiblockRegistration<RefineryLogic.State> REFINERY = metal(new RefineryLogic(), "refinery")
			.structure(() -> IEMultiblocks.REFINERY)
			.redstone(s -> s.rsState, RefineryLogic.REDSTONE_POS)
			.build();

	public static final MultiblockRegistration<SqueezerLogic.State> SQUEEZER = metal(new SqueezerLogic(), "squeezer")
			.structure(() -> IEMultiblocks.SQUEEZER)
			.redstone(s -> s.rsState, SqueezerLogic.REDSTONE_POS)
			.gui(IEMenuTypes.SQUEEZER)
			.comparator(SqueezerLogic.makeComparator())
			.build();

	public static final MultiblockRegistration<BucketWheelLogic.State> BUCKET_WHEEL = metal(new BucketWheelLogic(), "bucket_wheel")
			.structure(() -> IEMultiblocks.BUCKET_WHEEL)
			.notMirrored()
			.build();

	public static final MultiblockRegistration<ExcavatorLogic.State> EXCAVATOR = metal(new ExcavatorLogic(), "excavator")
			.structure(() -> IEMultiblocks.EXCAVATOR)
			.redstone(s -> s.rsState, ExcavatorLogic.REDSTONE_POS)
			.comparator(new ComparatorManager<>(ExcavatorLogic::computeComparatorValue))
			.build();

	public static final MultiblockRegistration<SawmillLogic.State> SAWMILL = metal(new SawmillLogic(), "sawmill")
			.structure(() -> IEMultiblocks.SAWMILL)
			.comparator(SawmillLogic.makeComparator())
			.redstone(s -> s.rsState, SawmillLogic.REDSTONE_POS)
			.build();

	public static final MultiblockRegistration<ArcFurnaceLogic.State> ARC_FURNACE = metal(new ArcFurnaceLogic(), "arc_furnace")
			.structure(() -> IEMultiblocks.ARC_FURNACE)
			.comparator(ArcFurnaceLogic.makeElectrodeComparator())
			.comparator(ArcFurnaceLogic.makeInventoryComparator())
			.redstone(s -> s.rsControl, ArcFurnaceLogic.REDSTONE_POS)
			.gui(IEMenuTypes.ARC_FURNACE)
			.build();

	public static final MultiblockRegistration<RadioTowerLogic.State> RADIO_TOWER = metal(new RadioTowerLogic(), "radio_tower")
			.structure(() -> IEMultiblocks.RADIO_TOWER)
			.gui(IEMenuTypes.RADIO_TOWER)
			.build();

	public static final MultiblockRegistration<ChunkLoaderLogic.State> CHUNK_LOADER = metal(new ChunkLoaderLogic(), "chunk_loader")
			.structure(() -> IEMultiblocks.CHUNK_LOADER)
			.redstone(s -> s.rsState, ChunkLoaderLogic.REDSTONE_POS)
			.gui(IEMenuTypes.CHUNK_LOADER)
			.build();

	private static <S extends IMultiblockState>
	IEMultiblockBuilder<S> stone(IMultiblockLogic<S> logic, String name, boolean solid)
	{
		Properties properties = Properties.of()
				.sound(SoundType.NETHER_BRICKS)
				.mapColor(MapColor.STONE)
				.instrument(NoteBlockInstrument.BASEDRUM)
				.forceSolidOn()
				.strength(2, 20);
		if(!solid)
			properties.noOcclusion();
		else
			properties.forceSolidOn();
		return new IEMultiblockBuilder<>(logic, name)
				.notMirrored()
				.customBlock(
						BLOCK_REGISTER, ITEM_REGISTER,
						r -> new NonMirrorableWithActiveBlock<>(properties, r),
						MultiblockItem::new
				)
				.defaultBEs(BE_REGISTER);
	}

	private static <S extends IMultiblockState>
	IEMultiblockBuilder<S> metal(IMultiblockLogic<S> logic, String name)
	{
		return new IEMultiblockBuilder<>(logic, name)
				.defaultBEs(BE_REGISTER)
				.defaultBlock(BLOCK_REGISTER, ITEM_REGISTER, IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get());
	}

	public static void init(IEventBus bus)
	{
		BLOCK_REGISTER.register(bus);
		ITEM_REGISTER.register(bus);
		BE_REGISTER.register(bus);
		IEMultiblockBuilder.handleModBusRegistrations(bus);
	}
}
