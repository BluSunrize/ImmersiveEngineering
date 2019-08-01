/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.IDrillHead;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.Tag;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags.Items;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ItemDrillhead extends ItemIEBase implements IDrillHead
{
	//Maximal damage is slightly proportionate to pickaxes
	public static final DrillHeadPerm STEEL = new DrillHeadPerm("steel", "ingotSteel", 3, 1, 3, 10, 7, 10000, "immersiveengineering:items/drill_diesel");
	public static final DrillHeadPerm IRON = new DrillHeadPerm("iron", Items.INGOTS_IRON, 2, 1, 2, 9, 6, 6000, "immersiveengineering:items/drill_iron");

	public DrillHeadPerm perms;

	public ItemDrillhead(DrillHeadPerm perms)
	{
		super("drillhead_"+perms.name, new Properties().maxStackSize(1));
		this.perms = perms;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
	{
		list.add(new TranslationTextComponent(Lib.DESC_FLAVOUR+"drillhead.size", perms.drillSize, perms.drillDepth));
		list.add(new TranslationTextComponent(Lib.DESC_FLAVOUR+"drillhead.level", Utils.getHarvestLevelName(getMiningLevel(stack))));
		list.add(new TranslationTextComponent(Lib.DESC_FLAVOUR+"drillhead.speed", Utils.formatDouble(getMiningSpeed(stack), "0.###")));
		list.add(new TranslationTextComponent(Lib.DESC_FLAVOUR+"drillhead.damage", Utils.formatDouble(getAttackDamage(stack), "0.###")));

		int maxDmg = getMaximumHeadDamage(stack);
		int dmg = maxDmg-getHeadDamage(stack);
		float quote = dmg/(float)maxDmg;
		String status = ""+(quote < .1?TextFormatting.RED: quote < .3?TextFormatting.GOLD: quote < .6?TextFormatting.YELLOW: TextFormatting.GREEN);
		String s = status+(getMaximumHeadDamage(stack)-getHeadDamage(stack))+"/"+getMaximumHeadDamage(stack);
		list.add(new TranslationTextComponent(Lib.DESC_INFO+"durability", s));
	}

	@Override
	public boolean getIsRepairable(ItemStack stack, ItemStack material)
	{
		return perms.repairMaterial.contains(material.getItem());
	}

	@Override
	public boolean beforeBlockbreak(ItemStack drill, ItemStack head, PlayerEntity player)
	{
		return false;
	}

	@Override
	public void afterBlockbreak(ItemStack drill, ItemStack head, PlayerEntity player)
	{
	}

	@Override
	public int getMiningLevel(ItemStack head)
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
		return ItemNBTHelper.getInt(head, "headDamage");
	}

	@Override
	public int getMaximumHeadDamage(ItemStack head)
	{
		return perms.maxDamage;
	}

	@Override
	public void damageHead(ItemStack head, int dmg)
	{
		ItemNBTHelper.putInt(head, "headDamage", ItemNBTHelper.getInt(head, "headDamage")+dmg);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public TextureAtlasSprite getDrillTexture(ItemStack drill, ItemStack head)
	{
		return perms.sprite;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack)
	{
		return (double)ItemNBTHelper.getInt(stack, "headDamage")/(double)getMaximumHeadDamage(stack);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return ItemNBTHelper.getInt(stack, "headDamage") > 0;
	}

	public static class DrillHeadPerm
	{
		public static final Set<DrillHeadPerm> ALL_PERMS = new HashSet<>();

		final String name;
		final Tag<Item> repairMaterial;
		final int drillSize;
		final int drillDepth;
		final int drillLevel;
		final float drillSpeed;
		final float drillAttack;
		final int maxDamage;
		public final String texture;
		@OnlyIn(Dist.CLIENT)
		public TextureAtlasSprite sprite;

		public DrillHeadPerm(String name, Tag<Item> repairMaterial, int drillSize, int drillDepth, int drillLevel, float drillSpeed, int drillAttack, int maxDamage, String texture)
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

			ALL_PERMS.add(this);
		}
	}

	@Override
	public ImmutableList<BlockPos> getExtraBlocksDug(ItemStack head, World world, PlayerEntity player, RayTraceResult mop)
	{
		Direction side = mop.sideHit;
		int diameter = perms.drillSize;
		int depth = perms.drillDepth;

		BlockPos startPos = mop.getBlockPos();
		BlockState state = world.getBlockState(startPos);
		Block block = state.getBlock();
		float maxHardness = 1;
		if(block!=null&&!block.isAir(state, world, startPos))
			maxHardness = state.getPlayerRelativeBlockHardness(player, world, startPos)*0.6F;
		if(maxHardness < 0)
			maxHardness = 0;

		if(diameter%2==0)//even numbers
		{
			float hx = (float)mop.hitVec.x-mop.getBlockPos().getX();
			float hy = (float)mop.hitVec.y-mop.getBlockPos().getY();
			float hz = (float)mop.hitVec.z-mop.getBlockPos().getZ();
			if((side.getAxis()==Axis.Y&&hx < .5)||(side.getAxis()==Axis.Z&&hx < .5))
				startPos = startPos.add(-diameter/2, 0, 0);
			if(side.getAxis()!=Axis.Y&&hy < .5)
				startPos = startPos.add(0, -diameter/2, 0);
			if((side.getAxis()==Axis.Y&&hz < .5)||(side.getAxis()==Axis.X&&hz < .5))
				startPos = startPos.add(0, 0, -diameter/2);
		}
		else//odd numbers
		{
			startPos = startPos.add(-(side.getAxis()==Axis.X?0: diameter/2), -(side.getAxis()==Axis.Y?0: diameter/2), -(side.getAxis()==Axis.Z?0: diameter/2));
		}
		Builder<BlockPos> b = ImmutableList.builder();
		for(int dd = 0; dd < depth; dd++)
			for(int dw = 0; dw < diameter; dw++)
				for(int dh = 0; dh < diameter; dh++)
				{
					BlockPos pos = startPos.add((side.getAxis()==Axis.X?dd: dw), (side.getAxis()==Axis.Y?dd: dh), (side.getAxis()==Axis.Y?dh: side.getAxis()==Axis.X?dw: dd));
					if(pos.equals(mop.getBlockPos()))
						continue;
					state = world.getBlockState(pos);
					block = state.getBlock();
					float h = state.getPlayerRelativeBlockHardness(player, world, pos);
					boolean canHarvest = block.canHarvestBlock(world.getBlockState(pos), world, pos, player);
					boolean drillMat = ((ItemDrill)Tools.drill).isEffective(state.getMaterial());
					boolean hardness = h > maxHardness;
					if(canHarvest&&drillMat&&hardness)
						b.add(pos);
				}
		return b.build();
	}
}