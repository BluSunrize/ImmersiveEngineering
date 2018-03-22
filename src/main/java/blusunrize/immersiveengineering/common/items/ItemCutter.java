package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.tool.ITool;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.IESaveData;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class ItemCutter extends ItemIEBase implements ITool
{
	public final static String NAME = "wirecutter";

	public ItemCutter()
	{
		super(NAME, 1);
	}

	@Nonnull
	@Override
	public ItemStack getContainerItem(@Nonnull ItemStack stack)
	{
		int nbtDamage = ItemNBTHelper.getInt(stack, "cutterDmg")+1;
		if(nbtDamage< IEConfig.Tools.cutterDurabiliy)
		{
			ItemStack container = stack.copy();
			ItemNBTHelper.setInt(container, "cutterDmg", nbtDamage);
			return container;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagIn)
	{
		if (flagIn.isAdvanced())
		{
			int nbtDamage = ItemNBTHelper.getInt(stack, "cutterDmg");
			int maxDamage = IEConfig.Tools.cutterDurabiliy;
			tooltip.add("Durability: " + (maxDamage - nbtDamage) + " / " + maxDamage);
		}
	}

	@Nonnull
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side,
									  float hitX, float hitY, float hitZ)
	{
		TileEntity tileEntity = world.getTileEntity(pos);
		ItemStack stack = player.getHeldItem(hand);
		if(tileEntity instanceof IImmersiveConnectable)
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
					if(nbtDamage < IEConfig.Tools.cutterDurabiliy)
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
		return EnumActionResult.PASS;
	}

	@Nonnull
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @Nonnull EnumHand hand)
	{
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote)
		{
			double reachDistance = player.getAttributeMap().getAttributeInstance(EntityPlayer.REACH_DISTANCE).getAttributeValue();
			ImmersiveNetHandler.Connection target = ApiUtils.getTargetConnection(world, player, null, reachDistance);
			if (target != null)
				ImmersiveNetHandler.INSTANCE.removeConnectionAndDrop(target, world, player.getPosition());
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, stack);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return (ItemNBTHelper.getInt(stack, "cutterDmg")>0);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "cutterDmg") / (double) IEConfig.Tools.cutterDurabiliy;
	}

	@Override
	public Set<String> getToolClasses(ItemStack stack)
	{
		return ImmutableSet.of(Lib.TOOL_WIRECUTTER);
	}

	@Override
	public boolean isTool(ItemStack item)
	{
		return true;
	}
}
