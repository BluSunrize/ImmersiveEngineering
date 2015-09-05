package blusunrize.immersiveengineering.common.items;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.ForgeDirection;
import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;

import com.google.common.collect.ImmutableSet;

public class ItemIETool extends ItemIEBase
{
	public ItemIETool()
	{
		super("tool", 1, "hammer","wirecutter","voltmeter","manual");
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		if(ItemNBTHelper.hasKey(stack, "linkingPos"))
		{
			int[] link = ItemNBTHelper.getIntArray(stack, "linkingPos");
			if(link!=null&&link.length>3)
				list.add(StatCollector.translateToLocalFormatted(Lib.DESC_INFO+"attachedToDim", link[1],link[2],link[3],link[0]));
		}
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return stack.getItemDamage()==0;
	}
	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		return stack.getItemDamage()==0?stack: null;
	}
	@Override
    public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
    {
        return stack.getItemDamage()!=0;
    }
	
	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(!world.isRemote)
		{
			if(stack.getItemDamage()==0)
			{
				for(IMultiblock mb : MultiblockHandler.getMultiblocks())
					if(mb.isBlockTrigger(world.getBlock(x, y, z), world.getBlockMetadata(x, y, z)) && mb.createStructure(world, x, y, z, side, player))
						return true;
			}
			else if(stack.getItemDamage()==1 && world.getTileEntity(x, y, z) instanceof IImmersiveConnectable)
			{
				IImmersiveConnectable nodeHere = (IImmersiveConnectable)world.getTileEntity(x, y, z);
				ImmersiveNetHandler.INSTANCE.clearAllConnectionsFor(Utils.toCC(nodeHere),world, new TargetingInfo(side,hitX,hitY,hitZ));
				IESaveData.setDirty(world.provider.dimensionId);
				return true;
			}
			else if(stack.getItemDamage()==2)
			{
				if(!player.isSneaking() && (world.getTileEntity(x, y, z) instanceof IEnergyReceiver || world.getTileEntity(x, y, z) instanceof IEnergyProvider))
				{
					int max = 0;
					int stored = 0;
					if(world.getTileEntity(x, y, z) instanceof IEnergyReceiver)
					{
						max = ((IEnergyReceiver)world.getTileEntity(x, y, z)).getMaxEnergyStored(ForgeDirection.getOrientation(side));
						stored = ((IEnergyReceiver)world.getTileEntity(x, y, z)).getEnergyStored(ForgeDirection.getOrientation(side));
					}
					else if(world.getTileEntity(x, y, z) instanceof IEnergyProvider)
					{
						max = ((IEnergyProvider)world.getTileEntity(x, y, z)).getMaxEnergyStored(ForgeDirection.getOrientation(side));
						stored = ((IEnergyProvider)world.getTileEntity(x, y, z)).getEnergyStored(ForgeDirection.getOrientation(side));
					}
					if(max>0)
						player.addChatMessage(new ChatComponentTranslation(Lib.CHAT_INFO+"energyStorage", stored,max));
				}
				if(player.isSneaking() && world.getTileEntity(x, y, z) instanceof IImmersiveConnectable)
				{
					if(!ItemNBTHelper.hasKey(stack, "linkingPos"))
						ItemNBTHelper.setIntArray(stack, "linkingPos", new int[]{world.provider.dimensionId,x,y,z});
					else
					{
						int[] pos = ItemNBTHelper.getIntArray(stack, "linkingPos");
						if(pos[0]==world.provider.dimensionId)
						{
							IImmersiveConnectable nodeHere = (IImmersiveConnectable)world.getTileEntity(x, y, z);
							IImmersiveConnectable nodeLink = (IImmersiveConnectable)world.getTileEntity(pos[1], pos[2], pos[3]);
							if(nodeHere!=null && nodeLink!=null)
							{
								ConcurrentSkipListSet<AbstractConnection> connections = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(nodeLink), world);
								for(AbstractConnection con : connections)
									if(Utils.toCC(nodeHere).equals(con.end))
										player.addChatComponentMessage(new ChatComponentTranslation(Lib.CHAT_INFO+"averageLoss",Utils.formatDouble(con.getAverageLossRate()*100, "###.000")));
							}
						}
						ItemNBTHelper.remove(stack, "linkingPos");
					}
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		if(player.getCurrentEquippedItem()!=null && this.equals(player.getCurrentEquippedItem().getItem()))
			return player.getCurrentEquippedItem().getItemDamage()==0;
		return false;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if(world.isRemote && stack.getItemDamage()==3)
			player.openGui(ImmersiveEngineering.instance, Lib.GUIID_Manual, world, (int)player.posX,(int)player.posY,(int)player.posZ);
		return stack;
	}

	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass)
	{
		if(getToolClasses(stack).contains(toolClass))
			return 2;
		else
			return -1;
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		int meta = stack.getItemDamage();
		return meta==0?ImmutableSet.of(Lib.TOOL_HAMMER): meta==1?ImmutableSet.of(Lib.TOOL_WIRECUTTER): new HashSet<String>();
	}

	@Override
	public float getDigSpeed(ItemStack stack, Block block, int meta)
	{
		if(ForgeHooks.isToolEffective(stack, block, meta))
			return 6;
		return super.getDigSpeed(stack, block, meta);
	}
}