/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.impl.ShaderCaseItem;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerBlockEntity;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerStandingBlock;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerWallBlock;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.register.IEBlocks.Cloth;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class ShaderItem extends IEBaseItem implements IShaderItem, IColouredItem
{
	public static final String SHADER_NAME_KEY = "shader_name";

	public ShaderItem()
	{
		super(new Properties().stacksTo(1));
	}

	@Override
	public ShaderCase getShaderCase(ItemStack shader, ResourceLocation shaderType)
	{
		return ShaderRegistry.getShader(getShaderName(shader), shaderType);
	}


	@Override
	public ResourceLocation getShaderName(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, SHADER_NAME_KEY))
			return new ResourceLocation(ItemNBTHelper.getString(stack, SHADER_NAME_KEY));
		return null;
	}

	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext ctx)
	{
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		ResourceLocation name = getShaderName(ctx.getItemInHand());
		if(ShaderRegistry.shaderRegistry.containsKey(name))
		{
			BlockState blockState = world.getBlockState(pos);
			BlockEntity tile = world.getBlockEntity(pos);
			if(tile instanceof BannerBlockEntity)
			{
				ShaderCase sCase = ShaderRegistry.shaderRegistry.get(name).getCase(new ResourceLocation(ImmersiveEngineering.MODID, "banner"));
				if(sCase!=null)
				{
					boolean wall = blockState.getBlock() instanceof WallBannerBlock;

					if(wall)
						world.setBlockAndUpdate(pos, Cloth.SHADER_BANNER_WALL.defaultBlockState()
								.setValue(ShaderBannerWallBlock.FACING, blockState.getValue(WallBannerBlock.FACING)));
					else
						world.setBlockAndUpdate(pos, Cloth.SHADER_BANNER.defaultBlockState()
								.setValue(ShaderBannerStandingBlock.ROTATION, blockState.getValue(BannerBlock.ROTATION)));
					tile = world.getBlockEntity(pos);
					if(tile instanceof ShaderBannerBlockEntity)
					{
						((ShaderBannerBlockEntity)tile).shader.setShaderItem(ItemHandlerHelper.copyStackWithSize(ctx.getItemInHand(), 1));
						tile.setChanged();
						return InteractionResult.SUCCESS;
					}
				}
			}
			else if(tile instanceof ShaderBannerBlockEntity)
			{
				((ShaderBannerBlockEntity)tile).shader.setShaderItem(ItemHandlerHelper.copyStackWithSize(ctx.getItemInHand(), 1));
				tile.setChanged();
				return InteractionResult.SUCCESS;
			}

		}
		return InteractionResult.FAIL;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		//TODO proper translation
		list.add(Component.translatable(Lib.DESC_INFO+"shader.level")
				.append(this.getRarity(stack).color.toString())
				.append(Component.translatable(Lib.DESC_INFO+"shader.rarity."+this.getRarity(stack).name().toLowerCase(Locale.US)))
		);
		if(!Screen.hasShiftDown())
			list.add(Component.translatable(Lib.DESC_INFO+"shader.applyTo")
					.append(" ")
					.append(Component.translatable(Lib.DESC_INFO+"holdShift")));
		else
		{
			list.add(Component.translatable(Lib.DESC_INFO+"shader.applyTo"));
			ResourceLocation rl = getShaderName(stack);
			if(rl!=null)
			{
				List<ShaderCase> array = ShaderRegistry.shaderRegistry.get(rl).getCases();
				for(ShaderCase sCase : array)
					if(!(sCase instanceof ShaderCaseItem))
						list.add(TextUtils.applyFormat(
								Component.translatable(Lib.DESC_INFO+"shader."+sCase.getShaderType()),
								ChatFormatting.DARK_GRAY
						));
			}
		}
	}

	@Nonnull
	@Override
	public Component getName(@Nonnull ItemStack stack)
	{
		MutableComponent itc = super.getName(stack).copy();
		ResourceLocation rl = getShaderName(stack);
		if(rl!=null)
			itc.append(": ")
					.append(Component.translatable("item."+rl.getNamespace()+".shader.name."+rl.getPath()));
		return itc;
	}

	@Nonnull
	@Override
	public Rarity getRarity(ItemStack stack)
	{
		ResourceLocation rl = getShaderName(stack);
		return ShaderRegistry.shaderRegistry.containsKey(rl)?ShaderRegistry.shaderRegistry.get(rl).getRarity(): Rarity.COMMON;
	}

	@Override
	public void fillCreativeTab(Output out)
	{
		for(ResourceLocation key : ShaderRegistry.shaderRegistry.keySet())
		{
			ItemStack s = new ItemStack(this);
			ItemNBTHelper.putString(s, SHADER_NAME_KEY, key.toString());
			out.accept(s);
		}
	}

	@Override
	public int getColourForIEItem(ItemStack stack, int pass)
	{
		ResourceLocation name = getShaderName(stack);
		if(ShaderRegistry.shaderRegistry.containsKey(name))
		{
			ShaderCase sCase = ShaderRegistry.shaderRegistry.get(name).getCase(new ResourceLocation(ImmersiveEngineering.MODID, "item"));
			if(sCase!=null)
			{
				ShaderLayer[] layers = sCase.getLayers();
				if(pass < layers.length&&layers[pass]!=null)
					return Utils.intFromRGBA(layers[pass].getColor());
				return 0xffffffff;
			}
		}
		return -1;
	}
}