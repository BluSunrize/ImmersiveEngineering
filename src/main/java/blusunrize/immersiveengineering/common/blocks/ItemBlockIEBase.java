/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockIEBase extends ItemBlock
{
	private int burnTime;

	public ItemBlockIEBase(Block b)
	{
		super(b, new Item.Properties().group(ImmersiveEngineering.itemGroup));
	}

	@Override
	public String getTranslationKey(ItemStack stack)
	{
		return getBlock().getTranslationKey();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack)
	{
		return ClientProxy.itemFont;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag advanced)
	{
		if(getBlock().hasFlavour())
		{
			String flavourKey = Lib.DESC_FLAVOUR+getBlock().name;
			//TODO color
			tooltip.add(new TextComponentTranslation(I18n.format(flavourKey)));
		}
		super.addInformation(stack, world, tooltip, advanced);
		if(ItemNBTHelper.hasKey(stack, "energyStorage"))
			tooltip.add(new TextComponentTranslation(Lib.DESC_INFO+"energyStored",
					ItemNBTHelper.getInt(stack, "energyStorage")));
		if(ItemNBTHelper.hasKey(stack, "tank"))
		{
			FluidStack fs = FluidStack.loadFluidStackFromNBT(ItemNBTHelper.getTagCompound(stack, "tank"));
			if(fs!=null)
				tooltip.add(new TextComponentTranslation(Lib.DESC_INFO+"fluidStored",
						fs.getLocalizedName(), fs.amount));
		}
	}


	public ItemBlockIEBase setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
	}

	@Override
	public int getBurnTime(ItemStack itemStack)
	{
		return this.burnTime;
	}

	@Override
	protected boolean placeBlock(BlockItemUseContext context, IBlockState newState)
	{
		if(!getBlock().canIEBlockBePlaced(newState, context))
			return false;
		boolean ret = super.placeBlock(context, newState);
		if(ret)
			getBlock().onIEBlockPlacedBy(context, newState);
		return ret;
	}

	@Override
	public EnumActionResult tryPlace(BlockItemUseContext context)
	{
		EntityPlayer player = context.getPlayer();
		BlockPos pos = context.getPos();
		World world = context.getWorld();
		if(player==null)
			return EnumActionResult.FAIL;
		EnumHand hand = player.getActiveHand();//TODO is this correct?
		ItemStack stack = player.getHeldItem(hand);
		IBlockState currState = world.getBlockState(pos);
		Block block = currState.getBlock();
		EnumFacing side = context.getFace();
		if(!block.isReplaceable(currState, context))
			pos = pos.offset(side);
		IBlockState newState = getBlock().getStateForPlacement(context);
		if(stack.getCount() > 0&&player.canPlayerEdit(pos, side, stack)&&canPlace(context, newState))
		{
			if(newState!=null&&placeBlock(context, newState))
			{
				SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
				world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume()+1.0F)/2.0F, soundtype.getPitch()*0.8F);
				if(!player.abilities.isCreativeMode)
					stack.shrink(1);
			}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.FAIL;
	}

	@Override
	protected boolean canPlace(BlockItemUseContext context, IBlockState state)
	{
		World worldIn = context.getWorld();
		BlockPos pos = context.getPos();
		IBlockState oldState = worldIn.getBlockState(pos);
		EnumFacing side = context.getFace();
		if(oldState.getBlock()==Blocks.SNOW&&oldState.isReplaceable(context))
			side = EnumFacing.UP;
		else if(!oldState.isReplaceable(context))
			pos = pos.offset(side);
		return worldIn.checkNoEntityCollision(state, pos);
	}

	@Override
	public BlockIEBase getBlock()
	{
		return (BlockIEBase)super.getBlock();
	}

	public static class ItemBlockIENoInventory extends ItemBlockIEBase
	{
		public ItemBlockIENoInventory(Block b)
		{
			super(b);
		}

		@Nullable
		@Override
		public NBTTagCompound getShareTag(ItemStack stack)
		{
			NBTTagCompound ret = super.getShareTag(stack);
			if(ret!=null)
			{
				ret = ret.copy();
				ret.removeTag("inventory");
			}
			return ret;
		}
	}
}