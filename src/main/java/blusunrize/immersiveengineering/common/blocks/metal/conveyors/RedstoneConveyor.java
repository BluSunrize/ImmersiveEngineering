/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal.conveyors;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.client.render.tile.DynamicModel;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Map;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;

/**
 * @author BluSunrize - 06.05.2017
 */
public class RedstoneConveyor extends BasicConveyor
{
	public static final ResourceLocation NAME = new ResourceLocation(MODID, "redstone");

	@OnlyIn(Dist.CLIENT)
	public static DynamicModel<Direction> MODEL_PANEL;
	public static ResourceLocation texture_panel = new ResourceLocation("immersiveengineering:block/conveyor/redstone");

	private boolean panelRight = true;

	public RedstoneConveyor(TileEntity tile)
	{
		super(tile);
	}

	/* Prevent Diagonals */

	@Override
	public boolean changeConveyorDirection()
	{
		return false;
	}

	@Override
	public boolean setConveyorDirection(ConveyorDirection dir)
	{
		return false;
	}

	/* Redstone & Player Interaction */

	@Override
	public boolean isActive()
	{
		return !isPowered();
	}

	@Override
	public boolean playerInteraction(PlayerEntity player, Hand hand, ItemStack heldItem, float hitX, float hitY, float hitZ, Direction side)
	{
		if(super.playerInteraction(player, hand, heldItem, hitX, hitY, hitZ, side))
			return true;
		if(Utils.isHammer(heldItem)&&player.isSneaking())
		{
			panelRight = !panelRight;
			return true;
		}
		return false;
	}

	/* NBT */

	@Override
	public CompoundNBT writeConveyorNBT()
	{
		CompoundNBT nbt = super.writeConveyorNBT();
		nbt.putBoolean("panelRight", panelRight);
		return nbt;
	}

	@Override
	public void readConveyorNBT(CompoundNBT nbt)
	{
		super.readConveyorNBT(nbt);
		panelRight = nbt.getBoolean("panelRight");
	}

	/* Selection Box */

	private static final Map<Direction, VoxelShape> WALL_SHAPES = Maps.newEnumMap(ImmutableMap.of(
			Direction.NORTH, Block.makeCuboidShape(13, 2, 5, 16, 16, 14.5),
			Direction.SOUTH, Block.makeCuboidShape(0, 2, 1.5, 3, 16, 11),
			Direction.WEST, Block.makeCuboidShape(5, 2, 0, 14.5, 16, 3),
			Direction.EAST, Block.makeCuboidShape(1.5, 2, 13, 11, 16, 16)));

	@Override
	public VoxelShape getSelectionShape()
	{
		VoxelShape ret = conveyorBounds;
		VoxelShape extensionShape = WALL_SHAPES.get(panelRight?getFacing(): getFacing().getOpposite());
		if(extensionShape!=null)
			ret = VoxelShapes.combine(ret, extensionShape, IBooleanFunction.OR);
		return ret;
	}

	/* Rendering */

	@Override
	public String getModelCacheKey()
	{
		String key = super.getModelCacheKey();
		key += "p"+this.panelRight;
		return key;
	}

	@Override
	public boolean renderWall(Direction facing, int wall)
	{
		if((panelRight&&wall==1)||(!panelRight&&wall==0))
			return true;
		return super.renderWall(facing, wall);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public List<BakedQuad> modifyQuads(List<BakedQuad> baseModel)
	{
		IBakedModel model = MODEL_PANEL.get(panelRight?getFacing(): getFacing().getOpposite());
		if(model!=null)
		{
			String[] parts = (getTile()!=null&&!isActive())?new String[]{"panel", "lamp"}: new String[]{"panel"};
			IEObjState objState = new IEObjState(VisibilityList.show(parts));
			BlockState state = ConveyorHandler.getBlock(NAME).getDefaultState();
			baseModel.addAll(model.getQuads(state, null, Utils.RAND,
					new SinglePropertyModelData<>(objState, Model.IE_OBJ_STATE)));
		}
		return baseModel;
	}
}
