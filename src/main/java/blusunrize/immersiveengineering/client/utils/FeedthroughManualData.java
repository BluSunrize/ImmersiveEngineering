package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks.MultiblockManualData;
import blusunrize.immersiveengineering.common.blocks.multiblocks.FeedthroughMultiblock;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Supplier;

public class FeedthroughManualData implements MultiblockManualData
{
	private static final Component ARBITRARY_SOLID = new TranslatableComponent("block.immersiveengineering.arb_solid");
	private final Supplier<NonNullList<ItemStack>> materials = Suppliers.memoize(() -> NonNullList.of(
			ItemStack.EMPTY,
			new ItemStack(FeedthroughMultiblock.getDemoConnector(), 2),
			new ItemStack(Blocks.BOOKSHELF, 1).setHoverName(ARBITRARY_SOLID)
	));

	@Override
	public NonNullList<ItemStack> getTotalMaterials()
	{
		return materials.get();
	}

	@Override
	public boolean canRenderFormedStructure()
	{
		return false;
	}

	@Override
	public void renderFormedStructure(PoseStack transform, MultiBufferSource buffer)
	{
	}
}
