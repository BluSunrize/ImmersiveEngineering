/*
 *  BluSunrize
 *  Copyright (c) 2021
 *
 *  This code is licensed under "Blu's License of Common Sense"
 *  Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.mixin.coremods;

import blusunrize.immersiveengineering.api.wires.IConnectionTemplate;
import blusunrize.immersiveengineering.common.wires.WireTemplateHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(StructureTemplate.class)
public abstract class TemplateMixin implements IConnectionTemplate
{
	private final List<TemplateConnection> connections = new ArrayList<>();

	@Inject(method = "fillFromWorld", at = @At("HEAD"))
	public void takeConnectionsFromWorld(
			Level worldIn, BlockPos startPos, Vec3i size, boolean p_163806_, Block p_163807_, CallbackInfo ci
	)
	{
		WireTemplateHelper.fillConnectionsInArea(worldIn, startPos, size, this);
	}

	@Inject(method = "placeInWorld", at = @At("RETURN"))
	public void addConnectionsToWorld(
			ServerLevelAccessor iworld,
			BlockPos startPos, BlockPos p_237146_3_, StructurePlaceSettings orientation,
			RandomSource p_237146_5_, int p_237146_6_, CallbackInfoReturnable<Boolean> cir
	)
	{
		if(cir.getReturnValue()==Boolean.TRUE)
			WireTemplateHelper.addConnectionsFromTemplate(iworld, this, orientation, startPos);
	}

	@Inject(method = "save", at = @At("RETURN"))
	public void writeConnectionsToNBT(CompoundTag $, CallbackInfoReturnable<CompoundTag> cir)
	{
		WireTemplateHelper.addConnectionsToNBT(this, cir.getReturnValue());
	}

	@Inject(method = "load", at = @At("RETURN"))
	public void readConnectionsFromNBT(HolderGetter<Block> blockAccess, CompoundTag compound, CallbackInfo ci)
	{
		WireTemplateHelper.readConnectionsFromNBT(compound, this);
	}

	@Override
	public List<TemplateConnection> getStoredConnections()
	{
		return connections;
	}
}
