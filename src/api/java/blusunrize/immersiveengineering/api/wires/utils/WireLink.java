package blusunrize.immersiveengineering.api.wires.utils;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WireLink
{
	public final ConnectionPoint cp;
	public final ResourceKey<Level> dimension;
	public final BlockPos offset;
	public final TargetingInfo target;

	public WireLink(ConnectionPoint cp, ResourceKey<Level> dimension, BlockPos offset, TargetingInfo info)
	{
		this.cp = cp;
		this.dimension = dimension;
		this.offset = offset.immutable();
		this.target = info;
	}

	public static WireLink create(ConnectionPoint cp, Level world, BlockPos offset, TargetingInfo info)
	{
		return new WireLink(cp, world.dimension(), offset, info);
	}

	public void writeToItem(ItemStack stack)
	{
		CompoundTag nbt = stack.getOrCreateTag();
		nbt.putString("linkingDim", dimension.location().toString());
		nbt.put("linkingPos", cp.createTag());
		nbt.put("linkingOffset", NbtUtils.writeBlockPos(offset));
		CompoundTag targetNBT = new CompoundTag();
		target.writeToNBT(targetNBT);
		nbt.put("linkingTarget", targetNBT);
	}

	public static WireLink readFromItem(ItemStack stack)
	{
		CompoundTag nbt = stack.hasTag()?stack.getOrCreateTag(): new CompoundTag();
		ConnectionPoint cp = new ConnectionPoint(nbt.getCompound("linkingPos"));
		ResourceLocation dim = new ResourceLocation(nbt.getString("linkingDim"));
		BlockPos offset = NbtUtils.readBlockPos(nbt.getCompound("linkingOffset"));
		TargetingInfo info = TargetingInfo.readFromNBT(nbt.getCompound("linkingTarget"));
		return new WireLink(cp, ResourceKey.create(Registry.DIMENSION_REGISTRY, dim), offset, info);
	}
}
