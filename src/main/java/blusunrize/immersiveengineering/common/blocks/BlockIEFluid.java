/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;

/**
 * @author BluSunrize - 04.08.2016
 */
public class BlockIEFluid extends BlockFluidClassic
{
	private int flammability = 0;
	private int fireSpread = 0;
	private EffectInstance[] potionEffects;

	public BlockIEFluid(String name, Fluid fluid, Material material)
	{
		super(fluid, material);
		this.setTranslationKey(ImmersiveEngineering.MODID+"."+name);
		this.setCreativeTab(ImmersiveEngineering.itemGroup);
		IEContent.registeredIEBlocks.add(this);
	}

	public BlockIEFluid setFlammability(int flammability, int fireSpread)
	{
		this.flammability = flammability;
		this.fireSpread = fireSpread;
		return this;
	}

	public BlockIEFluid setPotionEffects(EffectInstance... potionEffects)
	{
		this.potionEffects = potionEffects;
		return this;
	}

	@Override
	public int getFlammability(IBlockAccess world, BlockPos pos, Direction face)
	{
		return this.flammability;
	}

	@Override
	public int getFireSpreadSpeed(IBlockAccess world, BlockPos pos, Direction face)
	{
		return fireSpread;
	}

	@Override
	public boolean isFlammable(IBlockAccess world, BlockPos pos, Direction face)
	{
		return this.flammability > 0;
	}


	@Override
	public void onEntityCollision(World world, BlockPos pos, BlockState state, Entity entity)
	{
		if(potionEffects!=null&&entity instanceof LivingEntity)
		{
			for(EffectInstance effect : potionEffects)
				if(effect!=null)
					((LivingEntity)entity).addPotionEffect(new EffectInstance(effect));
		}
	}
}