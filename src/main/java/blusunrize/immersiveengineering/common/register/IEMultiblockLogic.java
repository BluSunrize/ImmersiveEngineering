package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistrationBuilder;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockItem;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.*;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.arcfurnace.ArcFurnaceLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.bottling_machine.BottlingMachineLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.mixer.MixerLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.sawmill.SawmillLogic;
import blusunrize.immersiveengineering.common.blocks.stone.StoneMultiBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class IEMultiblockLogic
{
	public static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(
			ForgeRegistries.BLOCKS, Lib.MODID
	);
	private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(
			ForgeRegistries.ITEMS, Lib.MODID
	);
	private static final DeferredRegister<BlockEntityType<?>> BE_REGISTER = DeferredRegister.create(
			ForgeRegistries.BLOCK_ENTITY_TYPES, Lib.MODID
	);

	public static final MultiblockRegistration<CokeOvenLogic.State> COKE_OVEN = stone(new CokeOvenLogic(), "coke_oven", true)
			.structure(() -> IEMultiblocks.COKE_OVEN)
			.build();

	public static final MultiblockRegistration<BlastFurnaceLogic.State> BLAST_FURNACE = stone(new BlastFurnaceLogic(), "blast_furnace", true)
			.structure(() -> IEMultiblocks.BLAST_FURNACE)
			.build();

	public static final MultiblockRegistration<AlloySmelterLogic.State> ALLOY_SMELTER = stone(new AlloySmelterLogic(), "alloy_smelter", true)
			.structure(() -> IEMultiblocks.ALLOY_SMELTER)
			.build();

	public static final MultiblockRegistration<LightningRodLogic.State> LIGHTNING_ROD = metal(new LightningRodLogic(), "lightning_rod")
			.notMirrored()
			.structure(() -> IEMultiblocks.LIGHTNING_ROD)
			.build();

	public static final MultiblockRegistration<CrusherLogic.State> CRUSHER = metal(new CrusherLogic(), "crusher")
			.structure(() -> IEMultiblocks.CRUSHER)
			.withComparator()
			.redstoneAware()
			.build();

	public static final MultiblockRegistration<FermenterLogic.State> FERMENTER = metal(new FermenterLogic(), "fermenter")
			.structure(() -> IEMultiblocks.FERMENTER)
			.withComparator()
			.redstoneAware()
			.build();

	public static final MultiblockRegistration<DieselGeneratorLogic.State> DIESEL_GENERATOR = metal(new DieselGeneratorLogic(), "diesel_generator")
			.structure(() -> IEMultiblocks.DIESEL_GENERATOR)
			.redstoneAware()
			.postProcessesShape()
			.build();

	public static final MultiblockRegistration<MetalPressLogic.State> METAL_PRESS = metal(new MetalPressLogic(), "metal_press")
			.structure(() -> IEMultiblocks.METAL_PRESS)
			.redstoneAware()
			.build();

	public static final MultiblockRegistration<AssemblerLogic.State> ASSEMBLER = metal(new AssemblerLogic(), "assembler")
			.structure(() -> IEMultiblocks.ASSEMBLER)
			.redstoneAware()
			.notMirrored()
			.build();

	public static final MultiblockRegistration<AutoWorkbenchLogic.State> AUTO_WORKBENCH = metal(new AutoWorkbenchLogic(), "auto_workbench")
			.structure(() -> IEMultiblocks.AUTO_WORKBENCH)
			.redstoneAware()
			.build();

	public static final MultiblockRegistration<BottlingMachineLogic.State> BOTTLING_MACHINE = metal(new BottlingMachineLogic(), "bottling_machine")
			.structure(() -> IEMultiblocks.BOTTLING_MACHINE)
			.redstoneAware()
			.build();

	public static final MultiblockRegistration<SiloLogic.State> SILO = metal(new SiloLogic(), "silo")
			.structure(() -> IEMultiblocks.SILO)
			.redstoneAware()
			.withComparator()
			.notMirrored()
			.build();

	public static final MultiblockRegistration<SheetmetalTankLogic.State> TANK = metal(new SheetmetalTankLogic(), "tank")
			.structure(() -> IEMultiblocks.SHEETMETAL_TANK)
			.redstoneAware()
			.withComparator()
			.notMirrored()
			.build();

	public static final MultiblockRegistration<MixerLogic.State> MIXER = metal(new MixerLogic(), "mixer")
			.structure(() -> IEMultiblocks.MIXER)
			.redstoneAware()
			.build();

	public static final MultiblockRegistration<RefineryLogic.State> REFINERY = metal(new RefineryLogic(), "refinery")
			.structure(() -> IEMultiblocks.REFINERY)
			.redstoneAware()
			.build();

	public static final MultiblockRegistration<SqueezerLogic.State> SQUEEZER = metal(new SqueezerLogic(), "squeezer")
			.structure(() -> IEMultiblocks.SQUEEZER)
			.redstoneAware()
			.build();

	public static final MultiblockRegistration<BucketWheelLogic.State> BUCKET_WHEEL = metal(new BucketWheelLogic(), "bucket_wheel")
			.structure(() -> IEMultiblocks.BUCKET_WHEEL)
			.notMirrored()
			.build();

	public static final MultiblockRegistration<ExcavatorLogic.State> EXCAVATOR = metal(new ExcavatorLogic(), "excavator")
			.structure(() -> IEMultiblocks.EXCAVATOR)
			.redstoneAware()
			.withComparator()
			.build();

	public static final MultiblockRegistration<SawmillLogic.State> SAWMILL = metal(new SawmillLogic(), "sawmill")
			.structure(() -> IEMultiblocks.SAWMILL)
			.redstoneAware()
			.withComparator()
			.build();

	public static final MultiblockRegistration<ArcFurnaceLogic.State> ARC_FURNACE = metal(new ArcFurnaceLogic(), "arc_furnace")
			.structure(() -> IEMultiblocks.ARC_FURNACE)
			.redstoneAware()
			.withComparator()
			.build();

	private static <S extends IMultiblockState>
	MultiblockRegistrationBuilder<S> stone(IMultiblockLogic<S> logic, String name, boolean solid)
	{
		return MultiblockRegistration.builder(logic, name)
				.notMirrored()
				.customBlock(
						BLOCK_REGISTER, ITEM_REGISTER,
						r -> new NonMirrorableWithActiveBlock<>(StoneMultiBlock.properties(solid).get(), r),
						MultiblockItem::new
				)
				.defaultBEs(BE_REGISTER);
	}

	private static <S extends IMultiblockState>
	MultiblockRegistrationBuilder<S> metal(IMultiblockLogic<S> logic, String name)
	{
		return MultiblockRegistration.builder(logic, name)
				.defaultBEs(BE_REGISTER)
				.defaultBlock(BLOCK_REGISTER, ITEM_REGISTER, IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get());
	}

	public static void init(IEventBus bus)
	{
		BLOCK_REGISTER.register(bus);
		ITEM_REGISTER.register(bus);
		BE_REGISTER.register(bus);
	}
}
