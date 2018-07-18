/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.datafixers;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.util.inventory.IIEInventory;
import com.google.common.collect.ImmutableSet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.datafix.walkers.ItemStackData;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraftforge.common.util.CompoundDataFixer;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import static net.minecraft.util.datafix.FixTypes.BLOCK_ENTITY;
import static net.minecraft.util.datafix.FixTypes.ITEM_INSTANCE;

public class IEDataFixers
{
	public static void register()
	{
		CompoundDataFixer fixer = FMLCommonHandler.instance().getDataFixer();
		ModFixs fixs = fixer.init(ImmersiveEngineering.MODID,
				ImmersiveEngineering.DATA_FIXER_VERSION);
		fixs.registerFix(ITEM_INSTANCE, new DataFixerHammerCutterDamage());
		fixer.registerVanillaWalker(ITEM_INSTANCE, new IEItemFixWalker());

		fixer.registerVanillaWalker(BLOCK_ENTITY,
				new ItemStackData(TileEntityMetalPress.class, "mold"));
		fixer.registerVanillaWalker(BLOCK_ENTITY,
				new ItemStackData(TileEntityChargingStation.class, "inventory"));
		fixer.registerVanillaWalker(BLOCK_ENTITY,
				new ItemStackDataLists(TileEntityCrusher.class, "inputs"));
		fixer.registerVanillaWalker(BLOCK_ENTITY, new AssemblerPatternWalker());
		fixer.registerVanillaWalker(BLOCK_ENTITY, new BottlingQueueWalker());
		try
		{
			Set<Class<? extends TileEntity>> specialCases = ImmutableSet.of(TileEntityTurretChem.class,
					TileEntityExcavator.class,
					TileEntityMetalPress.class,
					TileEntityDieselGenerator.class,
					TileEntityCrusher.class,
					TileEntityChargingStation.class);
			Method isInWorld = TileEntityMultiblockMetal.class.getMethod("isInWorldProcessingMachine");

			for(Class<? extends TileEntity> cl : IEContent.registeredIETiles)
			{
				if(TileEntityMultiblockMetal.class.isAssignableFrom(cl))
				{
					Object te = cl.newInstance();
					if((boolean)isInWorld.invoke(te))
						fixer.registerVanillaWalker(BLOCK_ENTITY, new MultiblockProcessWalker(cl));
				}
				if(IIEInventory.class.isAssignableFrom(cl)&&!specialCases.contains(cl))
					fixer.registerVanillaWalker(BLOCK_ENTITY,
							new ItemStackDataLists(cl, "inventory"));
			}
		} catch(NoSuchMethodException|IllegalAccessException|InstantiationException|InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}
}
