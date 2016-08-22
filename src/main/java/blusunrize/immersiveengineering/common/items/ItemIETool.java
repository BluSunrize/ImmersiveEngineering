package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IGuiItem;
import blusunrize.immersiveengineering.common.util.*;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemIETool extends ItemIEBase implements ITool, IGuiItem
{
	static int hammerMaxDamage;
	static int cutterMaxDamage;
	public ItemIETool()
	{
		super("tool", 1, "hammer","wirecutter","voltmeter","manual");
		hammerMaxDamage = Config.getInt("hammerDurabiliy");
		cutterMaxDamage = Config.getInt("cutterDurabiliy");
	}

	@Override
	public int getGuiID(ItemStack stack)
	{
		return stack.getItemDamage()==3?Lib.GUIID_Manual:-1;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		if(ItemNBTHelper.hasKey(stack, "linkingPos"))
		{
			int[] link = ItemNBTHelper.getIntArray(stack, "linkingPos");
			if(link!=null&&link.length>3)
				list.add(I18n.format(Lib.DESC_INFO+"attachedToDim", link[1],link[2],link[3],link[0]));
		}
		if(adv && stack.getItemDamage()<2)
		{
			int nbtDamage = ItemNBTHelper.getInt(stack, stack.getItemDamage()==0?"hammerDmg":"cutterDmg");
			int maxDamage = stack.getItemDamage()==0?hammerMaxDamage:cutterMaxDamage;
			list.add("Durability: "+(maxDamage-nbtDamage)+" / "+maxDamage);
		}
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return stack.getItemDamage()<2;
	}
	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		if(stack.getItemDamage()==0)
		{
			int nbtDamage = ItemNBTHelper.getInt(stack, "hammerDmg")+1;
			if(nbtDamage<hammerMaxDamage)
			{
				ItemStack container = stack.copy();
				ItemNBTHelper.setInt(container, "hammerDmg", nbtDamage);
				return container;
			}
		}
		else if(stack.getItemDamage()==1)
		{
			int nbtDamage = ItemNBTHelper.getInt(stack, "cutterDmg")+1;
			if(nbtDamage<cutterMaxDamage)
			{
				ItemStack container = stack.copy();
				ItemNBTHelper.setInt(container, "cutterDmg", nbtDamage);
				return container;
			}
		}
		return null;
	}
	//	@Override
	//	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	//	{
	//		return stack.getItemDamage()!=0;
	//	}

	@Override
	public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		if(stack.getItemDamage() == 0)
		{
			if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
				return EnumActionResult.PASS;
			String[] interdictedMultiblocks = null;
			if(ItemNBTHelper.hasKey(stack, "multiblockInterdiction"))
			{
				NBTTagList list = stack.getTagCompound().getTagList("multiblockInterdiction", 8);
				interdictedMultiblocks = new String[list.tagCount()];
				for(int i = 0; i < interdictedMultiblocks.length; i++)
					interdictedMultiblocks[i] = list.getStringTagAt(i);
			}
			for(IMultiblock mb : MultiblockHandler.getMultiblocks())
				if(mb.isBlockTrigger(world.getBlockState(pos)))
				{
					if(interdictedMultiblocks != null)
						for(String s : interdictedMultiblocks)
							if(mb.getUniqueName().equalsIgnoreCase(s))
								return EnumActionResult.FAIL;
					if(mb.createStructure(world, pos, side, player))
						return EnumActionResult.SUCCESS;
				}
			if(!(world.getTileEntity(pos) instanceof IDirectionalTile))
				return RotationUtil.rotateBlock(world, pos, side, player) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
		} else if(stack.getItemDamage() == 1 && tileEntity instanceof IImmersiveConnectable && !world.isRemote)
		{
			IImmersiveConnectable nodeHere = (IImmersiveConnectable) tileEntity;
			ImmersiveNetHandler.INSTANCE.clearAllConnectionsFor(Utils.toCC(nodeHere), world, new TargetingInfo(side, hitX, hitY, hitZ));
			IESaveData.setDirty(world.provider.getDimension());

			int nbtDamage = ItemNBTHelper.getInt(stack, "cutterDmg") + 1;
			if(nbtDamage < cutterMaxDamage)
				ItemNBTHelper.setInt(stack, "cutterDmg", nbtDamage);
			else
			{
				player.renderBrokenItemStack(stack);
				player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, null);
			}
			return EnumActionResult.SUCCESS;
		} else if(stack.getItemDamage() == 2 && !world.isRemote)
		{
			if(!player.isSneaking() && (tileEntity instanceof IFluxReceiver || tileEntity instanceof IFluxProvider))
			{
				int max = 0;
				int stored = 0;
				if(tileEntity instanceof IFluxReceiver)
				{
					max = ((IFluxReceiver) tileEntity).getMaxEnergyStored(side);
					stored = ((IFluxReceiver) tileEntity).getEnergyStored(side);
				}
				else
				{
					max = ((IFluxProvider) tileEntity).getMaxEnergyStored(side);
					stored = ((IFluxProvider) tileEntity).getEnergyStored(side);
				}
				if(max > 0)
					ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO + "energyStorage", stored, max));
				return EnumActionResult.SUCCESS;
			}
			if(player.isSneaking() && tileEntity instanceof IImmersiveConnectable)
			{
				if(!ItemNBTHelper.hasKey(stack, "linkingPos"))
					ItemNBTHelper.setIntArray(stack, "linkingPos", new int[]{world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ()});
				else
				{
					int[] array = ItemNBTHelper.getIntArray(stack, "linkingPos");
					BlockPos linkPos = new BlockPos(array[1], array[2], array[3]);
					TileEntity tileEntityLinkingPos = world.getTileEntity(linkPos);
					if(array[0] == world.provider.getDimension())
					{
						IImmersiveConnectable nodeHere = (IImmersiveConnectable) tileEntity;
						IImmersiveConnectable nodeLink = (IImmersiveConnectable) tileEntityLinkingPos;
						if(nodeLink != null)
						{
							Set<AbstractConnection> connections = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(nodeLink), world, true);
							for(AbstractConnection con : connections)
								if(Utils.toCC(nodeHere).equals(con.end))
									player.addChatComponentMessage(new TextComponentTranslation(Lib.CHAT_INFO + "averageLoss", Utils.formatDouble(con.getAverageLossRate() * 100, "###.000")));
						}
					}
					ItemNBTHelper.remove(stack, "linkingPos");
				}
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.PASS;
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand)
	{
		return !player.worldObj.isRemote && stack.getItemDamage() == 0 && RotationUtil.rotateEntity(entity, player);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player)
	{
		return stack.getItemDamage()==0;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand)
	{
		if(stack.getItemDamage()==3)
		{
			player.addStat(IEAchievements.openManual);
			if(world.isRemote)
				CommonProxy.openGuiForItem(player, hand==EnumHand.MAIN_HAND? EntityEquipmentSlot.MAINHAND:EntityEquipmentSlot.OFFHAND);
			return new ActionResult(EnumActionResult.SUCCESS, stack);
		}
		return new ActionResult(EnumActionResult.PASS, stack);
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
	public boolean showDurabilityBar(ItemStack stack)
	{
		if(stack.getItemDamage()==0)
			return (ItemNBTHelper.getInt(stack, "hammerDmg")>0);
		else if(stack.getItemDamage()==1)
			return (ItemNBTHelper.getInt(stack, "cutterDmg")>0);
		return false;
	}
	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		if(stack.getItemDamage()==0)
			return ItemNBTHelper.getInt(stack, "hammerDmg") / (double)hammerMaxDamage;
		else if(stack.getItemDamage()==1)
			return ItemNBTHelper.getInt(stack, "cutterDmg") / (double)cutterMaxDamage;
		return 0;
	}
	@Override
	public boolean isDamaged(ItemStack stack)
	{
		return false;
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		int meta = stack.getItemDamage();
		return meta==0?ImmutableSet.of(Lib.TOOL_HAMMER): meta==1?ImmutableSet.of(Lib.TOOL_WIRECUTTER): new HashSet<String>();
	}

	@Override
	public float getStrVsBlock(ItemStack stack, IBlockState state)
	{
		for(String type : this.getToolClasses(stack))
			if(state.getBlock().isToolEffective(type, state))
				return 6;
		return super.getStrVsBlock(stack, state);
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return item.getItemDamage()!=3;
	}

	//	@Override
	//	@Optional.Method(modid = "CoFHAPI|item")
	//	public boolean isUsable(ItemStack stack, EntityLivingBase living, int x, int y, int z)
	//	{
	//		return stack!=null&&stack.getItemDamage()==0;
	//	}
	//
	//	@Override
	//	@Optional.Method(modid = "CoFHAPI|item")
	//	public void toolUsed(ItemStack stack, EntityLivingBase living, int x, int y, int z)
	//	{
	//	}
}