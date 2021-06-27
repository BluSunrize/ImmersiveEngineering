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
import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.client.utils.FontUtils;
import blusunrize.immersiveengineering.common.entities.FluorescentTubeEntity;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class FluorescentTubeItem extends IEBaseItem implements IConfigurableTool, IElectricEquipment,
		IOBJModelCallback<ItemStack>
{

	public FluorescentTubeItem()
	{
		super(new Properties().maxStackSize(1).setISTER(() -> () -> IEOBJItemRenderer.INSTANCE));
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext ctx)
	{
		Direction side = ctx.getFace();
		World world = ctx.getWorld();
		PlayerEntity player = ctx.getPlayer();
		if(side==Direction.UP&&player!=null)
		{
			if(!world.isRemote)
			{
				ItemStack stack = ctx.getItem();
				Vector3d look = player.getLookVec();
				float angle = (float)Math.toDegrees(Math.atan2(look.x, look.z));
				FluorescentTubeEntity tube = new FluorescentTubeEntity(world, stack.copy(), angle);
				tube.setPosition(ctx.getHitVec().x, ctx.getHitVec().y+1.5, ctx.getHitVec().z);
				world.addEntity(tube);
				stack.split(1);
				if(stack.getCount() > 0)
					player.setHeldItem(ctx.getHand(), stack);
				else
					player.setHeldItem(ctx.getHand(), ItemStack.EMPTY);
			}
			return ActionResultType.SUCCESS;
		}
		return super.onItemUse(ctx);
	}

	public static float[] getRGB(ItemStack s)
	{
		if(ItemNBTHelper.hasKey(s, "rgb"))
		{
			CompoundNBT nbt = ItemNBTHelper.getTagCompound(s, "rgb");
			return new float[]{nbt.getFloat("r"), nbt.getFloat("g"), nbt.getFloat("b")};
		}
		return new float[]{1, 1, 1};
	}

	public static void setRGB(ItemStack s, float[] rgb)
	{
		CompoundNBT nbt = new CompoundNBT();
		nbt.putFloat("r", rgb[0]);
		nbt.putFloat("g", rgb[1]);
		nbt.putFloat("b", rgb[2]);
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
		int id = key.equals("red")?0: (key.equals("green")?1: 2);
		float[] rgb = getRGB(stack);
		rgb[id] = (float)value;
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

	@OnlyIn(Dist.CLIENT)
	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		int color = getRGBInt(stack, 1);
		list.add(FontUtils.withAppendColoredColour(
				new TranslationTextComponent(Lib.DESC_INFO+"colour"),
				color
		));
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
			return getRGBInt(stack, 1);
		return super.getColourForIEItem(stack, pass);
	}

	public static float[] getRGBFloat(ItemStack stack, float factor)
	{
		float[] fRGB = getRGB(stack);
		return new float[]{fRGB[0]*factor, fRGB[1]*factor, fRGB[2]*factor, 1};
	}

	public static int getRGBInt(ItemStack stack, float factor)
	{
		float[] scaled = getRGBFloat(stack, factor);
		return (((int)(scaled[0]*255)<<16)+((int)(scaled[1]*255)<<8)+(int)(scaled[2]*255));
	}

	private static final String LIT_TIME = "litTime";
	private static final String LIT_STRENGTH = "litStrength";

	public static boolean isLit(ItemStack stack)
	{
		return ItemNBTHelper.hasKey(stack, LIT_TIME);
	}

	public static void setLit(ItemStack stack, float strength)
	{
		ItemNBTHelper.putInt(stack, LIT_TIME, 35);
		ItemNBTHelper.putFloat(stack, LIT_STRENGTH, MathHelper.clamp(strength, 0, 1F));
	}

	@Override
	public void onStrike(ItemStack equipped, EquipmentSlotType eqSlot, LivingEntity owner, Map<String, Object> cache, DamageSource dmg,
						 ElectricSource eSource)
	{
		setLit(equipped, eSource.level);
	}

	@Override
	public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if(!worldIn.isRemote&&isLit(stack))
		{
			int litTicksRemaining = ItemNBTHelper.getInt(stack, LIT_TIME);
			litTicksRemaining--;
			if(litTicksRemaining <= 0)
			{
				ItemNBTHelper.remove(stack, LIT_TIME);
				ItemNBTHelper.remove(stack, LIT_STRENGTH);
			}
			else
				ItemNBTHelper.putInt(stack, LIT_TIME, litTicksRemaining);
		}
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged)
	{
		return !ItemStack.areItemsEqual(oldStack, newStack)||!Arrays.equals(getRGB(oldStack), getRGB(newStack));
	}

	private static final String[][] special = {{"tube"}};

	@Override
	public String[][] getSpecialGroups(ItemStack stack, ItemCameraTransforms.TransformType transform, LivingEntity entity)
	{
		if(isLit(stack))
			return special;
		return IOBJModelCallback.EMPTY_STRING_A;
	}

	@Override
	public boolean areGroupsFullbright(ItemStack stack, String[] groups)
	{
		return groups.length==1&&"tube".equals(groups[0])&&isLit(stack);
	}

	@Override
	public Vector4f getRenderColor(ItemStack object, String group, Vector4f original)
	{
		if("tube".equals(group))
		{
			boolean lit = isLit(object);
			float min = .3F+(lit?ItemNBTHelper.getFloat(object, LIT_STRENGTH)*.68F: 0);
			float mult = min+(lit?Utils.RAND.nextFloat()*MathHelper.clamp(1-min, 0, .1F): 0);
			float[] colors = getRGBFloat(object, mult);
			return new Vector4f(colors[0], colors[1], colors[2], colors[3]);
		}
		else
			return new Vector4f(.067f, .067f, .067f, 1);
	}
}
