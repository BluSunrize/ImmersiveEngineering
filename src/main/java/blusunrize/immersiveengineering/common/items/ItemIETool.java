/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IGuiItem;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RotationUtil;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.advancements.IEAdvancements;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
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
		hammerMaxDamage = IEConfig.Tools.hammerDurabiliy;
		cutterMaxDamage = IEConfig.Tools.cutterDurabiliy;
	}

	@Override
	public int getGuiID(ItemStack stack)
	{
		return stack.getItemDamage()==3?Lib.GUIID_Manual:-1;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		if(ItemNBTHelper.hasKey(stack, "linkingPos"))
		{
			int[] link = ItemNBTHelper.getIntArray(stack, "linkingPos");
			if(link!=null&&link.length>3)
				list.add(I18n.format(Lib.DESC_INFO+"attachedToDim", link[1],link[2],link[3],link[0]));
		}
		if(stack.getItemDamage()==0)
		{
			if(ItemNBTHelper.hasKey(stack, "multiblockPermission"))
			{
				NBTTagList tagList = stack.getTagCompound().getTagList("multiblockPermission", 8);
				String s = I18n.format(Lib.DESC_INFO+"multiblocksAllowed");
				if(!GuiScreen.isShiftKeyDown())
					list.add(s+" "+I18n.format(Lib.DESC_INFO+"holdShift"));
				else
				{
					list.add(s);
					for(int i=0; i<tagList.tagCount(); i++)
						list.add(TextFormatting.DARK_GRAY+" "+I18n.format(Lib.DESC_INFO+"multiblock."+tagList.getStringTagAt(i)));
				}
			}
			if(ItemNBTHelper.hasKey(stack, "multiblockInterdiction"))
			{
				NBTTagList tagList = stack.getTagCompound().getTagList("multiblockInterdiction", 8);
				String s = I18n.format(Lib.DESC_INFO+"multiblockForbidden");
				if(!GuiScreen.isShiftKeyDown())
					list.add(s+" "+I18n.format(Lib.DESC_INFO+"holdShift"));
				else
				{
					list.add(s);
					for(int i=0; i<tagList.tagCount(); i++)
						list.add(TextFormatting.DARK_GRAY+" "+I18n.format(Lib.DESC_INFO+"multiblock."+tagList.getStringTagAt(i)));
				}
			}
		}
		if(flag.isAdvanced() && stack.getItemDamage()<2)
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
		return ItemStack.EMPTY;
	}
	//	@Override
	//	public boolean doesContainerItemLeaveCraftingGrid(ItemStack stack)
	//	{
	//		return stack.getItemDamage()!=0;
	//	}


	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if (stack.getMetadata() == 0)
		{
			String[] permittedMultiblocks = null;
			String[] interdictedMultiblocks = null;
			if (ItemNBTHelper.hasKey(stack, "multiblockPermission"))
			{
				NBTTagList list = stack.getTagCompound().getTagList("multiblockPermission", 8);
				permittedMultiblocks = new String[list.tagCount()];
				for (int i = 0; i < permittedMultiblocks.length; i++)
					permittedMultiblocks[i] = list.getStringTagAt(i);
			}
			if (ItemNBTHelper.hasKey(stack, "multiblockInterdiction"))
			{
				NBTTagList list = stack.getTagCompound().getTagList("multiblockInterdiction", 8);
				interdictedMultiblocks = new String[list.tagCount()];
				for (int i = 0; i < interdictedMultiblocks.length; i++)
					interdictedMultiblocks[i] = list.getStringTagAt(i);
			}
			for (MultiblockHandler.IMultiblock mb : MultiblockHandler.getMultiblocks())
				if (mb.isBlockTrigger(world.getBlockState(pos)))
				{
					boolean b = permittedMultiblocks == null;
					if (permittedMultiblocks != null)
						for (String s : permittedMultiblocks)
							if (mb.getUniqueName().equalsIgnoreCase(s))
							{
								b = true;
								continue;
							}
					if (!b)
						break;
					if (interdictedMultiblocks != null)
						for (String s : interdictedMultiblocks)
							if (mb.getUniqueName().equalsIgnoreCase(s))
							{
								b = false;
								continue;
							}
					if (!b)
						break;
					if (MultiblockHandler.postMultiblockFormationEvent(player, mb, pos, stack).isCanceled())
						continue;
					if (mb.createStructure(world, pos, side, player))
					{
						if(player instanceof EntityPlayerMP)
							IEAdvancements.TRIGGER_MULTIBLOCK.trigger((EntityPlayerMP)player, mb, stack);
						return EnumActionResult.SUCCESS;
					}
				}
		}
		return EnumActionResult.PASS;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		TileEntity tileEntity = world.getTileEntity(pos);
		if(stack.getItemDamage() == 0)
		{
			if(!(tileEntity instanceof IDirectionalTile) && !(tileEntity instanceof IHammerInteraction) && !(tileEntity instanceof IConfigurableSides))
				return RotationUtil.rotateBlock(world, pos, side) ? EnumActionResult.SUCCESS : EnumActionResult.PASS;
		}
		else if(stack.getItemDamage() == 1 && tileEntity instanceof IImmersiveConnectable)
		{
			TargetingInfo target = new TargetingInfo(side, hitX, hitY, hitZ);
			BlockPos masterPos = ((IImmersiveConnectable)tileEntity).getConnectionMaster(null, target);
			tileEntity = world.getTileEntity(masterPos);
			if(!(tileEntity instanceof IImmersiveConnectable))
				return EnumActionResult.PASS;

			if(!world.isRemote)
			{
				IImmersiveConnectable nodeHere = (IImmersiveConnectable)tileEntity;
				boolean cut = ImmersiveNetHandler.INSTANCE.clearAllConnectionsFor(Utils.toCC(nodeHere), world, target);
				IESaveData.setDirty(world.provider.getDimension());
				if(cut)
				{
					int nbtDamage = ItemNBTHelper.getInt(stack, "cutterDmg")+1;
					if(nbtDamage < cutterMaxDamage)
						ItemNBTHelper.setInt(stack, "cutterDmg", nbtDamage);
					else
					{
						player.renderBrokenItemStack(stack);
						player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
					}
				}
			}
			return EnumActionResult.SUCCESS;
		}
		else if(stack.getItemDamage() == 2 && !world.isRemote)
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
									player.sendMessage(new TextComponentTranslation(Lib.CHAT_INFO + "averageLoss", Utils.formatDouble(con.getAverageLossRate() * 100, "###.000")));
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
		return !player.world.isRemote && stack.getItemDamage() == 0 && RotationUtil.rotateEntity(entity, player);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player)
	{
		return stack.getItemDamage()==0;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(stack.getItemDamage()==3)
		{
			if(world.isRemote)
				CommonProxy.openGuiForItem(player, hand==EnumHand.MAIN_HAND? EntityEquipmentSlot.MAINHAND:EntityEquipmentSlot.OFFHAND);
			return new ActionResult(EnumActionResult.SUCCESS, stack);
		}
		return new ActionResult(EnumActionResult.PASS, stack);
	}

	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState)
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