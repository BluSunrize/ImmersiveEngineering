/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.fluids.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class JerrycanItem extends IEBaseItem
{
	private final int jerrycanMaxMB = 10*FluidAttributes.BUCKET_VOLUME;

	public JerrycanItem()
	{
		super("jerrycan", new Properties().maxStackSize(1));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		if(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY!=null) //cap is null until after ForgeMod.preInit, and Minecraft.init calls this method before that
		{
			Optional<FluidStack> fsCap = FluidUtil.getFluidContained(stack);
			fsCap.ifPresent(fs -> list.add(IEItemFluidHandler.fluidItemInfoFlavor(fs, jerrycanMaxMB)));
		}
	}

	@Nonnull
	@Override
	public ActionResultType onItemUse(ItemUseContext ctx)
	{
		World world = ctx.getWorld();
		BlockPos pos = ctx.getPos();
		ItemStack stack = ctx.getItem();
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity==null||!tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY).isPresent())
		{
			Optional<FluidStack> fs = FluidUtil.getFluidContained(stack);
			if(fs.isPresent()&&Utils.placeFluidBlock(world, pos.offset(ctx.getFace()), fs.get()))
			{
				ItemNBTHelper.setFluidStack(stack, "Fluid", fs.get());
				return ActionResultType.SUCCESS;
			}
		}
		return ActionResultType.PASS;
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return ItemNBTHelper.hasKey(stack, "jerrycanDrain")||FluidUtil.getFluidContained(stack).isPresent();
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "jerrycanDrain"))
		{
			ItemStack ret = stack.copy();
			IFluidHandler handler = FluidUtil.getFluidHandler(ret).orElseThrow(RuntimeException::new);
			handler.drain(ItemNBTHelper.getInt(ret, "jerrycanDrain"), FluidAction.EXECUTE);
			ItemNBTHelper.remove(ret, "jerrycanDrain");
			return ret;
		}
		else if(FluidUtil.getFluidContained(stack).isPresent())
		{
			ItemStack ret = stack.copy();
			IFluidHandler handler = FluidUtil.getFluidHandler(ret).orElseThrow(RuntimeException::new);
			handler.drain(FluidAttributes.BUCKET_VOLUME, FluidAction.EXECUTE);
			return ret;
		}
		return stack;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundNBT nbt)
	{
		if(!stack.isEmpty())
			return new FluidHandlerItemStack(stack, jerrycanMaxMB);
		return null;
	}
}
