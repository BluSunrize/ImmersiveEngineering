package blusunrize.immersiveengineering.api.wires.utils;

import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class WireLink
{
	public final ConnectionPoint cp;
	public final RegistryKey<World> dimension;
	public final BlockPos offset;
	public final TargetingInfo target;

	public WireLink(ConnectionPoint cp, RegistryKey<World> dimension, BlockPos offset, TargetingInfo info)
	{
		this.cp = cp;
		this.dimension = dimension;
		this.offset = offset.toImmutable();
		this.target = info;
	}

	public static WireLink create(ConnectionPoint cp, World world, BlockPos offset, TargetingInfo info)
	{
		return new WireLink(cp, world.getDimensionKey(), offset, info);
	}

	public void writeToItem(ItemStack stack)
	{
		CompoundNBT nbt = stack.getOrCreateTag();
		nbt.putString("linkingDim", dimension.getLocation().toString());
		nbt.put("linkingPos", cp.createTag());
		nbt.put("linkingOffset", NBTUtil.writeBlockPos(offset));
		CompoundNBT targetNBT = new CompoundNBT();
		target.writeToNBT(targetNBT);
		nbt.put("linkingTarget", targetNBT);
	}

	public static WireLink readFromItem(ItemStack stack)
	{
		CompoundNBT nbt = stack.hasTag()?stack.getOrCreateTag(): new CompoundNBT();
		ConnectionPoint cp = new ConnectionPoint(nbt.getCompound("linkingPos"));
		ResourceLocation dim = new ResourceLocation(nbt.getString("linkingDim"));
		BlockPos offset = NBTUtil.readBlockPos(nbt.getCompound("linkingOffset"));
		TargetingInfo info = TargetingInfo.readFromNBT(nbt.getCompound("linkingTarget"));
		return new WireLink(cp, RegistryKey.getOrCreateKey(Registry.WORLD_KEY, dim), offset, info);
	}
}
