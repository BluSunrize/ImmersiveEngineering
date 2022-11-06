package blusunrize.immersiveengineering.common.blocks.multiblocks.logic;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockPartBlock;
import blusunrize.immersiveengineering.api.multiblocks.blocks.MultiblockRegistration;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.CokeOvenLogic.State;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.Property;

import javax.annotation.Nonnull;

public class CokeOvenBlock extends MultiblockPartBlock<CokeOvenLogic.State>
{
	public static final Property<Boolean> ACTIVE = IEProperties.ACTIVE;

	public CokeOvenBlock(Properties properties, MultiblockRegistration<State> multiblock)
	{
		super(properties, multiblock);
	}

	@Override
	protected void createBlockStateDefinition(@Nonnull Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(ACTIVE);
	}
}
