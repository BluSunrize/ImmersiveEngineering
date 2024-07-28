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
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.api.utils.codec.DualCodec;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.client.utils.FontUtils;
import blusunrize.immersiveengineering.common.entities.FluorescentTubeEntity;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FluorescentTubeItem extends IEBaseItem implements IConfigurableTool, IElectricEquipment, IColouredItem
{

	public FluorescentTubeItem()
	{
		super(new Properties().stacksTo(1));
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
				if(!player.getAbilities().instabuild)
					stack.shrink(1);
			}
			return InteractionResult.SUCCESS;
		}
		return super.useOn(ctx);
	}

	public static Color4 getRGB(ItemStack s)
	{
		return s.getOrDefault(IEDataComponents.COLOR, Color4.WHITE);
	}

	public static void setRGB(ItemStack s, Color4 color)
	{
		s.set(IEDataComponents.COLOR, color);
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
		var rgb = getRGB(stack);
		ret[0] = new ToolConfigFloat("red", 60, 20, rgb.r());
		ret[1] = new ToolConfigFloat("green", 60, 40, rgb.g());
		ret[2] = new ToolConfigFloat("blue", 60, 60, rgb.b());
		return ret;
	}

	@Override
	public void applyConfigOption(ItemStack stack, String key, Object value)
	{
		var rgb = getRGB(stack);
		switch(key)
		{
			case "red" -> rgb = new Color4((float)value, rgb.g(), rgb.b(), rgb.a());
			case "green" -> rgb = new Color4(rgb.r(), (float)value, rgb.b(), rgb.a());
			case "blue" -> rgb = new Color4(rgb.r(), rgb.g(), (float)value, rgb.a());
		}
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

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> list, TooltipFlag flag)
	{
		int color = getRGBInt(stack, 1);
		list.add(FontUtils.withAppendColoredColour(
				Component.translatable(Lib.DESC_INFO+"colour").withStyle(ChatFormatting.GRAY),
				color
		));
	}

	@Override
	public int getColourForIEItem(ItemStack stack, int pass)
	{
		if(pass==0)
			return getRGBInt(stack, 1);
		else
			return -1;
	}

	public static float[] getRGBFloat(ItemStack stack, float factor)
	{
		var fRGB = getRGB(stack);
		return new float[]{fRGB.r()*factor, fRGB.g()*factor, fRGB.b()*factor, 1};
	}

	public static int getRGBInt(ItemStack stack, float factor)
	{
		float[] scaled = getRGBFloat(stack, factor);
		return (((int)(scaled[0]*255)<<16)+((int)(scaled[1]*255)<<8)+(int)(scaled[2]*255));
	}

	public static boolean isLit(ItemStack stack)
	{
		return stack.has(IEDataComponents.FLUORESCENT_TUBE_LIT);
	}

	public static void setLit(ItemStack stack, float strength)
	{
		stack.set(IEDataComponents.FLUORESCENT_TUBE_LIT, new LitState(Mth.clamp(strength, 0, 1F), 35));
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
		var state = stack.get(IEDataComponents.FLUORESCENT_TUBE_LIT);
		if(!worldIn.isClientSide&&state!=null)
		{
			if(state.time > 1)
				stack.set(IEDataComponents.FLUORESCENT_TUBE_LIT, new LitState(state.strength, state.time-1));
			else
				stack.remove(IEDataComponents.FLUORESCENT_TUBE_LIT);
		}
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged)
	{
		return !ItemStack.isSameItem(oldStack, newStack)||!Objects.equals(getRGB(oldStack), getRGB(newStack));
	}

	public record LitState(float strength, int time)
	{
		public static final DualCodec<ByteBuf, LitState> CODECS = DualCodecs.composite(
				DualCodecs.FLOAT.fieldOf("strength"), LitState::strength,
				DualCodecs.INT.fieldOf("time"), LitState::time,
				LitState::new
		);
	}
}
