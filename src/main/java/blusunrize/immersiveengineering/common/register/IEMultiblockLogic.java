package blusunrize.immersiveengineering.common.register;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic;
import blusunrize.immersiveengineering.api.multiblocks.blocks.IMultiblockLogic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.LightningRod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class IEMultiblockLogic
{
	private static final DeferredRegister<Block> BLOCK_REGISTER = DeferredRegister.create(
			ForgeRegistries.BLOCKS, Lib.MODID
	);
	private static final DeferredRegister<BlockEntityType<?>> BE_REGISTER = DeferredRegister.create(
			ForgeRegistries.BLOCK_ENTITY_TYPES, Lib.MODID
	);

	public static final MultiblockRegistration<LightningRod.State> LIGHTNING_ROD = registerMetal(
			new LightningRod(), "lightning_rod", false, LightningRod.MASTER_OFFSET
	);

	public static void init(IEventBus bus)
	{
		BLOCK_REGISTER.register(bus);
		BE_REGISTER.register(bus);
	}

	private static <State extends IMultiblockState>
	MultiblockRegistration<State> registerMetal(
			IMultiblockLogic<State> logic, String name, boolean mirrorable, BlockPos masterPosition
	)
	{
		return MultiblockRegistration.register(
				logic,
				// TODO
				name+"_new",
				BLOCK_REGISTER,
				BE_REGISTER,
				mirrorable,
				masterPosition,
				IEBlocks.METAL_PROPERTIES_NO_OCCLUSION.get()
		);
	}
}
