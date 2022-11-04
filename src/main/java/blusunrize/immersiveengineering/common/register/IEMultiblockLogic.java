package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.LightningRod;
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
	private static final DeferredRegister<BlockEntityType<?>> BE_REGISTER = DeferredRegister.create(
			ForgeRegistries.BLOCK_ENTITY_TYPES, Lib.MODID
	);

	public static final MultiblockRegistration<LightningRod.State> LIGHTNING_ROD = builder(new LightningRod(), "lightning_rod")
			.notMirrored()
			.defaultBEs(BE_REGISTER)
			.defaultBlock(BLOCK_REGISTER, IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get())
			.structure(() -> IEMultiblocks.LIGHTNING_ROD)
			.build();

	public static void init(IEventBus bus)
	{
		BLOCK_REGISTER.register(bus);
		BE_REGISTER.register(bus);
	}
}
