/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxProvider;
import blusunrize.immersiveengineering.api.energy.immersiveflux.IFluxReceiver;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.AbstractConnection;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.blocks.BlockIEBase;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IHammerInteraction;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IGuiItem;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IItemDamageableIE;
import blusunrize.immersiveengineering.common.util.ChatUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RotationUtil;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.advancements.IEAdvancements;
import com.google.common.collect.ImmutableSet;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Enchantments;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.StatList;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static blusunrize.immersiveengineering.common.Config.IEConfig.Tools.cutterDurabiliy;
import static blusunrize.immersiveengineering.common.Config.IEConfig.Tools.hammerDurabiliy;

public class ItemIETool extends ItemIEBase implements ITool, IGuiItem, IItemDamageableIE
{
	public static final int HAMMER_META = 0;
	public static final int CUTTER_META = 1;
	public static final int VOLTMETER_META = 2;
	public static final int MANUAL_META = 3;

	public ItemIETool()
	{
		super("tool", 1, "hammer", "wirecutter", "voltmeter", "manual");
		canRepair = false;//Uses a custom repair recipe to prevent #2990
	}

	@Override
	public int getGuiID(ItemStack stack)
	{
		return stack.getMetadata()==MANUAL_META?Lib.GUIID_Manual: -1;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		if(ItemNBTHelper.hasKey(stack, "linkingPos"))
		{
			int[] link = ItemNBTHelper.getIntArray(stack, "linkingPos");
			if(link!=null&&link.length > 3)
				list.add(I18n.format(Lib.DESC_INFO+"attachedToDim", link[1], link[2], link[3], link[0]));
		}
		if(stack.getMetadata()==HAMMER_META)
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
					for(int i = 0; i < tagList.tagCount(); i++)
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
					for(int i = 0; i < tagList.tagCount(); i++)
						list.add(TextFormatting.DARK_GRAY+" "+I18n.format(Lib.DESC_INFO+"multiblock."+tagList.getStringTagAt(i)));
				}
			}
		}
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return stack.getMetadata() < VOLTMETER_META;
	}

	@Nonnull
	@Override
	public ItemStack getContainerItem(@Nonnull ItemStack stack)
	{
		if(stack.getMetadata()==HAMMER_META||stack.getMetadata()==CUTTER_META)
		{
			ItemStack container = stack.copy();
			this.damageIETool(container, 1, Utils.RAND, null);
			return container;
		}
		return ItemStack.EMPTY;
	}

	private void damageIETool(ItemStack stack, int amount, Random rand, @Nullable EntityPlayer player)
	{
		if(amount <= 0)
			return;

		int unbreakLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, stack);
		for(int i = 0; unbreakLevel > 0&&i < amount; i++)
			if(EnchantmentDurability.negateDamage(stack, unbreakLevel, rand))
				amount--;
		if(amount <= 0)
			return;

		int curDamage = ItemNBTHelper.getInt(stack, Lib.NBT_DAMAGE);
		curDamage += amount;

		if(player instanceof EntityPlayerMP)
			CriteriaTriggers.ITEM_DURABILITY_CHANGED.trigger((EntityPlayerMP)player, stack, curDamage);

		if(curDamage >= (stack.getMetadata()==HAMMER_META?hammerDurabiliy: cutterDurabiliy))
		{
			if(player!=null)
			{
				player.renderBrokenItemStack(stack);
				player.addStat(StatList.getObjectBreakStats(this));
			}
			stack.shrink(1);
			return;
		}
		ItemNBTHelper.setInt(stack, Lib.NBT_DAMAGE, curDamage);
	}

	@Override
	public boolean isDamageable()
	{
		return true;
	}

	@Override
	public int getMaxDamageIE(ItemStack stack)
	{
		if(stack.getMetadata()==HAMMER_META)
			return hammerDurabiliy;
		else if(stack.getMetadata()==CUTTER_META)
			return cutterDurabiliy;
		return 0;
	}

	@Override
	public int getItemDamageIE(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, Lib.NBT_DAMAGE);
	}

	@Override
	public boolean isEnchantable(@Nonnull ItemStack stack)
	{
		return stack.getMetadata()==HAMMER_META||stack.getMetadata()==CUTTER_META;
	}

	@Override
	public int getItemEnchantability()
	{
		return 14;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
	{
		if(stack.getMetadata()==HAMMER_META||stack.getMetadata()==CUTTER_META)
			return enchantment==Enchantments.EFFICIENCY||enchantment==Enchantments.UNBREAKING||enchantment==Enchantments.MENDING;
		return super.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public boolean onBlockStartBreak(ItemStack itemstack, BlockPos pos, EntityPlayer player)
	{
		if((itemstack.getMetadata()==HAMMER_META||itemstack.getMetadata()==CUTTER_META)&&player!=null&&!player.world.isRemote&&!player.capabilities.isCreativeMode)
		{
			IBlockState state = player.world.getBlockState(pos);
			boolean effective = false;
			for(String tool : getToolClasses(itemstack))
				if(state.getBlock().isToolEffective(tool, state))
				{
					effective = true;
					break;
				}
			this.damageIETool(itemstack, effective?1: 2, player.getRNG(), player);
		}
		return false;
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(stack.getMetadata()==HAMMER_META)
		{
			String[] permittedMultiblocks = null;
			String[] interdictedMultiblocks = null;
			if(ItemNBTHelper.hasKey(stack, "multiblockPermission"))
			{
				NBTTagList list = stack.getTagCompound().getTagList("multiblockPermission", 8);
				permittedMultiblocks = new String[list.tagCount()];
				for(int i = 0; i < permittedMultiblocks.length; i++)
					permittedMultiblocks[i] = list.getStringTagAt(i);
			}
			if(ItemNBTHelper.hasKey(stack, "multiblockInterdiction"))
			{
				NBTTagList list = stack.getTagCompound().getTagList("multiblockInterdiction", 8);
				interdictedMultiblocks = new String[list.tagCount()];
				for(int i = 0; i < interdictedMultiblocks.length; i++)
					interdictedMultiblocks[i] = list.getStringTagAt(i);
			}
			for(MultiblockHandler.IMultiblock mb : MultiblockHandler.getMultiblocks())
				if(mb.isBlockTrigger(world.getBlockState(pos)))
				{
					boolean b = permittedMultiblocks==null;
					if(permittedMultiblocks!=null)
						for(String s : permittedMultiblocks)
							if(mb.getUniqueName().equalsIgnoreCase(s))
							{
								b = true;
								continue;
							}
					if(!b)
						break;
					if(interdictedMultiblocks!=null)
						for(String s : interdictedMultiblocks)
							if(mb.getUniqueName().equalsIgnoreCase(s))
							{
								b = false;
								continue;
							}
					if(!b)
						break;
					if(MultiblockHandler.fireMultiblockFormationEventPre(player, mb, pos, stack).isCanceled())
						continue;
					if(mb.createStructure(world, pos, side, player))
					{
						if(player instanceof EntityPlayerMP)
							IEAdvancements.TRIGGER_MULTIBLOCK.trigger((EntityPlayerMP)player, mb, stack);
						return EnumActionResult.SUCCESS;
					}
				}
		}
		return EnumActionResult.PASS;
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		TileEntity tileEntity = world.getTileEntity(pos);
		if(stack.getMetadata()==HAMMER_META)
		{
			if(!(tileEntity instanceof IDirectionalTile)&&!(tileEntity instanceof IHammerInteraction)&&!(tileEntity instanceof IConfigurableSides))
				return RotationUtil.rotateBlock(world, pos, side)?EnumActionResult.SUCCESS: EnumActionResult.PASS;
		}
		else if(stack.getMetadata()==CUTTER_META&&tileEntity instanceof IImmersiveConnectable)
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
					int nbtDamage = ItemNBTHelper.getInt(stack, Lib.NBT_DAMAGE)+1;
					if(nbtDamage < cutterDurabiliy)
						ItemNBTHelper.setInt(stack, Lib.NBT_DAMAGE, nbtDamage);
					else
					{
						player.renderBrokenItemStack(stack);
						player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
					}
				}
			}
			return EnumActionResult.SUCCESS;
		}
		else if(stack.getMetadata()==VOLTMETER_META&&!world.isRemote)
		{
			if(!player.isSneaking()&&(tileEntity instanceof IFluxReceiver||tileEntity instanceof IFluxProvider))
			{
				int max = 0;
				int stored = 0;
				if(tileEntity instanceof IFluxReceiver)
				{
					max = ((IFluxReceiver)tileEntity).getMaxEnergyStored(side);
					stored = ((IFluxReceiver)tileEntity).getEnergyStored(side);
				}
				else
				{
					max = ((IFluxProvider)tileEntity).getMaxEnergyStored(side);
					stored = ((IFluxProvider)tileEntity).getEnergyStored(side);
				}
				if(max > 0)
					ChatUtils.sendServerNoSpamMessages(player, new TextComponentTranslation(Lib.CHAT_INFO+"energyStorage", stored, max));
				return EnumActionResult.SUCCESS;
			}
			if(player.isSneaking()&&tileEntity instanceof IImmersiveConnectable)
			{
				if(!ItemNBTHelper.hasKey(stack, "linkingPos"))
					ItemNBTHelper.setIntArray(stack, "linkingPos", new int[]{world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ()});
				else
				{
					int[] array = ItemNBTHelper.getIntArray(stack, "linkingPos");
					BlockPos linkPos = new BlockPos(array[1], array[2], array[3]);
					TileEntity tileEntityLinkingPos = world.getTileEntity(linkPos);
					if(array[0]==world.provider.getDimension())
					{
						IImmersiveConnectable nodeHere = (IImmersiveConnectable)tileEntity;
						IImmersiveConnectable nodeLink = (IImmersiveConnectable)tileEntityLinkingPos;
						if(nodeLink!=null)
						{
							Set<AbstractConnection> connections = ImmersiveNetHandler.INSTANCE.getIndirectEnergyConnections(Utils.toCC(nodeLink), world, true);
							for(AbstractConnection con : connections)
								if(Utils.toCC(nodeHere).equals(con.end))
									player.sendMessage(new TextComponentTranslation(Lib.CHAT_INFO+"averageLoss", Utils.formatDouble(con.getAverageLossRate()*100, "###.000")));
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
		return !player.world.isRemote&&stack.getMetadata()==HAMMER_META&&RotationUtil.rotateEntity(entity, player);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player)
	{
		return stack.getMetadata()==HAMMER_META;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(stack.getMetadata()==MANUAL_META)
		{
			if(world.isRemote)
				CommonProxy.openGuiForItem(player, hand==EnumHand.MAIN_HAND?EntityEquipmentSlot.MAINHAND: EntityEquipmentSlot.OFFHAND);
			return new ActionResult<>(EnumActionResult.SUCCESS, stack);
		}
		else if(stack.getMetadata()==CUTTER_META)
		{
			if(!world.isRemote)
			{
				double reachDistance = player.getAttributeMap().getAttributeInstance(EntityPlayer.REACH_DISTANCE).getAttributeValue();
				Connection target = ApiUtils.getTargetConnection(world, player, null, reachDistance);
				if(target!=null)
					ImmersiveNetHandler.INSTANCE.removeConnectionAndDrop(target, world, player.getPosition());
			}
			return new ActionResult<>(EnumActionResult.SUCCESS, stack);
		}
		return new ActionResult<>(EnumActionResult.PASS, stack);
	}

	@Override
	public int getHarvestLevel(ItemStack stack, @Nonnull String toolClass, @Nullable EntityPlayer player, @Nullable IBlockState blockState)
	{
		if(getToolClasses(stack).contains(toolClass))
			return 2;
		else
			return -1;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, Lib.NBT_DAMAGE) > 0;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		double max = getMaxDamageIE(stack);
		return ItemNBTHelper.getInt(stack, Lib.NBT_DAMAGE)/max;
	}

	@Nonnull
	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		int meta = stack.getMetadata();
		return meta==HAMMER_META?ImmutableSet.of(Lib.TOOL_HAMMER): meta==CUTTER_META?ImmutableSet.of(Lib.TOOL_WIRECUTTER): new HashSet<String>();
	}

	@Override
	public float getDestroySpeed(ItemStack stack, IBlockState state)
	{
		for(String type : this.getToolClasses(stack))
			if(state.getBlock().isToolEffective(type, state))
				return 6;
		return super.getDestroySpeed(stack, state);
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return item.getMetadata()!=MANUAL_META;
	}

	@Override
	public boolean canHarvestBlock(@Nonnull IBlockState state, ItemStack stack)
	{
		if(stack.getMetadata()==HAMMER_META)
		{
			if(state.getBlock() instanceof BlockIEBase)
			{
				if(((BlockIEBase)state.getBlock()).allowHammerHarvest(state))
					return true;
			}
			else if(state.getBlock().isToolEffective(Lib.TOOL_HAMMER, state))
				return true;
		}
		else if(stack.getMetadata()==CUTTER_META)
		{
			if(state.getBlock() instanceof BlockIEBase)
			{
				if(((BlockIEBase)state.getBlock()).allowWirecutterHarvest(state))
					return true;
			}
			else if(state.getBlock().isToolEffective(Lib.TOOL_WIRECUTTER, state))
				return true;
		}
		return super.canHarvestBlock(state, stack);
	}
}