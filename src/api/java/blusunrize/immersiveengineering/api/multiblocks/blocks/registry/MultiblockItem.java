package blusunrize.immersiveengineering.api.multiblocks.blocks.registry;

import net.minecraft.core.NonNullList;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nonnull;

public class MultiblockItem extends BlockItem
{
	public MultiblockItem(Block block)
	{
		super(block, new Properties());
	}

	@Override
	public void fillItemCategory(@Nonnull CreativeModeTab category, @Nonnull NonNullList<ItemStack> items)
	{
		// NOP, multiblock items should not show up in creative menu
	}

	@Nonnull
	@Override
	public InteractionResult place(@Nonnull BlockPlaceContext context)
	{
		// Do not allow multiblocks to be placed directly
		return InteractionResult.FAIL;
	}
}
