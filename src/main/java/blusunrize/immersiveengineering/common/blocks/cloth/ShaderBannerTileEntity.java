/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.shader.CapabilityShader;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper;
import blusunrize.immersiveengineering.api.shader.CapabilityShader.ShaderWrapper_Direct;
import blusunrize.immersiveengineering.common.blocks.IEBaseTileEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ICollisionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISelectionBounds;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ITileDrop;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.BannerTileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ShaderBannerTileEntity extends IEBaseTileEntity implements ICollisionBounds, ITileDrop
{
	public boolean wall = false;
	public ShaderWrapper_Direct shader = new ShaderWrapper_Direct(new ResourceLocation(ImmersiveEngineering.MODID, "banner"));

	public static TileEntityType<BannerTileEntity> TYPE;
	private static VoxelShape SHAPE = Block.makeCuboidShape(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);
	private static Map<Direction, VoxelShape> WALL_SHAPES = Maps.newEnumMap(ImmutableMap.of(
			Direction.NORTH, Block.makeCuboidShape(0.0D, 0.0D, 14.0D, 16.0D, 12.5D, 16.0D),
			Direction.SOUTH, Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 12.5D, 2.0D),
			Direction.WEST, Block.makeCuboidShape(14.0D, 0.0D, 0.0D, 16.0D, 12.5D, 16.0D),
			Direction.EAST, Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 2.0D, 12.5D, 16.0D)));

	public ShaderBannerTileEntity()
	{
		super(TYPE);
		reInitCapability();
	}

	@Override
	public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		this.wall = nbt.getBoolean("wall");
		if(nbt.contains("shader", NBT.TAG_COMPOUND))
		{
			shader = new ShaderWrapper_Direct(new ResourceLocation(ImmersiveEngineering.MODID, "banner"));
			shader.deserializeNBT(nbt.getCompound("shader"));
		}
	}

	@Override
	public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
	{
		nbt.putBoolean("wall", this.wall);
		nbt.put("shader", shader.serializeNBT());
	}

	@Nonnull
	@Override
	public VoxelShape getCollisionShape()
	{
		if(this.wall)
			return WALL_SHAPES.get(getState().get(ShaderBannerWallBlock.FACING));
		return SHAPE;
	}

	@Override
	public List<ItemStack> getTileDrops(LootContext context)
	{
		return ImmutableList.of(new ItemStack(Items.WHITE_BANNER, 1), this.shader.getShaderItem());
	}

	@Override
	public void readOnPlacement(@Nullable LivingEntity placer, ItemStack stack)
	{
	}

	@Override
	public boolean receiveClientEvent(int id, int arg)
	{
		if(id==0)
		{
			this.markContainingBlockForUpdate(null);
			return true;
		}
		return super.receiveClientEvent(id, arg);
	}

	private LazyOptional<ShaderWrapper> shaderCap;

	private void reInitCapability()
	{
		if(shaderCap!=null)
			unregisterCap(shaderCap);
		shaderCap = registerConstantCap(shader);
	}

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(capability==CapabilityShader.SHADER_CAPABILITY)
			return shaderCap.cast();
		return super.getCapability(capability, facing);
	}
}