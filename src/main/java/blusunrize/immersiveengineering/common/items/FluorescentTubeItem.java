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
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
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
		super("fluorescent_tube", new Properties().stacksTo(1).setISTER(() -> () -> IEOBJItemRenderer.INSTANCE));
	}

	@Override
	public InteractionResult useOn(UseOnContext ctx)
	{
		Direction side = ctx.getClickedFace();
		Level world = ctx.getLevel();
		Player player = ctx.getPlayer();
		if(side==Direction.UP&&player!=null)
		{
			if(!world.isClientSide)
			{
				ItemStack stack = ctx.getItemInHand();
				Vec3 look = player.getLookAngle();
				float angle = (float)Math.toDegrees(Math.atan2(look.x, look.z));
				FluorescentTubeEntity tube = new FluorescentTubeEntity(world, stack.copy(), angle);
				tube.setPos(ctx.getClickLocation().x, ctx.getClickLocation().y+1.5, ctx.getClickLocation().z);
				world.addFreshEntity(tube);
				stack.split(1);
				if(stack.getCount() > 0)
					player.setItemInHand(ctx.getHand(), stack);
				else
					player.setItemInHand(ctx.getHand(), ItemStack.EMPTY);
			}
			return InteractionResult.SUCCESS;
		}
		return super.useOn(ctx);
	}

	public static float[] getRGB(ItemStack s)
	{
		if(ItemNBTHelper.hasKey(s, "rgb"))
		{
			CompoundTag nbt = ItemNBTHelper.getTagCompound(s, "rgb");
			return new float[]{nbt.getFloat("r"), nbt.getFloat("g"), nbt.getFloat("b")};
		}
		return new float[]{1, 1, 1};
	}

	public static void setRGB(ItemStack s, float[] rgb)
	{
		CompoundTag nbt = new CompoundTag();
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
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		int color = getRGBInt(stack, 1);
		list.add(FontUtils.withAppendColoredColour(
				new TranslatableComponent(Lib.DESC_INFO+"colour"),
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
		ItemNBTHelper.putFloat(stack, LIT_STRENGTH, Mth.clamp(strength, 0, 1F));
	}

	@Override
	public void onStrike(ItemStack equipped, EquipmentSlot eqSlot, LivingEntity owner, Map<String, Object> cache, DamageSource dmg,
						 ElectricSource eSource)
	{
		setLit(equipped, eSource.level);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
		if(!worldIn.isClientSide&&isLit(stack))
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
		return !ItemStack.isSame(oldStack, newStack)||!Arrays.equals(getRGB(oldStack), getRGB(newStack));
	}

	private static final String[][] special = {{"tube"}};

	@Override
	public String[][] getSpecialGroups(ItemStack stack, ItemTransforms.TransformType transform, LivingEntity entity)
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
			float mult = min+(lit?Utils.RAND.nextFloat()*Mth.clamp(1-min, 0, .1F): 0);
			float[] colors = getRGBFloat(object, mult);
			return new Vector4f(colors[0], colors[1], colors[2], colors[3]);
		}
		else
			return new Vector4f(.067f, .067f, .067f, 1);
	}
}
