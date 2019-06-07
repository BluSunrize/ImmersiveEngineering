/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.DimensionChunkCoords;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDevices;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCoresample extends ItemIEBase
{
	public ItemCoresample()
	{
		super("coresample", 1);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		DimensionChunkCoords coords = DimensionChunkCoords.readFromNBT(stack.getOrCreateTag().getCompound("coords"));
		if(coords!=null)
		{
			if(ItemNBTHelper.hasKey(stack, "mineral"))
			{
				String mineral = ItemNBTHelper.getString(stack, "mineral");
				String unloc = Lib.DESC_INFO+"mineral."+mineral;
				String loc = I18n.format(unloc);
				list.add(new TextComponentTranslation(Lib.CHAT_INFO+"coresample.mineral", (unloc.equals(loc)?mineral: loc)));
			}
			else
				list.add(new TextComponentTranslation(Lib.CHAT_INFO+"coresample.noMineral"));
			boolean singleplayer = Minecraft.getInstance().isSingleplayer();
			if(world==null||world.getDimension().getType()!=coords.dimension)
			{
				World clientWorld = Minecraft.getInstance().world;
				if(clientWorld!=null&&clientWorld.getDimension().getType()==coords.dimension)
					world = clientWorld;
				else
					world = null;
			}
			String s0 = (coords.x*16)+", "+(coords.z*16);
			String s1 = (coords.x*16+16)+", "+(coords.z*16+16);
			//TODO
			String s2 = coords.dimension.getRegistryName().getPath();
			list.add(new TextComponentString(s2));
			list.add(new TextComponentTranslation(Lib.CHAT_INFO+"coresample.pos", s0, s1, ""));

			if(ItemNBTHelper.hasKey(stack, "infinite"))
				list.add(new TextComponentTranslation(Lib.CHAT_INFO+"coresample.infinite"));
			else if(ItemNBTHelper.hasKey(stack, "depletion"))
				list.add(new TextComponentTranslation(Lib.CHAT_INFO+"coresample.yield", ExcavatorHandler.mineralVeinCapacity-ItemNBTHelper.getInt(stack, "depletion")));

			boolean hasStamp = ItemNBTHelper.hasKey(stack, "timestamp");
			if(hasStamp&&world!=null)
			{
				long timestamp = ItemNBTHelper.getLong(stack, "timestamp");
				long dist = world.getGameTime()-timestamp;
				if(dist < 0)
					list.add(new TextComponentString("Somehow this sample is dated in the future...are you a time traveller?!"));
				else
					list.add(new TextComponentTranslation(Lib.CHAT_INFO+"coresample.timestamp", ClientUtils.fomatTimestamp(dist, ClientUtils.TimestampFormat.DHM)));
			}
			else if(hasStamp)
				list.add(new TextComponentTranslation(Lib.CHAT_INFO+"coresample.timezone"));
			else
				list.add(new TextComponentTranslation(Lib.CHAT_INFO+"coresample.noTimestamp"));
		}
	}


	@Override
	public EnumActionResult onItemUse(ItemUseContext ctx)
	{
		EntityPlayer player = ctx.getPlayer();
		ItemStack stack = ctx.getItem();
		if(player!=null&&player.isSneaking())
		{
			World world = ctx.getWorld();
			BlockPos pos = ctx.getPos();
			EnumFacing side = ctx.getFace();
			IBlockState state = world.getBlockState(pos);
			if(!state.isReplaceable(new BlockItemUseContext(ctx)))
				pos = pos.offset(side);

			if(!stack.isEmpty()&&player.canPlayerEdit(pos, side, stack)&&world.mayPlace(IEContent.blockStoneDevice, pos, false, side, null))
			{
				IBlockState toolbox = IEContent.blockStoneDevice.getStateFromMeta(BlockTypes_StoneDevices.CORESAMPLE.getMeta());
				if(world.setBlockState(pos, toolbox, 3))
				{
					IEContent.blockStoneDevice.onIEBlockPlacedBy(, world, toolbox);
					SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
					world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume()+1.0F)/2.0F, soundtype.getPitch()*0.8F);
					stack.shrink(1);
				}
				return EnumActionResult.SUCCESS;
			}
			else
				return EnumActionResult.FAIL;
		}
		return super.onItemUse(ctx);
	}

}