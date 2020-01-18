/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.shader.IShaderItem;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderCaseItem;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Cloth;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerTileEntity;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.ITextureOverride;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BannerBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallBannerBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
		super("shader", new Properties().maxStackSize(1));
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
	public ActionResultType onItemUse(ItemUseContext ctx)
	{
		World world = ctx.getWorld();
		BlockPos pos = ctx.getPos();
		ResourceLocation name = getShaderName(ctx.getItem());
		if(ShaderRegistry.shaderRegistry.containsKey(name))
		{
			BlockState blockState = world.getBlockState(pos);
			TileEntity tile = world.getTileEntity(pos);
			if(tile instanceof BannerTileEntity)
			{
				ShaderCase sCase = ShaderRegistry.shaderRegistry.get(name).getCase(new ResourceLocation(ImmersiveEngineering.MODID, "banner"));
				if(sCase!=null)
				{
					boolean wall = blockState.getBlock() instanceof WallBannerBlock;
					int orientation = wall?blockState.get(WallBannerBlock.HORIZONTAL_FACING).getIndex(): blockState.get(BannerBlock.ROTATION);
					world.setBlockState(pos, Cloth.shaderBanner.getDefaultState().with(IEProperties.FACING_HORIZONTAL, Direction.SOUTH));
					tile = world.getTileEntity(pos);
					if(tile instanceof ShaderBannerTileEntity)
					{
						//TODO use blockstate props similar to vanilla banners
						((ShaderBannerTileEntity)tile).wall = wall;
						((ShaderBannerTileEntity)tile).orientation = (byte)orientation;
						((ShaderBannerTileEntity)tile).shader.setShaderItem(Utils.copyStackWithAmount(ctx.getItem(), 1));
						tile.markDirty();
						return ActionResultType.SUCCESS;
					}
				}
			}
			else if(tile instanceof ShaderBannerTileEntity)
			{
				ItemStack current = ((ShaderBannerTileEntity)tile).shader.getShaderItem();
				if(!current.isEmpty() && !world.isRemote && (ctx.getPlayer()==null || !ctx.getPlayer().abilities.isCreativeMode))
				{
					double dx = pos.getX()+.5+ctx.getFace().getXOffset();
					double dy = pos.getY()+.5+ctx.getFace().getYOffset();
					double dz = pos.getZ()+.5+ctx.getFace().getZOffset();
					ItemEntity entityitem = new ItemEntity(world, dx, dy, dz, current.copy());
					entityitem.setDefaultPickupDelay();
					world.addEntity(entityitem);
				}
				((ShaderBannerTileEntity)tile).shader.setShaderItem(Utils.copyStackWithAmount(ctx.getItem(), 1));
				tile.markDirty();
				return ActionResultType.SUCCESS;
			}

		}
		return ActionResultType.FAIL;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		//TODO proper translation
		list.add(new TranslationTextComponent(Lib.DESC_INFO+"shader.level")
				.appendText(this.getRarity(stack).color.toString())
				.appendSibling(new TranslationTextComponent(Lib.DESC_INFO+"shader.rarity."+this.getRarity(stack).name().toLowerCase(Locale.US)))
		);
		if(!Screen.hasShiftDown())
			list.add(new TranslationTextComponent(Lib.DESC_INFO+"shader.applyTo")
					.appendText(" ")
					.appendSibling(new TranslationTextComponent(Lib.DESC_INFO+"holdShift")));
		else
		{
			list.add(new TranslationTextComponent(Lib.DESC_INFO+"shader.applyTo"));
			ResourceLocation rl = getShaderName(stack);
			if(rl!=null)
			{
				List<ShaderCase> array = ShaderRegistry.shaderRegistry.get(rl).getCases();
				for(ShaderCase sCase : array)
					if(!(sCase instanceof ShaderCaseItem))
						list.add(new TranslationTextComponent(Lib.DESC_INFO+"shader."+sCase.getShaderType())
								.setStyle(new Style().setColor(TextFormatting.DARK_GRAY)));
			}
		}
	}

	@Nonnull
	@Override
	public ITextComponent getDisplayName(@Nonnull ItemStack stack)
	{
		ITextComponent itc = super.getDisplayName(stack);
		ResourceLocation rl = getShaderName(stack);
		if(rl!=null)
			itc.appendText(": ").appendSibling(new TranslationTextComponent("item."+rl.getNamespace()+".shader.name."+rl.getPath()));
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
	public void fillItemGroup(ItemGroup tab, NonNullList<ItemStack> list)
	{
		if(this.isInGroup(tab))
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
					return layers[pass].getColour();
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