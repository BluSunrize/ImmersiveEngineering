/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.Tags.Items;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class DrillheadItem extends IEBaseItem implements IDrillHead
{
	//Maximal damage is slightly proportionate to pickaxes
	public static final DrillHeadPerm STEEL = new DrillHeadPerm("steel", IETags.getTagsFor(EnumMetals.STEEL).ingot, 3, 1, Tiers.DIAMOND, 10, 7, 10000, ImmersiveEngineering.rl("item/drill_diesel"));
	public static final DrillHeadPerm IRON = new DrillHeadPerm("iron", Items.INGOTS_IRON, 2, 1, Tiers.IRON, 9, 6, 6000, ImmersiveEngineering.rl("item/drill_iron"));

	public static final String DAMAGE_KEY_OLD = "headDamage";
	public static final String DAMAGE_KEY = "Damage";

	public DrillHeadPerm perms;

	public DrillheadItem(DrillHeadPerm perms)
	{
		super(new Properties().stacksTo(1));
		this.perms = perms;
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		list.add(Component.translatable(Lib.DESC_FLAVOUR+"drillhead.size", perms.drillSize, perms.drillDepth));
		list.add(Component.translatable(Lib.DESC_FLAVOUR+"drillhead.level", Utils.getHarvestLevelName(getMiningLevel(stack))));
		list.add(Component.translatable(Lib.DESC_FLAVOUR+"drillhead.speed", Utils.formatDouble(getMiningSpeed(stack), "0.###")));
		list.add(Component.translatable(Lib.DESC_FLAVOUR+"drillhead.damage", Utils.formatDouble(getAttackDamage(stack), "0.###")));

		int maxDmg = getMaximumHeadDamage(stack);
		int dmg = maxDmg-getHeadDamage(stack);
		float quote = dmg/(float)maxDmg;
		String status = ""+(quote < .1?ChatFormatting.RED: quote < .3?ChatFormatting.GOLD: quote < .6?ChatFormatting.YELLOW: ChatFormatting.GREEN);
		String s = status+(getMaximumHeadDamage(stack)-getHeadDamage(stack))+"/"+getMaximumHeadDamage(stack);
		list.add(Component.translatable(Lib.DESC_INFO+"durability", s));
	}

	@Override
	public boolean isValidRepairItem(ItemStack stack, ItemStack material)
	{
		return material.is(perms.repairMaterial);
	}

	@Override
	public boolean beforeBlockbreak(ItemStack drill, ItemStack head, Player player)
	{
		return false;
	}

	@Override
	public void afterBlockbreak(ItemStack drill, ItemStack head, Player player)
	{
	}

	@Override
	public Tier getMiningLevel(ItemStack head)
	{
		return perms.drillLevel;
	}

	@Override
	public float getMiningSpeed(ItemStack head)
	{
		return perms.drillSpeed;
	}

	@Override
	public float getAttackDamage(ItemStack head)
	{
		return perms.drillAttack;
	}

	@Override
	public int getHeadDamage(ItemStack head)
	{
		if(head.hasTag())
		{
			CompoundTag nbt = head.getOrCreateTag();
			if(nbt.contains(DAMAGE_KEY_OLD, Tag.TAG_INT))
				return nbt.getInt(DAMAGE_KEY_OLD);
			else
				return nbt.getInt(DAMAGE_KEY);
		}
		else
			return 0;
	}

	@Override
	public int getMaximumHeadDamage(ItemStack head)
	{
		return perms.maxDamage;
	}

	@Override
	public void damageHead(ItemStack head, int dmg)
	{
		setHeadDamage(head, getHeadDamage(head)+dmg);
	}

	public static void setHeadDamage(ItemStack head, int totalDamage)
	{
		CompoundTag nbt = head.getOrCreateTag();
		nbt.remove(DAMAGE_KEY_OLD);
		nbt.putInt(DAMAGE_KEY, totalDamage);
	}

	@Override
	public ResourceLocation getDrillTexture(ItemStack drill, ItemStack head)
	{
		return perms.texture;
	}

	@Override
	public int getBarWidth(@Nonnull ItemStack stack)
	{
		return Math.round(MAX_BAR_WIDTH*(1-getHeadDamage(stack)/(float)getMaximumHeadDamage(stack)));
	}

	@Override
	public boolean isBarVisible(@Nonnull ItemStack stack)
	{
		return getHeadDamage(stack) > 0;
	}

	public static class DrillHeadPerm
	{
		final String name;
		final TagKey<Item> repairMaterial;
		final int drillSize;
		final int drillDepth;
		final Tier drillLevel;
		final float drillSpeed;
		final float drillAttack;
		final int maxDamage;
		public final ResourceLocation texture;

		public DrillHeadPerm(String name, TagKey<Item> repairMaterial, int drillSize, int drillDepth, Tier drillLevel, float drillSpeed, int drillAttack, int maxDamage, ResourceLocation texture)
		{
			this.name = name;
			this.repairMaterial = repairMaterial;
			this.drillSize = drillSize;
			this.drillDepth = drillDepth;
			this.drillLevel = drillLevel;
			this.drillSpeed = drillSpeed;
			this.drillAttack = drillAttack;
			this.maxDamage = maxDamage;
			this.texture = texture;
		}
	}

	@Override
	public ImmutableList<BlockPos> getExtraBlocksDug(ItemStack head, Level world, Player player, HitResult rtr)
	{
		if(!(rtr instanceof BlockHitResult brtr))
			return ImmutableList.of();
		Direction side = brtr.getDirection();
		int diameter = perms.drillSize;
		int depth = perms.drillDepth;

		BlockPos startPos = brtr.getBlockPos();
		BlockState state = world.getBlockState(startPos);
		float maxHardness = 1;
		if(!state.isAir())
			maxHardness = state.getDestroyProgress(player, world, startPos)*0.4F;
		if(maxHardness < 0)
			maxHardness = 0;

		if(diameter%2==0)//even numbers
		{
			float hx = (float)brtr.getLocation().x-brtr.getBlockPos().getX();
			float hy = (float)brtr.getLocation().y-brtr.getBlockPos().getY();
			float hz = (float)brtr.getLocation().z-brtr.getBlockPos().getZ();
			if((side.getAxis()==Axis.Y&&hx < .5)||(side.getAxis()==Axis.Z&&hx < .5))
				startPos = startPos.offset(-diameter/2, 0, 0);
			if(side.getAxis()!=Axis.Y&&hy < .5)
				startPos = startPos.offset(0, -diameter/2, 0);
			if((side.getAxis()==Axis.Y&&hz < .5)||(side.getAxis()==Axis.X&&hz < .5))
				startPos = startPos.offset(0, 0, -diameter/2);
		}
		else//odd numbers
			startPos = startPos.offset(-(side.getAxis()==Axis.X?0: diameter/2), -(side.getAxis()==Axis.Y?0: diameter/2), -(side.getAxis()==Axis.Z?0: diameter/2));
		Builder<BlockPos> b = ImmutableList.builder();
		for(int dd = 0; dd < depth; dd++)
			for(int dw = 0; dw < diameter; dw++)
				for(int dh = 0; dh < diameter; dh++)
				{
					BlockPos pos = startPos.offset((side.getAxis()==Axis.X?dd: dw), (side.getAxis()==Axis.Y?dd: dh), (side.getAxis()==Axis.Y?dh: side.getAxis()==Axis.X?dw: dd));
					if(pos.equals(brtr.getBlockPos()))
						continue;
					state = world.getBlockState(pos);
					if(state.isAir())
						continue;
					Block block = state.getBlock();
					float h = state.getDestroyProgress(player, world, pos);
					boolean canHarvest = block.canHarvestBlock(world.getBlockState(pos), world, pos, player);
					boolean drillMat = Tools.DRILL.get().isEffective(ItemStack.EMPTY, state);
					boolean hardness = h >= maxHardness;
					if(canHarvest&&drillMat&&hardness)
						b.add(pos);
				}
		return b.build();
	}
}