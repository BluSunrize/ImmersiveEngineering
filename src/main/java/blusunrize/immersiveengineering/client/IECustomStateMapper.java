/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IIEMetaBlock;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;

public class IECustomStateMapper extends StateMapperBase
{
	public static HashMap<String, StateMapperBase> stateMappers = new HashMap<>();

	public static StateMapperBase getStateMapper(IIEMetaBlock metaBlock)
	{
		String key = metaBlock.getIEBlockName();
		StateMapperBase mapper = stateMappers.get(key);
		if(mapper==null)
		{
			mapper = metaBlock.getCustomMapper();
			if(mapper==null)
				mapper = new IECustomStateMapper();
			stateMappers.put(key, mapper);
		}
		return mapper;
	}

	@Override
	protected ModelResourceLocation getModelResourceLocation(IBlockState state)
	{
		try
		{
			ResourceLocation rl = Block.REGISTRY.getNameForObject(state.getBlock());
			IIEMetaBlock metaBlock = (IIEMetaBlock)state.getBlock();
			String custom = metaBlock.getCustomStateMapping(state.getBlock().getMetaFromState(state), false);
			if(custom!=null)
				rl = new ResourceLocation(rl.toString()+"_"+custom);
			String prop = metaBlock.appendPropertiesToState()?this.getPropertyString(state.getProperties()): null;
			return new ModelResourceLocation(rl, prop);
		} catch(Exception e)
		{
			e.printStackTrace();
			ResourceLocation rl = Block.REGISTRY.getNameForObject(state.getBlock());
			return new ModelResourceLocation(rl, this.getPropertyString(state.getProperties()));
		}
	}
}