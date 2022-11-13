package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockItem;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration.builder;

public class IEMultiblockLogic
{
	private static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(
			ForgeRegistries.BLOCKS, Lib.MODID
	);
	private static final DeferredRegister<Item> ITEM_REGISTER = DeferredRegister.create(
			ForgeRegistries.ITEMS, Lib.MODID
	);
	private static final DeferredRegister<BlockEntityType<?>> BE_REGISTER = DeferredRegister.create(
			ForgeRegistries.BLOCK_ENTITY_TYPES, Lib.MODID
	);

	public static final MultiblockRegistration<LightningRodLogic.State> LIGHTNING_ROD = builder(new LightningRodLogic(), "lightning_rod")
			.notMirrored()
			.defaultBEs(BE_REGISTER)
			.defaultBlock(BLOCK_REGISTER, ITEM_REGISTER, IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get())
			.structure(() -> IEMultiblocks.LIGHTNING_ROD)
			.build();

	public static final MultiblockRegistration<CokeOvenLogic.State> COKE_OVEN = builder(new CokeOvenLogic(), "coke_oven")
			.notMirrored()
			.defaultBEs(BE_REGISTER)
			.customBlock(
					BLOCK_REGISTER, ITEM_REGISTER,
					r -> new CokeOvenBlock(IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get(), r), MultiblockItem::new
			)
			.structure(() -> IEMultiblocks.COKE_OVEN)
			.build();

	public static final MultiblockRegistration<CrusherLogic.State> CRUSHER = builder(new CrusherLogic(), "crusher")
			.defaultBEs(BE_REGISTER)
			.defaultBlock(BLOCK_REGISTER, ITEM_REGISTER, IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get())
			.structure(() -> IEMultiblocks.CRUSHER)
			.withComparator()
			.redstoneAware()
			.build();

	public static final MultiblockRegistration<FermenterLogic.State> FERMENTER = builder(new FermenterLogic(), "fermenter")
			.defaultBEs(BE_REGISTER)
			.defaultBlock(BLOCK_REGISTER, ITEM_REGISTER, IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get())
			.structure(() -> IEMultiblocks.FERMENTER)
			.withComparator()
			.redstoneAware()
			.build();

	public static final MultiblockRegistration<DieselGeneratorLogic.State> DIESEL_GENERATOR = builder(new DieselGeneratorLogic(), "diesel_generator")
			.defaultBEs(BE_REGISTER)
			.defaultBlock(BLOCK_REGISTER, ITEM_REGISTER, IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get())
			.structure(() -> IEMultiblocks.DIESEL_GENERATOR)
			.redstoneAware()
			.build();

	public static final MultiblockRegistration<MetalPressLogic.State> METAL_PRESS = builder(new MetalPressLogic(), "metal_press")
			.defaultBEs(BE_REGISTER)
			.defaultBlock(BLOCK_REGISTER, ITEM_REGISTER, IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get())
			.structure(() -> IEMultiblocks.METAL_PRESS)
			.redstoneAware()
			.build();

	public static final MultiblockRegistration<AssemblerLogic.State> ASSEMBLER = builder(new AssemblerLogic(), "assembler")
			.defaultBEs(BE_REGISTER)
			.defaultBlock(BLOCK_REGISTER, ITEM_REGISTER, IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get())
			.structure(() -> IEMultiblocks.ASSEMBLER)
			.redstoneAware()
			.notMirrored()
			.build();

	public static void init(IEventBus bus)
	{
		BLOCK_REGISTER.register(bus);
		ITEM_REGISTER.register(bus);
		BE_REGISTER.register(bus);
	}
}
