/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigBoolean;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigFloat;
import blusunrize.immersiveengineering.api.tool.ITeslaEquipment;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.client.render.ItemRenderFluorescentTube;
import blusunrize.immersiveengineering.common.entities.EntityFluorescentTube;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ItemFluorescentTube extends ItemIEBase implements IConfigurableTool, ITeslaEquipment
{

	public ItemFluorescentTube()
	{
		super("fluorescent_tube", 1);
		//TODO this is SideOnly(CLIENT)
		setTileEntityItemStackRenderer(new ItemRenderFluorescentTube());
	}
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(side==EnumFacing.UP)
		{
			if(!world.isRemote)
			{
				ItemStack stack = player.getHeldItem(hand);
				Vec3d look = player.getLookVec();
				float angle = (float) Math.toDegrees(Math.atan2(look.x, look.z));
				EntityFluorescentTube tube = new EntityFluorescentTube(world, stack.copy(), angle);
				tube.setPosition(pos.getX()+hitX, pos.getY()+1.5, pos.getZ()+hitZ);
				world.spawnEntity(tube);
				stack.splitStack(1);
				if (stack.getCount()>0)
					player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, stack);
				else
					player.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
			}
			return EnumActionResult.SUCCESS;
		}
		return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
	}
	public static float[] getRGB(ItemStack s)
	{
		if (ItemNBTHelper.hasKey(s, "rgb"))
		{
			NBTTagCompound nbt = ItemNBTHelper.getTagCompound(s, "rgb");
			return new float[]{nbt.getFloat("r"), nbt.getFloat("g"), nbt.getFloat("b")};
		}
		return new float[]{1, 1, 1};
	}
	public static void setRGB(ItemStack s, float[] rgb)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setFloat("r", rgb[0]);
		nbt.setFloat("g", rgb[1]);
		nbt.setFloat("b", rgb[2]);
		ItemNBTHelper.setTagCompound(s, "rgb", nbt);
	}
	@Override
	public boolean canConfigure(ItemStack stack)
	{
		return true;
	}
	@Override
	public ToolConfigBoolean[] getBooleanOptions(ItemStack stack)
	{
		return new ToolConfigBoolean[0];
	}
	@Override
	public ToolConfigFloat[] getFloatOptions(ItemStack stack)
	{
		ToolConfigFloat[] ret = new ToolConfigFloat[3];
		float[] rgb = getRGB(stack);
		ret[0] = new ToolConfigFloat("red", 60, 20, rgb[0]);
		ret[1] = new ToolConfigFloat("green", 60, 40, rgb[1]);
		ret[2] = new ToolConfigFloat("blue", 60, 60, rgb[2]);
		return ret;
	}
	@Override
	public void applyConfigOption(ItemStack stack, String key, Object value)
	{
		int id = key.equals("red")?0:(key.equals("green")?1:2);
		float[] rgb = getRGB(stack);
		rgb[id] = (float) value;
		setRGB(stack, rgb);
	}
	@Override
	public String fomatConfigName(ItemStack stack, ToolConfig config)
	{
		return config.name;
	}
	@Override
	public String fomatConfigDescription(ItemStack stack, ToolConfig config)
	{
		return config.name;
	}
	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		list.add(I18n.format(Lib.DESC_INFO+"colour", "#"+hexColorString(stack)));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack)
	{
		return ClientProxy.itemFont;
	}
	@Override
	public boolean hasCustomItemColours()
	{
		return true;
	}
	@Override
	public int getColourForIEItem(ItemStack stack, int pass)
	{
		if(pass==0)
			return getRGBInt(stack);
		return super.getColourForIEItem(stack, pass);
	}
	public static int getRGBInt(ItemStack stack)
	{
		float[] fRGB = getRGB(stack);
		return (((int)(fRGB[0]*255)<<16)+((int)(fRGB[1]*255)<<8)+(int)(fRGB[2]*255));
	}
	public static String hexColorString(ItemStack stack)
	{
		String hexCol = Integer.toHexString(getRGBInt(stack));
		while (hexCol.length()<6)
			hexCol = "0"+hexCol;
		return hexCol;
	}

	private static final String IS_LIT = "isLit";

	public static boolean isLit(ItemStack stack)
	{
		return ItemNBTHelper.hasKey(stack, IS_LIT);
	}

	public static void setLit(ItemStack stack)
	{
		ItemNBTHelper.setInt(stack, IS_LIT, 35);
	}

	@Override
	public void onStrike(ItemStack s, EntityEquipmentSlot eqSlot, EntityLivingBase p, Map<String, Object> cache, DamageSource dmg)
	{
		setLit(s);
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
		int litTicksRemaining = ItemNBTHelper.getInt(stack, IS_LIT);
		litTicksRemaining--;
		if (litTicksRemaining<=0)
			ItemNBTHelper.remove(stack, IS_LIT);
		else
			ItemNBTHelper.setInt(stack, IS_LIT, litTicksRemaining);
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged)
	{
		return !ItemStack.areItemsEqual(oldStack, newStack)||!Arrays.equals(getRGB(oldStack), getRGB(newStack));
	}
}
