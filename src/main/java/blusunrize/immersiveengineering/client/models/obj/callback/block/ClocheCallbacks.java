/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.blocks.metal.ClocheBlockEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static blusunrize.immersiveengineering.common.blocks.metal.ClocheBlockEntity.SLOT_SOIL;

public class ClocheCallbacks implements BlockCallback<ClocheCallbacks.Key>
{
	public static final ClocheCallbacks INSTANCE = new ClocheCallbacks();
	private static final Key INVALID = new Key(null);

	@Override
	public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		if(!(blockEntity instanceof ClocheBlockEntity clocheHere))
			return INVALID;
		ClocheBlockEntity mainCloche = clocheHere.master();
		if(mainCloche==null)
			return INVALID;
		ItemStack soil = mainCloche.getInventory().get(SLOT_SOIL);
		return new Key(new ComparableItemStack(soil, true));
	}

	@Override
	public boolean dependsOnLayer()
	{
		return true;
	}

	@Override
	@Nullable
	public TextureAtlasSprite getTextureReplacement(Key key, String group, String material)
	{
		ItemStack soil = key.soil().stack;
		if(!soil.isEmpty()&&"farmland".equals(material))
		{
			ResourceLocation rl = getSoilTexture(soil);
			if(rl!=null)
				return ClientUtils.getSprite(rl);
		}
		return null;
	}

	@Override
	public boolean shouldRenderGroup(Key object, String group, RenderType layer)
	{
		return "glass".equals(group)==(layer==RenderType.translucent());
	}

	@Nullable
	private static ResourceLocation getSoilTexture(ItemStack soil)
	{
		ResourceLocation rl = ClocheRecipe.getSoilTexture(soil);
		if(rl==null)
		{
			try
			{
				BlockState state = Utils.getStateFromItemStack(soil);
				if(state!=null)
					rl = ModelUtils.getSideTexture(state, Direction.UP);
			} catch(Exception e)
			{
				rl = ModelUtils.getSideTexture(soil, Direction.UP);
			}
		}
		if(rl==null&&!soil.isEmpty()&&Utils.isFluidRelatedItemStack(soil))
			//TODO color
			rl = FluidUtil.getFluidContained(soil).map(fs -> fs.getFluid().getAttributes().getStillTexture(fs)).orElse(rl);
		return rl;
	}

	public record Key(ComparableItemStack soil)
	{
	}
}
