/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemJerrycan extends ItemIEBase
{
	public ItemJerrycan()
	{
		super("jerrycan", new Properties().maxStackSize(1));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		LazyOptional<FluidStack> fsCap = FluidUtil.getFluidContained(stack);
		fsCap.ifPresent(fs ->
		{
			TextFormatting rarity = fs.getFluid().getRarity()==EnumRarity.COMMON?TextFormatting.GRAY: fs.getFluid().getRarity().color;
			list.add(new TextComponentTranslation(Lib.DESC_FLAVOUR+"fluidStack", fs.amount, 10000)
					.setStyle(new Style().setColor(rarity)));
		});
		if(!fsCap.isPresent())
			list.add(new TextComponentTranslation(Lib.DESC_FLAVOUR+"drill.empty"));
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(ItemUseContext ctx)
	{
		World world = ctx.getWorld();
		BlockPos pos = ctx.getPos();
		ItemStack stack = ctx.getItem();
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity==null||!tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).isPresent())
		{
			FluidStack fs = FluidUtil.getFluidContained(stack).orElseThrow(RuntimeException::new);
			if(Utils.placeFluidBlock(world, pos.offset(ctx.getFace()), fs))
			{
				if(fs.amount <= 0)
					fs = null;
				ItemNBTHelper.setFluidStack(stack, "Fluid", fs);
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.PASS;
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return ItemNBTHelper.hasKey(stack, "jerrycanDrain")||FluidUtil.getFluidContained(stack)!=null;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "jerrycanDrain"))
		{
			ItemStack ret = stack.copy();
			IFluidHandler handler = FluidUtil.getFluidHandler(ret).orElseThrow(RuntimeException::new);
			handler.drain(ItemNBTHelper.getInt(ret, "jerrycanDrain"), true);
			ItemNBTHelper.remove(ret, "jerrycanDrain");
			return ret;
		}
		else if(FluidUtil.getFluidContained(stack)!=null)
		{
			ItemStack ret = stack.copy();
			IFluidHandler handler = FluidUtil.getFluidHandler(ret).orElseThrow(RuntimeException::new);
			handler.drain(1000, true);
			return ret;
		}
		return stack;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
	{
		if(!stack.isEmpty())
			return new FluidHandlerItemStack(stack, 10000);
		return null;
	}
}