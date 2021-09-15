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
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.ITextureOverride;
import blusunrize.immersiveengineering.common.register.IEBlocks.Cloth;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.CreativeModeTab;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ShaderItem extends IEBaseItem implements IShaderItem, ITextureOverride
{
	public ShaderItem()
	{
		super(new Properties().stacksTo(1));
	}

	@Override
	public ShaderCase getShaderCase(ItemStack shader, ItemStack item, ResourceLocation shaderType)
	{
		return ShaderRegistry.getShader(getShaderName(shader), shaderType);
	}


	@Override
	public ResourceLocation getShaderName(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "shader_name"))
			return new ResourceLocation(ItemNBTHelper.getString(stack, "shader_name"));
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
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		//TODO proper translation
		list.add(new TranslatableComponent(Lib.DESC_INFO+"shader.level")
				.append(this.getRarity(stack).color.toString())
				.append(new TranslatableComponent(Lib.DESC_INFO+"shader.rarity."+this.getRarity(stack).name().toLowerCase(Locale.US)))
		);
		if(!Screen.hasShiftDown())
			list.add(new TranslatableComponent(Lib.DESC_INFO+"shader.applyTo")
					.append(" ")
					.append(new TranslatableComponent(Lib.DESC_INFO+"holdShift")));
		else
		{
			list.add(new TranslatableComponent(Lib.DESC_INFO+"shader.applyTo"));
			ResourceLocation rl = getShaderName(stack);
			if(rl!=null)
			{
				List<ShaderCase> array = ShaderRegistry.shaderRegistry.get(rl).getCases();
				for(ShaderCase sCase : array)
					if(!(sCase instanceof ShaderCaseItem))
						list.add(TextUtils.applyFormat(
								new TranslatableComponent(Lib.DESC_INFO+"shader."+sCase.getShaderType()),
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
					.append(new TranslatableComponent("item."+rl.getNamespace()+".shader.name."+rl.getPath()));
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
	@OnlyIn(Dist.CLIENT)
	public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list)
	{
		if(this.allowdedIn(tab))
			for(ResourceLocation key : ShaderRegistry.shaderRegistry.keySet())
			{
				ItemStack s = new ItemStack(this);
				ItemNBTHelper.putString(s, "shader_name", key.toString());
				list.add(s);
			}
	}

	@Override
	public boolean hasCustomItemColours()
	{
		return true;
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
		return super.getColourForIEItem(stack, pass);
	}

	@Override
	public String getModelCacheKey(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "shader_name"))
			return ItemNBTHelper.getString(stack, "shader_name");
		return null;
	}

	@Override
	public List<ResourceLocation> getTextures(ItemStack stack, String key)
	{
		ResourceLocation name = getShaderName(stack);
		if(ShaderRegistry.shaderRegistry.containsKey(name))
		{
			ShaderCase sCase = ShaderRegistry.shaderRegistry.get(name).getCase(new ResourceLocation(ImmersiveEngineering.MODID, "item"));
			if(sCase!=null)
			{
				ShaderLayer[] layers = sCase.getLayers();
				ArrayList<ResourceLocation> list = new ArrayList<>(layers.length);
				for(ShaderLayer layer : layers)
					list.add(layer.getTexture());
				return list;
			}
		}
//		return Arrays.asList(new ResourceLocation("immersiveengineering:item/shader_0"));
		return Arrays.asList(new ResourceLocation("immersiveengineering:item/shader_0"), new ResourceLocation("immersiveengineering:item/shader_1"), new ResourceLocation("immersiveengineering:item/shader_2"));
	}
}