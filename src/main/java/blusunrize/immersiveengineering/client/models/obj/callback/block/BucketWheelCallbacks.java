/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.obj.callback.block;

import blusunrize.immersiveengineering.api.client.ieobj.BlockCallback;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.client.models.obj.callback.block.BucketWheelCallbacks.Key;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.BucketWheelLogic.State;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BucketWheelCallbacks implements BlockCallback<Key>
{
	public static final BucketWheelCallbacks INSTANCE = new BucketWheelCallbacks();
	private static final BucketWheelCallbacks.Key INVALID = new BucketWheelCallbacks.Key(Collections.emptyMap());

	@Override
	public Key extractKey(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, @Nullable BlockState blockState, BlockEntity blockEntity)
	{
		if(!(blockEntity instanceof MultiblockBlockEntityMaster<?> masterBE))
			return getDefaultKey();
		if(!(masterBE.getHelper().getState() instanceof State state))
			return getDefaultKey();
		Map<String, TextureAtlasSprite> texMap = new HashMap<>();
		for(int i = 0; i < state.digStacks.size(); i++)
		{
			ItemStack stackAtIndex = state.digStacks.get(i);
			if(!stackAtIndex.isEmpty())
			{
				Block b = Block.byItem(stackAtIndex.getItem());
				BlockState digState = b!=Blocks.AIR?b.defaultBlockState(): Blocks.COBBLESTONE.defaultBlockState();
				BakedModel digModel = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(digState);
				texMap.put("dig"+i, digModel.getParticleIcon(ModelData.EMPTY));
			}
		}
		return new BucketWheelCallbacks.Key(texMap);
	}

	@Nullable
	@Override
	public TextureAtlasSprite getTextureReplacement(Key key, String group, String material)
	{
		return key.texMap.get(group);
	}

	@Override
	public Key getDefaultKey()
	{
		return INVALID;
	}

	@Override
	public boolean shouldRenderGroup(Key key, String group, RenderType layer)
	{
		return ("bucketWheel".equals(group)||key.texMap.containsKey(group));
	}

	public record Key(Map<String, TextureAtlasSprite> texMap)
	{
	}
}
