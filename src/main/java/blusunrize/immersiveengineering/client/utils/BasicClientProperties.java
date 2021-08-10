package blusunrize.immersiveengineering.client.utils;

import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks.MultiblockRenderProperties;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IEMultiblocks;
import blusunrize.immersiveengineering.common.blocks.multiblocks.IETemplateMultiblock;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class BasicClientProperties implements MultiblockRenderProperties
{
	private static final Map<ResourceLocation, DynamicModel> MODELS = new HashMap<>();

	private final IETemplateMultiblock multiblock;
	@Nullable
	private NonNullList<ItemStack> materials;
	private final Supplier<DynamicModel> model;

	public BasicClientProperties(IETemplateMultiblock multiblock)
	{
		this.multiblock = multiblock;
		this.model = Suppliers.memoize(() -> MODELS.get(multiblock.getUniqueName()));
	}

	public static void initModels()
	{
		for (IMultiblock mb : IEMultiblocks.IE_MULTIBLOCKS)
			if (mb instanceof IETemplateMultiblock ieMB)
				MODELS.put(mb.getUniqueName(), new DynamicModel(ieMB.getBlockName().getPath()));
	}

	@Override
	public NonNullList<ItemStack> getTotalMaterials()
	{
		if(materials==null)
		{
			List<StructureBlockInfo> structure = multiblock.getStructure(null);
			materials = NonNullList.create();
			for(StructureBlockInfo info : structure)
			{
				ItemStack picked = Utils.getPickBlock(info.state);
				boolean added = false;
				for(ItemStack existing : materials)
					if(ItemStack.isSame(existing, picked))
					{
						existing.grow(1);
						added = true;
						break;
					}
				if(!added)
					materials.add(picked.copy());
			}
		}
		return materials;
	}

	@Override
	public void renderFormedStructure(PoseStack transform, MultiBufferSource bufferSource)
	{
		transform.pushPose();
		BlockPos offset = multiblock.getMasterFromOriginOffset();
		transform.translate(offset.getX(), offset.getY(), offset.getZ());
		List<BakedQuad> nullQuads = model.get().getNullQuads();
		VertexConsumer buffer = bufferSource.getBuffer(IERenderTypes.TRANSLUCENT_FULLBRIGHT);
		nullQuads.forEach(quad -> buffer.putBulkData(
				transform.last(), quad, 1, 1, 1, 1, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY
		));
		transform.popPose();
	}

	@Override
	public boolean canRenderFormedStructure()
	{
		return true;
	}
}
