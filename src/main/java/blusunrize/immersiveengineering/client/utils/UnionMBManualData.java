package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks.MultiblockManualData;
import blusunrize.immersiveengineering.common.blocks.multiblocks.UnionMultiblock.TransformedMultiblock;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Rotation;

import java.util.List;
import java.util.function.Supplier;

public class UnionMBManualData implements MultiblockManualData
{
	private final Supplier<NonNullList<ItemStack>> materials;
	private final List<ClientSubMultiblock> partData;

	public UnionMBManualData(List<TransformedMultiblock> parts, Vec3i min)
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
		this.partData = parts.stream()
				.map(mb -> toClientMultiblock(mb, min))
				.filter(p -> p.mbData().canRenderFormedStructure())
				.toList();
	}

	@Override
	public NonNullList<ItemStack> getTotalMaterials()
	{
		return materials.get();
	}

	@Override
	public boolean canRenderFormedStructure()
	{
		return true;
	}

	@Override
	public void renderFormedStructure(PoseStack transform, MultiBufferSource buffer)
	{
		for(ClientSubMultiblock part : partData)
		{
			transform.pushPose();
			Vec3i offset = part.offset();
			transform.translate(offset.getX(), offset.getY(), offset.getZ());
			transform.translate(0.5, 0.5, 0.5);
			transform.mulPose(part.rotation());
			transform.translate(-0.5, -0.5, -0.5);
			part.mbData().renderFormedStructure(transform, buffer);
			transform.popPose();
		}
	}

	private static float getAngle(Rotation rot)
	{
		return switch(rot)
				{
					case NONE -> 0;
					case CLOCKWISE_90 -> -90;
					case CLOCKWISE_180 -> 180;
					case COUNTERCLOCKWISE_90 -> 90;
				};
	}

	private static ClientSubMultiblock toClientMultiblock(TransformedMultiblock mb, Vec3i min)
	{
		MultiblockManualData data = ClientMultiblocks.get(mb.multiblock());
		Vec3i offset = mb.offset().subtract(min);
		Quaternion rotation = new Quaternion(0, getAngle(mb.rotation()), 0, true);
		return new ClientSubMultiblock(data, offset, rotation);
	}

	private record ClientSubMultiblock(MultiblockManualData mbData, Vec3i offset, Quaternion rotation)
	{
	}
}
