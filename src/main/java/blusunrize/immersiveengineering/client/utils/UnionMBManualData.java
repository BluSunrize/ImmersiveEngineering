package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks.MultiblockManualData;
import blusunrize.immersiveengineering.common.blocks.multiblocks.UnionMultiblock.TransformedMultiblock;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Supplier;

public class UnionMBManualData implements MultiblockManualData
{
	private final Supplier<NonNullList<ItemStack>> materials;

	public UnionMBManualData(List<TransformedMultiblock> parts)
	{
		this.materials = Suppliers.memoize(() -> {
			NonNullList<ItemStack> stacks = NonNullList.create();
			for(TransformedMultiblock part : parts)
				for(ItemStack stack : ClientMultiblocks.get(part.multiblock()).getTotalMaterials())
				{
					boolean added = false;
					for(ItemStack ex : stacks)
						if(ItemStack.isSame(ex, stack))
						{
							ex.grow(stack.getCount());
							added = true;
							break;
						}
					if(!added)
						stacks.add(stack.copy());
				}
			return stacks;
		});
	}

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
