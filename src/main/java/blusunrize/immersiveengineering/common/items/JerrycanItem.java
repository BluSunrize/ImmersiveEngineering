/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.fluids.IEItemFluidHandler;
import blusunrize.immersiveengineering.common.items.ItemCapabilityRegistration.ItemCapabilityRegistrar;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class JerrycanItem extends IEBaseItem
{
	private static final int jerrycanMaxMB = 10*FluidType.BUCKET_VOLUME;

	public JerrycanItem()
	{
		super(new Properties().stacksTo(1));
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		Optional<FluidStack> fsCap = FluidUtil.getFluidContained(stack);
		fsCap.ifPresent(fs -> list.add(IEItemFluidHandler.fluidItemInfoFlavor(fs, jerrycanMaxMB)));
	}

	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext ctx)
	{
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		ItemStack stack = ctx.getItemInHand();
		if(world.getCapability(FluidHandler.BLOCK, pos, null)==null)
		{
			Optional<FluidStack> fs = FluidUtil.getFluidContained(stack);
			if(fs.isPresent()&&Utils.placeFluidBlock(world, pos.relative(ctx.getClickedFace()), fs.get()))
			{
				ItemNBTHelper.setFluidStack(stack, "Fluid", fs.get());
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	public boolean hasCraftingRemainingItem(ItemStack stack)
	{
		return ItemNBTHelper.hasKey(stack, "jerrycanDrain")||FluidUtil.getFluidContained(stack).isPresent();
	}

	@Override
	public ItemStack getCraftingRemainingItem(ItemStack stack)
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
			handler.drain(FluidType.BUCKET_VOLUME, FluidAction.EXECUTE);
			return ret;
		}
		return stack;
	}

	public static void registerCapabilities(ItemCapabilityRegistrar registrar)
	{
		registrar.register(
				FluidHandler.ITEM, (stack, $) -> new FluidHandlerItemStack(stack, jerrycanMaxMB)
		);
	}
}
