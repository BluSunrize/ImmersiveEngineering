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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

public class BlockItemIE extends BlockItem
{
	private int burnTime;

	public BlockItemIE(Block b)
	{
		super(b, new Item.Properties().group(ImmersiveEngineering.itemGroup));
		setRegistryName(b.getRegistryName());
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
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag advanced)
	{
		if(getBlock() instanceof IEBaseBlock)
		{
			IEBaseBlock ieBlock = (IEBaseBlock)getBlock();
			if(ieBlock.hasFlavour())
			{
				String flavourKey = Lib.DESC_FLAVOUR+ieBlock.name;
				//TODO color
				tooltip.add(new TranslationTextComponent(I18n.format(flavourKey)));
			}
		}
		super.addInformation(stack, world, tooltip, advanced);
		if(ItemNBTHelper.hasKey(stack, "energyStorage"))
			tooltip.add(new TranslationTextComponent(Lib.DESC_INFO+"energyStored",
					ItemNBTHelper.getInt(stack, "energyStorage")));
		if(ItemNBTHelper.hasKey(stack, "tank"))
		{
			FluidStack fs = FluidStack.loadFluidStackFromNBT(ItemNBTHelper.getTagCompound(stack, "tank"));
			if(fs!=null)
				tooltip.add(new TranslationTextComponent(Lib.DESC_INFO+"fluidStored",
						fs.getDisplayName(), fs.getAmount()));
		}
	}


	public BlockItemIE setBurnTime(int burnTime)
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
	protected boolean placeBlock(BlockItemUseContext context, BlockState newState)
	{
		Block b = getBlock();
		if(b instanceof IEBaseBlock)
		{
			IEBaseBlock ieBlock = (IEBaseBlock)b;
			if(!ieBlock.canIEBlockBePlaced(newState, context))
				return false;
			boolean ret = super.placeBlock(context, newState);
			if(ret)
				ieBlock.onIEBlockPlacedBy(context, newState);
			return ret;
		}
		else
			return super.placeBlock(context, newState);
	}

	@Override
	public ActionResultType tryPlace(BlockItemUseContext context)
	{
		PlayerEntity player = context.getPlayer();
		BlockPos pos = context.getPos();
		World world = context.getWorld();
		if(player==null)
			return ActionResultType.FAIL;
		Hand hand = player.getActiveHand();//TODO is this correct?
		ItemStack stack = player.getHeldItem(hand);
		BlockState currState = world.getBlockState(pos);
		Block block = currState.getBlock();
		Direction side = context.getFace();
		if(!block.isReplaceable(currState, context))
			pos = pos.offset(side);
		BlockState newState = getBlock().getStateForPlacement(context);
		if(stack.getCount() > 0&&player.canPlayerEdit(pos, side, stack)&&canPlace(context, newState))
		{
			if(newState!=null&&placeBlock(context, newState))
			{
				SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
				world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume()+1.0F)/2.0F, soundtype.getPitch()*0.8F);
				if(!player.abilities.isCreativeMode)
					stack.shrink(1);
			}
			return ActionResultType.SUCCESS;
		}
		return ActionResultType.FAIL;
	}

	@Override
	protected boolean canPlace(BlockItemUseContext context, BlockState state)
	{
		World worldIn = context.getWorld();
		BlockPos pos = context.getPos();
		BlockState oldState = worldIn.getBlockState(pos);
		Direction side = context.getFace();
		if(oldState.getBlock()==Blocks.SNOW&&oldState.isReplaceable(context))
			side = Direction.UP;
		else if(!oldState.isReplaceable(context))
			pos = pos.offset(side);
		//TODO more accurate shape?
		return worldIn.checkNoEntityCollision(null, VoxelShapes.fullCube().withOffset(pos.getX(), pos.getY(), pos.getZ()));
	}

	public static class BlockItemIENoInventory extends BlockItemIE
	{
		public BlockItemIENoInventory(Block b)
		{
			super(b);
		}

		@Nullable
		@Override
		public CompoundNBT getShareTag(ItemStack stack)
		{
			CompoundNBT ret = super.getShareTag(stack);
			if(ret!=null)
			{
				ret = ret.copy();
				ret.remove("inventory");
			}
			return ret;
		}
	}
}