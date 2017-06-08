package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.stone.BlockTypes_StoneDevices;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Locale;

public class ItemCoresample extends ItemIEBase
{
	public ItemCoresample()
	{
		super("coresample", 1);
	}
	@Override
	public void getSubItems(Item item, CreativeTabs tab, NonNullList<ItemStack> list)
	{
		super.getSubItems(item, tab, list);
	}
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		if(ItemNBTHelper.hasKey(stack, "coords"))
		{
			if(ItemNBTHelper.hasKey(stack, "mineral"))
			{
				String mineral = ItemNBTHelper.getString(stack, "mineral");
				String unloc = Lib.DESC_INFO+"mineral."+mineral;
				String loc = I18n.format(unloc);
				list.add(I18n.format(Lib.CHAT_INFO+"coresample.mineral", (unloc.equals(loc)?mineral:loc)));
			}
			else
				list.add(I18n.format(Lib.CHAT_INFO+"coresample.noMineral"));

			int[] coords = ItemNBTHelper.getIntArray(stack, "coords");
			World world = DimensionManager.getWorld(coords[0]);
			String s0 = (coords[1]*16)+", "+(coords[2]*16);
			String s1 = (coords[1]*16+16)+", "+(coords[2]*16+16);
			String s2;
			if(world!=null && world.provider!=null)
			{
				String name = world.provider.getDimensionType().getName();
				if(name.toLowerCase(Locale.ENGLISH).startsWith("the "))
					name = name.substring(4);
				s2 = name;
			}
			else
				s2 = "Dimension "+coords[0];
			list.add(s2);
			list.add(I18n.format(Lib.CHAT_INFO+"coresample.pos", s0,s1,""));

			if(ItemNBTHelper.hasKey(stack, "infinite"))
				list.add(I18n.format(Lib.CHAT_INFO+"coresample.infinite"));
			else if(ItemNBTHelper.hasKey(stack, "depletion"))
				list.add(I18n.format(Lib.CHAT_INFO+"coresample.yield", ExcavatorHandler.mineralVeinCapacity-ItemNBTHelper.getInt(stack, "depletion")));

			if(ItemNBTHelper.hasKey(stack, "timestamp") && world!=null)
			{
				long timestamp = ItemNBTHelper.getLong(stack, "timestamp");
				long dist = world.getTotalWorldTime()-timestamp;
				if(dist<0)
					list.add("Somehow this sample is dated in the future...are you a time traveller?!");
				else
					list.add(I18n.format(Lib.CHAT_INFO+"coresample.timestamp", ClientUtils.fomatTimestamp(dist, ClientUtils.TimestampFormat.DHM)));
			}
			else
				list.add(I18n.format(Lib.CHAT_INFO+"coresample.noTimestamp"));
		}
	}


	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(player.isSneaking())
		{
			IBlockState state = world.getBlockState(pos);
			Block block = state.getBlock();
			if(!block.isReplaceable(world, pos))
				pos = pos.offset(side);

			if(!stack.isEmpty() && player.canPlayerEdit(pos, side, stack) && world.mayPlace(IEContent.blockStoneDevice, pos, false, side, null))
			{
				IBlockState toolbox = IEContent.blockStoneDevice.getStateFromMeta(BlockTypes_StoneDevices.CORESAMPLE.getMeta());
				if(world.setBlockState(pos, toolbox, 3))
				{
					IEContent.blockStoneDevice.onIEBlockPlacedBy(world, pos, toolbox, side, hitX,hitY,hitZ, player, stack);
					SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
					world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					stack.shrink(1);
				}
				return EnumActionResult.SUCCESS;
			}
			else
				return EnumActionResult.FAIL;
		}
		return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
	}

}