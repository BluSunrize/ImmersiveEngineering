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
import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.block.ClocheCallbacks.Key;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.common.blocks.metal.ClocheBlockEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import org.joml.Vector4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static blusunrize.immersiveengineering.common.blocks.metal.ClocheBlockEntity.SLOT_SOIL;

public class ClocheCallbacks implements BlockCallback<Key>
{
	public static final ClocheCallbacks INSTANCE = new ClocheCallbacks();
	private static final Key INVALID = new Key(new ComparableItemStack(ItemStack.EMPTY));

	@Override
	public Key extractKey(@Nonnull BlockAndTintGetter level, @Nonnull BlockPos pos, @Nonnull BlockState state, BlockEntity blockEntity)
	{
		if(!(blockEntity instanceof ClocheBlockEntity clocheHere))
			return getDefaultKey();
		ClocheBlockEntity mainCloche = clocheHere.master();
		if(mainCloche==null)
			return getDefaultKey();
		ItemStack soil = mainCloche.getInventory().get(SLOT_SOIL);
		return new Key(new ComparableItemStack(soil, true));
	}

	@Override
	public Key getDefaultKey()
	{
		return INVALID;
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
			rl = FluidUtil.getFluidContained(soil)
					.map(fs -> IClientFluidTypeExtensions.of(fs.getFluid()).getStillTexture(fs))
					.orElse(null);
		return rl;
	}

	@Override
	public Vector4f getRenderColor(Key key, String group, String material, ShaderCase shaderCase, Vector4f original)
	{
		ItemStack soil = key.soil().stack;
		if(!soil.isEmpty()&&"farmland".equals(material)&&Utils.isFluidRelatedItemStack(soil))
			return Utils.vec4fFromInt(
					FluidUtil.getFluidContained(soil)
							.map(fs -> IClientFluidTypeExtensions.of(fs.getFluid()).getTintColor(fs))
							.orElse(0xffffffff)
			);
		return original;
	}

	public record Key(ComparableItemStack soil)
	{
	}
}
